package vn.edu.huce.iic.bts_ops_platform.dto.phancong;

import lombok.Data;

import java.util.Map;

/** Số liệu tổng hợp phân công cho MCP tool phancong_tongquan. */
@Data
public class PhanCongToolThongKe {
    /** Số đối tượng ĐÃ phân nhà thầu (nha_thau_id không rỗng) - khớp tongDaPhanNhaThau của trang Phân công. */
    private long tongPhanCong;
    /** Tổng số đối tượng đang hoạt động trong phạm vi lọc - khớp tongTram của trang Phân công. */
    private long tongHoatDong;
    private long tongDaPhanNhaThau;
    private long soNhaThau;
    private long tongHoanThanh;
    private long tongVuongMac;
    private long tongTon;
    /** Tỷ lệ đối tượng đã phân nhà thầu (%), làm tròn 2 số. */
    private double tyLeDaPhanNhaThau;
    private Map<String, Long> theoKhuVuc;
    private Map<String, Long> theoTinhThanh;
}
