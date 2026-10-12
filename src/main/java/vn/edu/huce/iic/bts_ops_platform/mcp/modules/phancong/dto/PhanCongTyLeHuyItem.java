package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

/**
 * 1 nhóm (nhà thầu/cán bộ hoặc khu vực) của phancong_ty_le_huy_tram.
 *
 * @param ten        tên nhà thầu/cán bộ được giao (họ tên) hoặc tên khu vực; "Chưa gán ..." khi trống
 * @param tyLeHuy    "100%" = soTramHuy / tongTram
 * @param vuotNguong tỷ lệ hủy lớn hơn ngưỡng cảnh báo
 */
public record PhanCongTyLeHuyItem(String ten, long tongTram, long soTramHuy, String tyLeHuy, boolean vuotNguong) {
}
