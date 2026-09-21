package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongTrangThaiResponse;

import java.util.List;
import java.util.UUID;

public interface HopDongDoiTuongTrangThaiService {
    List<HopDongDoiTuongTrangThaiResponse> list(
            String search,
            Boolean activeOnly,
            boolean includeDeleted,
            UUID doiTuongId,
            UUID kieuHopDongId,
            UUID loaiHopDongId);

    HopDongDoiTuongTrangThaiResponse getById(UUID id);

    HopDongDoiTuongTrangThaiResponse create(HopDongDoiTuongTrangThaiTaoRequest request);

    HopDongDoiTuongTrangThaiResponse update(UUID id, HopDongDoiTuongTrangThaiCapNhatRequest request);

    void delete(UUID id);

    /** Đồng bộ cấu hình trạng thái đối tượng theo luồng trạng thái của kiểu HĐ. */
    void syncFromLuong(UUID kieuHopDongId, UUID loaiHopDongId, UUID luongTrangThaiId);

    /** Re-sync tất cả kiểu HĐ đang dùng luồng này (sau khi sửa danh sách bước). */
    void syncAllKieuUsingLuong(UUID luongTrangThaiId);

    /** Gỡ luồng khỏi kiểu HĐ — xóa mềm cấu hình trạng thái đối tượng. */
    void removeForKieu(UUID kieuHopDongId, UUID loaiHopDongId);

    /** Trạng thái mặc định (bước đầu luồng) cho đối tượng quản lý. */
    UUID resolveDefaultTrangThaiId(UUID kieuHopDongId, UUID loaiHopDongId, UUID doiTuongQuanLyId);

    boolean isAllowedTrangThai(
            UUID kieuHopDongId,
            UUID loaiHopDongId,
            UUID doiTuongQuanLyId,
            UUID trangThaiHopDongId);
}
