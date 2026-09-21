package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucChiTietRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucCongViecRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository.HangMucNhomRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cache dữ liệu hợp đồng trong một lần import — tránh query/save từng dòng.
 */
public final class HangMucImportContext {

    public final UUID hopDongId;
    public final String contractSuffix;
    public UUID flatGroupId;
    public final Map<String, UUID> groupByHangMucKey = new HashMap<>();
    public final Map<String, UUID> chiTietByHangMucKey = new HashMap<>();
    public final Map<UUID, UUID> defaultChiTietByGroup = new HashMap<>();
    public final Map<String, HangMucNhom> nhomByMaLower = new HashMap<>();
    public final Map<UUID, HangMucNhom> nhomById = new HashMap<>();
    public final Map<String, HangMucChiTiet> chiTietByMaLower = new HashMap<>();
    public final Map<String, HangMucCongViec> congViecByMaLower = new HashMap<>();
    public final Map<UUID, HangMucChiTiet> chiTietById = new HashMap<>();
    public final Map<UUID, List<HangMucChiTiet>> chiTietsByGroupId = new HashMap<>();
    /** Mã đã dùng (kể cả bản ghi đã xóa mềm) — tránh tái sử dụng / khôi phục nhầm. */
    public final Set<String> occupiedNhomMaLower = new HashSet<>();
    public final Set<String> occupiedChiTietMaLower = new HashSet<>();
    public final Set<String> occupiedCongViecMaLower = new HashSet<>();
    private final Set<String> reservedMaLower = new HashSet<>();
    private int nextHmSeq;
    private int nextCvSeq;

    private HangMucImportContext(UUID hopDongId, String contractSuffix) {
        this.hopDongId = hopDongId;
        this.contractSuffix = contractSuffix;
    }

    public static HangMucImportContext preload(
            UUID hopDongId,
            String contractSuffix,
            HangMucNhomRepository nhomRepo,
            HangMucChiTietRepository chiTietRepo,
            HangMucCongViecRepository congViecRepo) {
        HangMucImportContext ctx = new HangMucImportContext(hopDongId, contractSuffix);
        List<HangMucNhom> groups = nhomRepo.findByHopDongIdAndNgayXoaIsNull(hopDongId);
        groups.forEach(ctx::indexNhom);

        List<UUID> groupIds = groups.stream().map(HangMucNhom::getId).toList();
        if (!groupIds.isEmpty()) {
            List<HangMucChiTiet> chiTiets = chiTietRepo.findByHangMucNhomIdInAndNgayXoaIsNull(groupIds);
            chiTiets.forEach(ctx::indexChiTiet);
            List<UUID> chiTietIds = chiTiets.stream().map(HangMucChiTiet::getId).toList();
            if (!chiTietIds.isEmpty()) {
                congViecRepo.findByHangMucChiTietIdInAndNgayXoaIsNull(chiTietIds).forEach(ctx::indexCongViec);
            }
        }

        ctx.occupiedNhomMaLower.addAll(nhomRepo.findAllMaLowerByHopDongId(hopDongId));
        ctx.occupiedChiTietMaLower.addAll(chiTietRepo.findAllMaLowerByHopDongId(hopDongId));
        ctx.occupiedCongViecMaLower.addAll(congViecRepo.findAllMaLowerByHopDongId(hopDongId));

        String hmPrefix = contractSuffix + "_" + HangMucImportFields.MA_PREFIX_HM;
        String cvPrefix = contractSuffix + "_" + HangMucImportFields.MA_PREFIX_CV;
        ctx.nextHmSeq = HangMucImportMaHelper.computeNextSeq(ctx.occupiedChiTietMaLower, hmPrefix);
        ctx.nextCvSeq = HangMucImportMaHelper.computeNextSeq(ctx.occupiedCongViecMaLower, cvPrefix);
        return ctx;
    }

    public String nextAutoMa(ImportMaPrefix prefix) {
        int seq = prefix == ImportMaPrefix.HM ? nextHmSeq++ : nextCvSeq++;
        return HangMucImportMaHelper.nextAutoMa(contractSuffix, prefix, seq);
    }

    public void indexNhom(HangMucNhom group) {
        nhomById.put(group.getId(), group);
        if (group.getMa() != null) {
            String key = HangMucImportFields.maKey(group.getMa());
            nhomByMaLower.put(key, group);
            occupiedNhomMaLower.add(key);
        }
    }

    public void indexChiTiet(HangMucChiTiet chiTiet) {
        chiTietById.put(chiTiet.getId(), chiTiet);
        if (chiTiet.getMa() != null) {
            String key = HangMucImportFields.maKey(chiTiet.getMa());
            chiTietByMaLower.put(key, chiTiet);
            occupiedChiTietMaLower.add(key);
        }
        chiTietsByGroupId.computeIfAbsent(chiTiet.getHangMucNhomId(), key -> new ArrayList<>()).add(chiTiet);
    }

    public void indexCongViec(HangMucCongViec congViec) {
        if (congViec.getMa() != null) {
            String key = HangMucImportFields.maKey(congViec.getMa());
            congViecByMaLower.put(key, congViec);
            occupiedCongViecMaLower.add(key);
        }
    }

    public List<HangMucChiTiet> chiTietsInGroup(UUID groupId) {
        return chiTietsByGroupId.getOrDefault(groupId, List.of());
    }

    public String allocateUniqueNhomMa(String baseMa) {
        return HangMucImportMaHelper.allocateUniqueMa(
                baseMa, nhomByMaLower, occupiedNhomMaLower, reservedMaLower);
    }

    public String allocateUniqueChiTietMa(String baseMa) {
        return HangMucImportMaHelper.allocateUniqueMa(
                baseMa, chiTietByMaLower, occupiedChiTietMaLower, reservedMaLower);
    }

    public String allocateUniqueCongViecMa(String baseMa) {
        return HangMucImportMaHelper.allocateUniqueMa(
                baseMa, congViecByMaLower, occupiedCongViecMaLower, reservedMaLower);
    }
}
