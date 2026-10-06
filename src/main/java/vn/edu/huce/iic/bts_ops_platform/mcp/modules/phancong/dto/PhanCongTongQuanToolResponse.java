package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * phancong_tong_quan: số liệu tổng hợp phân công.
 *
 * @param tongPhanCong       số đối tượng đã phân nhà thầu
 * @param tongDoiTuong       tổng số đối tượng đang hoạt động trong phạm vi lọc
 * @param tyLeDaPhanNhaThau  "82,5%"
 * @param tongDoiTuong       ô "Tổng đối tượng" trên Dashboard và "Tổng trạm theo HĐ" trên màn Phân công
 * @param soKhuVuc           số khu vực đang hoạt động trong phạm vi dữ liệu (ô "Khu vực" trên Dashboard)
 * @param theoKhuVuc         số phân công theo khu vực
 * @param theoTinhThanh      số phân công theo tỉnh/thành
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PhanCongTongQuanToolResponse(
        long tongPhanCong,
        long tongDoiTuong,
        long tongDaPhanNhaThau,
        String tyLeDaPhanNhaThau,
        long soNhaThau,
        long tongHoanThanh,
        long tongVuongMac,
        long tongTon,
        long soKhuVuc,
        Map<String, Long> theoKhuVuc,
        Map<String, Long> theoTinhThanh) {

    public static PhanCongTongQuanToolResponse of(PhanCongToolThongKe tk) {
        return new PhanCongTongQuanToolResponse(tk.getTongPhanCong(), tk.getTongHoatDong(), tk.getTongDaPhanNhaThau(),
                vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat.phanTram(tk.getTyLeDaPhanNhaThau()),
                tk.getSoNhaThau(), tk.getTongHoanThanh(), tk.getTongVuongMac(), tk.getTongTon(), tk.getSoKhuVuc(),
                nhan(tk.getTheoKhuVuc()), nhan(tk.getTheoTinhThanh()));
    }

    /** Khoá "null" (chưa gán) -> "Chưa xác định". */
    private static Map<String, Long> nhan(Map<String, Long> m) {
        if (m == null) {
            return null;
        }
        Map<String, Long> out = new java.util.LinkedHashMap<>();
        m.forEach((k, v) -> out.merge(k == null || "null".equals(k) ? "Chưa xác định" : k, v, Long::sum));
        return out;
    }
}
