package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import java.util.UUID;

/** Số đối tượng hợp đồng đang hoạt động nhóm theo loại hợp đồng và đối tượng quản lý. */
public record HopDongDoiTuongLoaiThongKeResponse(
        UUID loaiHopDongId,
        UUID doiTuongQuanLyId,
        long soLuong) {
}
