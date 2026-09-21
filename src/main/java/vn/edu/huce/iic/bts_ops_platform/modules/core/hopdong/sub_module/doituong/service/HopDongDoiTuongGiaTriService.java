package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HopDongDoiTuongGiaTriService {

    /** For same-domain callers (e.g. HopDongDoiTuongService) that need entities, not response DTOs. */
    List<HopDongDoiTuongGiaTri> findActiveEntitiesByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds);

    /** For same-domain callers (e.g. HopDongService) that need entities, not response DTOs. */
    List<HopDongDoiTuongGiaTri> findActiveEntitiesByHopDongDoiTuongId(UUID hopDongDoiTuongId);

    /** Low-level save for same-domain callers (e.g. HopDongService) doing internal cascades. */
    HopDongDoiTuongGiaTri saveEntity(HopDongDoiTuongGiaTri entity);

    /** Bulk soft-delete mirroring the repository's bulk update, for same-domain callers (HopDongDoiTuongService). */
    int softDeleteByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds, Instant now);

    /** Bulk soft-delete by filter mirroring the repository's bulk update, for same-domain callers. */
    int softDeleteByFilter(
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
            Collection<UUID> scopeIds);

    /** Same as {@link #softDeleteByFilter} plus a keyword match, for same-domain callers. */
    int softDeleteByFilterWithKeyword(
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
            Collection<UUID> scopeIds);
    List<HopDongDoiTuongGiaTriResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongDoiTuongId);

    /** Lấy giá trị đối tượng theo danh sách id đối tượng hợp đồng (bỏ bản ghi đã xóa). Dùng dashboard. */
    List<HopDongDoiTuongGiaTriResponse> getByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds);
    HopDongDoiTuongGiaTriResponse getById(UUID id);
    HopDongDoiTuongGiaTriResponse create(HopDongDoiTuongGiaTriTaoRequest request);
    void createBatchForImport(List<HopDongDoiTuongGiaTriTaoRequest> requests);

    /** Giá trị đã được chuẩn hóa ở bước resolve import — không tra cứu lại thuộc tính/liên kết. */
    void createBatchForImportResolved(List<HopDongDoiTuongGiaTriTaoRequest> requests);
    HopDongDoiTuongGiaTriResponse update(UUID id, HopDongDoiTuongGiaTriCapNhatRequest request);

    /** Cập nhật giá trị bỏ qua kiểm soát số lần sửa — chỉ dùng nội bộ khi phê duyệt đề xuất. */
    HopDongDoiTuongGiaTriResponse updateSkipPolicy(UUID id, HopDongDoiTuongGiaTriCapNhatRequest request);

    /** Tạo giá trị bỏ qua kiểm soát — chỉ dùng nội bộ khi phê duyệt đề xuất. */
    HopDongDoiTuongGiaTriResponse createSkipPolicy(HopDongDoiTuongGiaTriTaoRequest request);

    void delete(UUID id);
}
