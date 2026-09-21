package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;

import java.util.Map;
import java.util.UUID;

public record HopDongLabelLookups(
        Map<UUID, String> tinhById,
        Map<UUID, String> tinhMaById,
        Map<String, String> tinhTenByMa,
        Map<UUID, String> khuById,
        Map<UUID, String> khuMaById,
        Map<UUID, String> nguoiDungById,
        Map<UUID, ThuocTinhResponse> thuocTinhById,
        /** id tỉnh hiện tại (tinh_thanh.id) -> mã tỉnh cũ cùng nhóm sáp nhập (tinh_thanh.ma, la_tinh_cu=true). */
        Map<UUID, String> tinhCuMaByTinhId,
        /** id tỉnh hiện tại -> tên tỉnh cũ cùng nhóm sáp nhập. */
        Map<UUID, String> tinhCuTenByTinhId,
        /** Mã/tên tỉnh (uppercase / không dấu) -> mã tỉnh hiện tại trong nhóm sáp nhập (cột Tỉnh mới). */
        Map<String, String> tinhCanonicalMaByKey,
        /** Mã/tên -> mã hiển thị gốc trên HĐ (cột Tỉnh thành, không gộp sáp nhập). */
        Map<String, String> tinhDisplayMaByKey,
        /** Mã tỉnh hiện tại -> tên tỉnh mới (la_tinh_cu=false). */
        Map<String, String> tinhMoiTenByMa,
        /** Mã tỉnh hiện tại -> mã tỉnh cũ cùng nhóm sáp nhập (khi chỉ suy được tỉnh từ mã trạm). */
        Map<String, String> tinhCuMaByProvinceMa,
        /** Mã tỉnh hiện tại -> mã khu vực (tinh_thanh_khu_vuc). */
        Map<String, String> khuMaByProvinceMa) {
}
