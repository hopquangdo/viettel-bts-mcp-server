package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 1 khu vực trong bảng phân bổ ngân sách. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoKhuVucCardToolItem {
    private String id;
    private String name;
    private String color;
    private BigDecimal usedPercent;
    private String barColor;
    private BigDecimal giaTriHopDong;
    private BigDecimal thanhTienThiCong;
    private BigDecimal giaTriConLai;
    /** Chênh lệch có dấu = thanhTienThiCong - giaTriHopDong (âm = thiếu, dương = thừa). */
    private BigDecimal chenhLech;
    private BigDecimal trungBinhMoiTram;
    private long soTramCanBang;
    private long soTramThieu;
    private long soTramThua;
}
