package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucImportMappingFields;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.ChiTietResolveResult;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.LeafRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.RowQuantities;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucCongViecRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class HangMucImportPersistenceHelper {

    private final EntityManager entityManager;
    private final HangMucNhomRepository hangMucNhomRepository;
    private final HangMucChiTietRepository hangMucChiTietRepository;
    private final HangMucCongViecRepository hangMucCongViecRepository;
    private final HangMucImportRowHelper rowHelper;

    public ImportCounters importGroupedRow(
            UUID hopDongId,
            HangMucImportContext ctx,
            Map<String, Object> row,
            HangMucImportMappingFields fields,
            String tenNhomHangMuc,
            String tenHangMuc,
            short groupOrder) {
        LeafRow leaf = rowHelper.resolveLeafRow(row, fields, tenHangMuc);
        if (leaf.leafTen() == null) {
            return ImportCounters.skipped(1);
        }

        UUID groupId = HangMucImportTextHelper.isBlank(tenNhomHangMuc)
                ? ctx.flatGroupId
                : resolveGroupByHangMuc(hopDongId, ctx, tenNhomHangMuc, groupOrder);

        if (leaf.asChiTiet()) {
            ChiTietResolveResult result = upsertChiTiet(
                    hopDongId, ctx, groupId, tenNhomHangMuc, leaf.leafTen(), row, fields, true);
            return result.created()
                    ? ImportCounters.created(1)
                    : ImportCounters.updated(1);
        }

        UUID chiTietId = HangMucImportTextHelper.isBlank(leaf.chiTietTen())
                ? resolveDefaultChiTiet(hopDongId, ctx, groupId, tenNhomHangMuc)
                : upsertChiTiet(hopDongId, ctx, groupId, tenNhomHangMuc, leaf.chiTietTen(), row, fields, false).id();

        return upsertCongViec(hopDongId, ctx, chiTietId, leaf.leafTen(), row, fields);
    }

    public UUID ensureDefaultImportGroup(UUID hopDongId, HangMucImportContext ctx) {
        return resolveGroup(
                hopDongId,
                ctx,
                HangMucImportFields.DEFAULT_GROUP_CACHE_KEY,
                HangMucImportMaHelper.buildDefaultImportGroupMa(ctx.contractSuffix, hopDongId),
                "",
                HangMucImportFields.DEFAULT_GROUP_MO_TA,
                (short) 0,
                () -> {
                    List<HangMucNhom> hopDongGroups =
                            hangMucNhomRepository.findByHopDongIdAndNgayXoaIsNull(hopDongId);
                    return hopDongGroups.isEmpty() ? null : hopDongGroups.get(0).getId();
                });
    }

    private UUID resolveGroupByHangMuc(
            UUID hopDongId,
            HangMucImportContext ctx,
            String tenNhomHangMuc,
            short groupOrder) {
        return resolveGroup(
                hopDongId,
                ctx,
                HangMucImportTextHelper.normalizeKey(tenNhomHangMuc),
                HangMucImportMaHelper.buildGroupedNhomMa(ctx.contractSuffix, tenNhomHangMuc),
                tenNhomHangMuc.trim(),
                HangMucImportFields.GROUP_IMPORT_MO_TA,
                groupOrder,
                () -> null);
    }

    private UUID resolveGroup(
            UUID hopDongId,
            HangMucImportContext ctx,
            String cacheKey,
            String groupMa,
            String ten,
            String moTa,
            short thuTu,
            Supplier<UUID> existingGroupId) {
        if (ctx.groupByHangMucKey.containsKey(cacheKey)) {
            return ctx.groupByHangMucKey.get(cacheKey);
        }

        UUID presetId = existingGroupId.get();
        if (presetId != null) {
            ctx.groupByHangMucKey.put(cacheKey, presetId);
            return presetId;
        }

        HangMucNhom existing = findActiveNhomByMa(ctx, hopDongId, groupMa);
        if (existing != null) {
            UUID groupId = existing.getId();
            ctx.groupByHangMucKey.put(cacheKey, groupId);
            return groupId;
        }

        String uniqueMa = ctx.allocateUniqueNhomMa(groupMa);
        HangMucNhom group = new HangMucNhom();
        group.setHopDongId(hopDongId);
        group.setMa(uniqueMa);
        group.setTen(ten);
        group.setMoTa(moTa);
        group.setThuTu(thuTu);
        group.setHoatDong(true);
        entityManager.persist(group);
        ctx.indexNhom(group);
        ctx.groupByHangMucKey.put(cacheKey, group.getId());
        return group.getId();
    }

    private HangMucNhom findActiveNhomByMa(HangMucImportContext ctx, UUID hopDongId, String groupMa) {
        HangMucNhom cached = ctx.nhomByMaLower.get(HangMucImportFields.maKey(groupMa));
        if (cached != null && hopDongId.equals(cached.getHopDongId())) {
            return cached;
        }
        return hangMucNhomRepository.findByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(groupMa, hopDongId)
                .map(group -> {
                    ctx.indexNhom(group);
                    return group;
                })
                .orElse(null);
    }

    private UUID resolveDefaultChiTiet(
            UUID hopDongId,
            HangMucImportContext ctx,
            UUID groupId,
            String tenNhomHangMuc) {
        if (ctx.defaultChiTietByGroup.containsKey(groupId)) {
            return ctx.defaultChiTietByGroup.get(groupId);
        }

        String chiTietMa = HangMucImportMaHelper.buildDefaultChiTietMa(ctx.contractSuffix, tenNhomHangMuc);
        HangMucChiTiet existing = findActiveChiTietByMa(ctx, hopDongId, chiTietMa);
        if (existing != null) {
            ctx.defaultChiTietByGroup.put(groupId, existing.getId());
            return existing.getId();
        }

        HangMucChiTiet dsItem = ctx.chiTietsInGroup(groupId).stream()
                .filter(item -> item.getMa() != null
                        && item.getMa().endsWith(HangMucImportFields.DEFAULT_CHI_TIET_DS_SUFFIX))
                .findFirst()
                .orElse(null);
        if (dsItem != null) {
            ctx.defaultChiTietByGroup.put(groupId, dsItem.getId());
            return dsItem.getId();
        }

        chiTietMa = ctx.allocateUniqueChiTietMa(chiTietMa);
        String ten = HangMucImportTextHelper.isBlank(tenNhomHangMuc)
                ? HangMucImportFields.DEFAULT_CHI_TIET_TEN
                : HangMucImportFields.DEFAULT_CHI_TIET_TEN + " — " + tenNhomHangMuc.trim();
        HangMucChiTiet entity = rowHelper.newChiTiet(groupId, chiTietMa, ten, BigDecimal.ZERO, BigDecimal.ZERO);
        entityManager.persist(entity);
        ctx.indexChiTiet(entity);
        ctx.defaultChiTietByGroup.put(groupId, entity.getId());
        return entity.getId();
    }

    private ChiTietResolveResult upsertChiTiet(
            UUID hopDongId,
            HangMucImportContext ctx,
            UUID groupId,
            String tenNhomHangMuc,
            String tenHangMuc,
            Map<String, Object> row,
            HangMucImportMappingFields fields,
            boolean useMappedMa) {
        String cacheKey = HangMucImportTextHelper.hangMucCacheKey(tenNhomHangMuc, tenHangMuc);
        if (ctx.chiTietByHangMucKey.containsKey(cacheKey)) {
            UUID existingId = ctx.chiTietByHangMucKey.get(cacheKey);
            HangMucChiTiet cached = ctx.chiTietById.get(existingId);
            if (cached != null) {
                rowHelper.applyChiTietFields(cached, groupId, tenHangMuc, row, fields);
            }
            return new ChiTietResolveResult(existingId, false);
        }

        String chiTietMa = rowHelper.resolveImportMa(
                ctx, row, fields, ImportMaPrefix.HM, useMappedMa);
        HangMucChiTiet existing = findActiveChiTietByMa(ctx, hopDongId, chiTietMa);
        if (existing != null) {
            rowHelper.applyChiTietFields(existing, groupId, tenHangMuc, row, fields);
            ctx.chiTietByHangMucKey.put(cacheKey, existing.getId());
            return new ChiTietResolveResult(existing.getId(), false);
        }

        if (useMappedMa && rowHelper.readMappedMa(row, fields) != null) {
            chiTietMa = ctx.allocateUniqueChiTietMa(chiTietMa);
        }

        HangMucChiTiet entity = rowHelper.newChiTiet(groupId, chiTietMa, tenHangMuc.trim(), null, null);
        rowHelper.applyChiTietFields(entity, groupId, tenHangMuc, row, fields);
        entityManager.persist(entity);
        ctx.indexChiTiet(entity);
        ctx.chiTietByHangMucKey.put(cacheKey, entity.getId());
        return new ChiTietResolveResult(entity.getId(), true);
    }

    private ImportCounters upsertCongViec(
            UUID hopDongId,
            HangMucImportContext ctx,
            UUID chiTietId,
            String tenCongViec,
            Map<String, Object> row,
            HangMucImportMappingFields fields) {
        String ma = rowHelper.resolveImportMa(
                ctx, row, fields, ImportMaPrefix.CV, true);
        RowQuantities qty = rowHelper.readRowQuantities(row, fields);

        HangMucCongViec existing = findActiveCongViecByMa(ctx, hopDongId, ma);
        if (existing != null) {
            rowHelper.applyCongViecFields(existing, chiTietId, tenCongViec, qty);
            return ImportCounters.updated(1);
        }

        if (rowHelper.readMappedMa(row, fields) != null) {
            ma = ctx.allocateUniqueCongViecMa(ma);
        }

        HangMucCongViec entity = new HangMucCongViec();
        rowHelper.applyCongViecFields(entity, chiTietId, tenCongViec, qty);
        entity.setMa(ma);
        entity.setTrangThai(HangMucImportFields.TRANG_THAI_CONG_VIEC_PENDING);
        entityManager.persist(entity);
        ctx.indexCongViec(entity);
        return ImportCounters.created(1);
    }

    private HangMucChiTiet findActiveChiTietByMa(HangMucImportContext ctx, UUID hopDongId, String ma) {
        HangMucChiTiet cached = ctx.chiTietByMaLower.get(HangMucImportFields.maKey(ma));
        if (cached != null && belongsToHopDong(ctx, cached, hopDongId)) {
            return cached;
        }
        return hangMucChiTietRepository.findByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(ma, hopDongId)
                .map(chiTiet -> {
                    ctx.indexChiTiet(chiTiet);
                    return chiTiet;
                })
                .orElse(null);
    }

    private HangMucCongViec findActiveCongViecByMa(HangMucImportContext ctx, UUID hopDongId, String ma) {
        HangMucCongViec cached = ctx.congViecByMaLower.get(HangMucImportFields.maKey(ma));
        if (cached != null && belongsToHopDong(ctx, cached, hopDongId)) {
            return cached;
        }
        return hangMucCongViecRepository.findByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(ma, hopDongId)
                .map(congViec -> {
                    ctx.indexCongViec(congViec);
                    return congViec;
                })
                .orElse(null);
    }

    private boolean belongsToHopDong(HangMucImportContext ctx, HangMucChiTiet chiTiet, UUID hopDongId) {
        if (chiTiet.getHangMucNhomId() == null) {
            return false;
        }
        HangMucNhom group = ctx.nhomById.get(chiTiet.getHangMucNhomId());
        return group != null && hopDongId.equals(group.getHopDongId());
    }

    private boolean belongsToHopDong(HangMucImportContext ctx, HangMucCongViec congViec, UUID hopDongId) {
        if (congViec.getHangMucChiTietId() == null) {
            return false;
        }
        HangMucChiTiet chiTiet = ctx.chiTietById.get(congViec.getHangMucChiTietId());
        return chiTiet != null && belongsToHopDong(ctx, chiTiet, hopDongId);
    }
}
