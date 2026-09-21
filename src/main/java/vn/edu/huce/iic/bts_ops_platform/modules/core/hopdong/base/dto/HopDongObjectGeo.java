package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto;

import java.util.UUID;

/** Phân giải vị trí / nhà thầu của đối tượng HĐ (volume, danh sách HĐ). */
public record HopDongObjectGeo(
        UUID hopDongDoiTuongId,
        UUID hopDongId,
        String region,
        String province,
        String provinceKey,
        String oldProvince,
        String contractor,
        String maDoiTuong,
        /** Địa chỉ/vị trí cụ thể (thuộc tính EAV dạng "địa chỉ"/"vị trí"/"điểm") — "—" nếu đối
         * tượng không có thuộc tính này. */
        String diaChi,
        /** UUID thật của khu_vuc/tinh_thanh khi region/province được gán trực tiếp (không phải
         * suy từ mã trạm) — null nếu suy gián tiếp. Dùng để lọc theo id thay vì so sánh label. */
        UUID khuVucId,
        UUID tinhThanhId) {
}
