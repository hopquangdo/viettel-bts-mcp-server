package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 1 dòng của danh sách đối tượng theo bộ lọc, khớp các cột của bảng trên trang Sản lượng. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongDoiTuongItem {
    /** Giá trị khoá chính của đối tượng (mã trạm, mã tuyến…); rỗng thì lấy loại đối tượng. */
    private String maDoiTuong;
    private String loaiDoiTuong;
    private String hopDong;
    private String nhaThau;
    private String khuVuc;
    /** Mã tỉnh của đối tượng; {@code laTinhCu} = true nghĩa là mã tỉnh cũ (trước sáp nhập). */
    private String tinh;
    private String tenTinh;
    private boolean laTinhCu;
    /** Giá trị sản lượng hoàn thành trong khoảng ngày lọc. */
    private BigDecimal giaTriTrongKy;
    /** SL tổng: lũy kế mọi bản ghi tính tiền (hoàn thành, khảo sát, thiết kế), không phụ thuộc khoảng ngày. */
    private BigDecimal slTong;
    /** SL hôm nay: bản ghi tính tiền có ngày thực hiện là hôm nay. */
    private BigDecimal slHomNay;
    /** Ngày thi công gần nhất (ngày thực hiện lớn nhất của bản ghi hoàn thành), yyyy-MM-dd. */
    private String thiCongGanNhat;
    private int soHangMucHoanThanh;
    private int tongHangMuc;
    private boolean coVuongMacMo;
}
