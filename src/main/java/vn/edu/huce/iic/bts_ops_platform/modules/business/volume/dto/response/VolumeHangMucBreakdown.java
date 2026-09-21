package vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeHangMucBreakdown {
    private UUID id;
    private String ma;
    private String ten;
    private BigDecimal khoiLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}
