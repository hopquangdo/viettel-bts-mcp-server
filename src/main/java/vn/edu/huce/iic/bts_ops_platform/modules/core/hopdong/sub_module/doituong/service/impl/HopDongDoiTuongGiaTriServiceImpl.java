package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.LienKetBangGiaTriNormalizer;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper.HopDongMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.service.ChinhSuaThongSoService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongGiaTriRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class HopDongDoiTuongGiaTriServiceImpl implements HopDongDoiTuongGiaTriService {

    private final HopDongDoiTuongGiaTriRepository hopDongDoiTuongGiaTriRepository;
    private final HopDongMapper hopDongMapper;
    private final AppEventContext appEventContext;
    private final vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService thuocTinhService;
    private final LienKetBangGiaTriNormalizer lienKetBangGiaTriNormalizer;
    private final ChinhSuaThongSoService chinhSuaThongSoService;
    private final HopDongDoiTuongService hopDongDoiTuongService;

    public HopDongDoiTuongGiaTriServiceImpl(
            HopDongDoiTuongGiaTriRepository hopDongDoiTuongGiaTriRepository,
            HopDongMapper hopDongMapper,
            AppEventContext appEventContext,
            vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services.ThuocTinhService thuocTinhService,
            LienKetBangGiaTriNormalizer lienKetBangGiaTriNormalizer,
            @Lazy ChinhSuaThongSoService chinhSuaThongSoService,
            @Lazy HopDongDoiTuongService hopDongDoiTuongService) {
        this.hopDongDoiTuongGiaTriRepository = hopDongDoiTuongGiaTriRepository;
        this.hopDongMapper = hopDongMapper;
        this.appEventContext = appEventContext;
        this.thuocTinhService = thuocTinhService;
        this.lienKetBangGiaTriNormalizer = lienKetBangGiaTriNormalizer;
        this.chinhSuaThongSoService = chinhSuaThongSoService;
        this.hopDongDoiTuongService = hopDongDoiTuongService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongGiaTri> findActiveEntitiesByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }
        return hopDongDoiTuongGiaTriRepository.findByHopDongDoiTuongIdInAndNgayXoaIsNull(hopDongDoiTuongIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongGiaTri> findActiveEntitiesByHopDongDoiTuongId(UUID hopDongDoiTuongId) {
        return hopDongDoiTuongGiaTriRepository.findByHopDongDoiTuongIdAndNgayXoaIsNull(hopDongDoiTuongId);
    }

    @Override
    @Transactional
    public HopDongDoiTuongGiaTri saveEntity(HopDongDoiTuongGiaTri entity) {
        return hopDongDoiTuongGiaTriRepository.save(entity);
    }

    @Override
    @Transactional
    public int softDeleteByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds, Instant now) {
        return hopDongDoiTuongGiaTriRepository.softDeleteByHopDongDoiTuongIds(hopDongDoiTuongIds, now);
    }

    @Override
    @Transactional
    public int softDeleteByFilter(
            Instant now,
            Boolean activeOnly,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            UUID hopDongNhomUuTienId,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            boolean hasExclude,
            Collection<UUID> excludeIds,
            boolean hasScope,
            Collection<UUID> scopeIds) {
        return hopDongDoiTuongGiaTriRepository.softDeleteByFilter(
                now, activeOnly, hopDongId, doiTuongQuanLyId, trangThaiHopDongId, hopDongNhomUuTienId,
                withoutNhomUuTien, withNhomUuTien, hasExclude, excludeIds, hasScope, scopeIds);
    }

    @Override
    @Transactional
    public int softDeleteByFilterWithKeyword(
            Instant now,
            Boolean activeOnly,
            UUID hopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId,
            UUID hopDongNhomUuTienId,
            String keyword,
            Boolean withoutNhomUuTien,
            Boolean withNhomUuTien,
            boolean hasExclude,
            Collection<UUID> excludeIds,
            boolean hasScope,
            Collection<UUID> scopeIds) {
        return hopDongDoiTuongGiaTriRepository.softDeleteByFilterWithKeyword(
                now, activeOnly, hopDongId, doiTuongQuanLyId, trangThaiHopDongId, hopDongNhomUuTienId, keyword,
                withoutNhomUuTien, withNhomUuTien, hasExclude, excludeIds, hasScope, scopeIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongGiaTriResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongDoiTuongId) {
        String keyword = EntityFilter.normalizeSearch(search);
        List<HopDongDoiTuongGiaTri> source = includeDeleted ? hopDongDoiTuongGiaTriRepository.findAll() : hopDongDoiTuongGiaTriRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(entity -> EntityFilter.isActive(entity, HopDongDoiTuongGiaTri::getHoatDong, activeOnly))
                .filter(entity -> hopDongDoiTuongId == null || hopDongDoiTuongId.equals(entity.getHopDongDoiTuongId()))
                .filter(entity -> EntityFilter.matchesKeyword(keyword, EntityFilter.nullToEmpty(entity.getGiaTri())))
                .sorted(Comparator.comparing(HopDongDoiTuongGiaTri::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(hopDongMapper::toHopDongDoiTuongGiaTriResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HopDongDoiTuongGiaTriResponse> getByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            return List.of();
        }
        return hopDongDoiTuongGiaTriRepository
                .findByHopDongDoiTuongIdInAndNgayXoaIsNull(hopDongDoiTuongIds.stream().distinct().toList())
                .stream()
                .map(hopDongMapper::toHopDongDoiTuongGiaTriResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HopDongDoiTuongGiaTriResponse getById(UUID id) {
        return hopDongMapper.toHopDongDoiTuongGiaTriResponse(findById(id));
    }

    @Override
    @Transactional
    public HopDongDoiTuongGiaTriResponse create(HopDongDoiTuongGiaTriTaoRequest request) {
        if (request.getGiaTri() != null && !request.getGiaTri().isBlank()) {
            chinhSuaThongSoService.requireDirectEditAllowed(
                    request.getHopDongDoiTuongId(), request.getThuocTinhId());
        }
        HopDongDoiTuongGiaTriResponse saved = createSkipPolicy(request);
        if (request.getGiaTri() != null && !request.getGiaTri().isBlank()) {
            recordDirectEdit(saved.getHopDongDoiTuongId(), saved.getThuocTinhId(), null, saved.getGiaTri());
        }
        return saved;
    }

    @Override
    @Transactional
    public HopDongDoiTuongGiaTriResponse createSkipPolicy(HopDongDoiTuongGiaTriTaoRequest request) {
        HopDongDoiTuongGiaTri entity = hopDongMapper.fromHopDongDoiTuongGiaTriTaoRequest(request);
        if (request.getGiaTri() != null) {
            entity.setGiaTri(lienKetBangGiaTriNormalizer.normalizeDoiTuongGiaTri(
                    request.getThuocTinhId(),
                    request.getGiaTri()));
        }
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        HopDongDoiTuongGiaTri saved = hopDongDoiTuongGiaTriRepository.save(entity);
        auditGiaTri("THEM_THUOC_TINH", saved, null, saved.getGiaTri());
        return hopDongMapper.toHopDongDoiTuongGiaTriResponse(saved);
    }

    @Override
    @Transactional
    public void createBatchForImport(List<HopDongDoiTuongGiaTriTaoRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        List<HopDongDoiTuongGiaTri> entities = new ArrayList<>(requests.size());
        for (HopDongDoiTuongGiaTriTaoRequest request : requests) {
            HopDongDoiTuongGiaTri entity = hopDongMapper.fromHopDongDoiTuongGiaTriTaoRequest(request);
            if (request.getGiaTri() != null) {
                entity.setGiaTri(lienKetBangGiaTriNormalizer.normalizeDoiTuongGiaTri(
                        request.getThuocTinhId(),
                        request.getGiaTri()));
            }
            entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
            entities.add(entity);
        }
        hopDongDoiTuongGiaTriRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void createBatchForImportResolved(List<HopDongDoiTuongGiaTriTaoRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        List<HopDongDoiTuongGiaTri> entities = new ArrayList<>(requests.size());
        for (HopDongDoiTuongGiaTriTaoRequest request : requests) {
            HopDongDoiTuongGiaTri entity = hopDongMapper.fromHopDongDoiTuongGiaTriTaoRequest(request);
            if (request.getGiaTri() != null) {
                entity.setGiaTri(request.getGiaTri().trim());
            }
            entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());
            entities.add(entity);
        }
        hopDongDoiTuongGiaTriRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public HopDongDoiTuongGiaTriResponse update(UUID id, HopDongDoiTuongGiaTriCapNhatRequest request) {
        HopDongDoiTuongGiaTri entity = findById(id);
        String giaTriTruoc = entity.getGiaTri();
        UUID thuocTinhId = request.getThuocTinhId() != null ? request.getThuocTinhId() : entity.getThuocTinhId();
        String giaTriMoi = request.getGiaTri() != null
                ? lienKetBangGiaTriNormalizer.normalizeDoiTuongGiaTri(thuocTinhId, request.getGiaTri())
                : giaTriTruoc;
        if (!sameGiaTri(giaTriTruoc, giaTriMoi)) {
            chinhSuaThongSoService.requireDirectEditAllowed(entity.getHopDongDoiTuongId(), thuocTinhId);
        }
        HopDongDoiTuongGiaTriResponse saved = updateSkipPolicy(id, request);
        if (!sameGiaTri(giaTriTruoc, saved.getGiaTri())) {
            recordDirectEdit(saved.getHopDongDoiTuongId(), saved.getThuocTinhId(), giaTriTruoc, saved.getGiaTri());
        }
        return saved;
    }

    @Override
    @Transactional
    public HopDongDoiTuongGiaTriResponse updateSkipPolicy(UUID id, HopDongDoiTuongGiaTriCapNhatRequest request) {
        HopDongDoiTuongGiaTri entity = findById(id);
        String giaTriTruoc = entity.getGiaTri();
        hopDongMapper.updateFromHopDongDoiTuongGiaTriCapNhatRequest(request, entity);
        if (request.getGiaTri() != null) {
            UUID thuocTinhId = request.getThuocTinhId() != null ? request.getThuocTinhId() : entity.getThuocTinhId();
            entity.setGiaTri(lienKetBangGiaTriNormalizer.normalizeDoiTuongGiaTri(thuocTinhId, request.getGiaTri()));
        }

        HopDongDoiTuongGiaTri saved = hopDongDoiTuongGiaTriRepository.save(entity);
        auditGiaTri("SUA_THUOC_TINH", saved, giaTriTruoc, saved.getGiaTri());
        return hopDongMapper.toHopDongDoiTuongGiaTriResponse(saved);
    }

    private void recordDirectEdit(UUID hopDongDoiTuongId, UUID thuocTinhId, String giaTriCu, String giaTriMoi) {
        UUID hopDongId = hopDongDoiTuongService.getById(hopDongDoiTuongId).getHopDongId();
        chinhSuaThongSoService.ghiApDungTrucTiep(
                hopDongId,
                hopDongDoiTuongId,
                thuocTinhId,
                tenThuocTinhAudit(thuocTinhId),
                giaTriCu,
                giaTriMoi);
    }

    private static boolean sameGiaTri(String a, String b) {
        String left = a == null ? "" : a.trim();
        String right = b == null ? "" : b.trim();
        return left.equals(right);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HopDongDoiTuongGiaTri entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        hopDongDoiTuongGiaTriRepository.save(entity);
        auditGiaTri("XOA_THUOC_TINH", entity, entity.getGiaTri(), null);
    }

    /** Audit thay đổi giá trị thuộc tính của trạm — gắn doiTuongIds để hiện trong Lịch sử
     * chỉnh sửa trạm. CHỈ các thao tác qua API đơn lẻ (sửa tay/bulk-edit từ FE) đi qua đây;
     * import Excel dùng createBatchForImport/saveEntity nên không tràn log theo từng ô. */
    private void auditGiaTri(String action, HopDongDoiTuongGiaTri entity, String truoc, String sau) {
        try {
            String detail = "'" + rutGonAudit(truoc) + "' → '" + rutGonAudit(sau) + "'";
            appEventContext.audit(action, "Thuộc tính " + tenThuocTinhAudit(entity.getThuocTinhId()), detail,
                    java.util.Map.of("doiTuongIds", java.util.List.of(entity.getHopDongDoiTuongId())));
        } catch (Exception ignored) {
            // audit best-effort — không được làm hỏng thao tác gốc
        }
    }

    private String tenThuocTinhAudit(UUID thuocTinhId) {
        if (thuocTinhId == null) {
            return "(không rõ)";
        }
        try {
            return thuocTinhService.getById(thuocTinhId).getTen();
        } catch (Exception ex) {
            return thuocTinhId.toString().substring(0, 8);
        }
    }

    private static String rutGonAudit(String value) {
        if (value == null || value.isBlank()) {
            return "trống";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 60 ? trimmed : trimmed.substring(0, 57) + "...";
    }

    private HopDongDoiTuongGiaTri findById(UUID id) {
        return hopDongDoiTuongGiaTriRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_GIA_TRI_NOT_FOUND, "Không tìm thấy bản ghi"));
    }
}
