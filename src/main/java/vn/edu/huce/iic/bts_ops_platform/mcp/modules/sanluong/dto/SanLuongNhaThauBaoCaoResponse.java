package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * sanluong_nha_thau_bao_cao: 1 danh sách nhà thầu theo đúng 1 trạng thái báo sản lượng trong kỳ, có phân trang.
 *
 * @param kyBaoCao       "01/01/2026 – 30/09/2026"
 * @param trangThai      "Đã báo sản lượng" | "Chưa báo sản lượng"
 * @param tongSo         tổng số nhà thầu khớp (mọi trang)
 * @param trang          trang hiện tại, bắt đầu từ 0
 * @param soDongMoiTrang số dòng mỗi trang
 * @param tongSoTrang    tổng số trang
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanLuongNhaThauBaoCaoResponse(
        String kyBaoCao,
        String trangThai,
        long tongSo,
        int trang,
        int soDongMoiTrang,
        int tongSoTrang,
        List<SanLuongNhaThauBaoCaoItem> danhSach,
        String ghiChu) {
}
