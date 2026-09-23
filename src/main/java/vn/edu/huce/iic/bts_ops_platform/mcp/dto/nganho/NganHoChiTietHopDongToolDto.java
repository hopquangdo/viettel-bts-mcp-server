package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Chi tiết mức tiêu thụ ngân sách 1 hợp đồng — mirror VolumeChiTietResponse (module Volume). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoChiTietHopDongToolDto {

    private UUID hopDongId;
    private String maHopDong;
    private String ten;
    private Long giaTriHd;
    private BigDecimal tongThanhTienThiCong;
    private BigDecimal chenhLech;
    private BigDecimal tyLeSuDung;
    private String trangThai;
    @Builder.Default
    private List<NganHoNhomBreakdownToolItem> nhom = new ArrayList<>();
}
