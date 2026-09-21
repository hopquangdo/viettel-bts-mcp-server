package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeChiTietResponse {

    private UUID hopDongId;
    private String maHopDong;
    private String ten;
    private Long giaTriHd;
    private BigDecimal tongThanhTienThiCong;
    private BigDecimal chenhLech;
    private BigDecimal tyLeSuDung;
    private String trangThai;
    @Builder.Default
    private List<VolumeNhomBreakdown> nhom = new ArrayList<>();
}
