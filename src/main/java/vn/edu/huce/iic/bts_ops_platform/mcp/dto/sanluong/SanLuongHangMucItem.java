package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 1 hạng mục của đối tượng: đã làm (bản ghi mới nhất là done) hay chưa. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanLuongHangMucItem {
    private String nhom;
    private String ma;
    private String ten;
    /** done | survey | design… theo bản ghi mới nhất; "chua_co_ban_ghi" nếu chưa từng ghi nhận. */
    private String trangThai;
    /** true khi bản ghi mới nhất là done (cùng định nghĩa "hoàn thành" của tiến độ). */
    private boolean daLam;
    private BigDecimal donGia;
    private BigDecimal khoiLuong;
    /** Giá trị (khối lượng × đơn giá) của bản ghi mới nhất. */
    private BigDecimal giaTri;
    /** Ngày thực hiện của bản ghi mới nhất, yyyy-MM-dd. */
    private String ngayThucHien;
    /** dat | khong_dat | null (chưa nghiệm thu). */
    private String ketQuaNghiemThu;
    private String lyDoKhongDat;
}
