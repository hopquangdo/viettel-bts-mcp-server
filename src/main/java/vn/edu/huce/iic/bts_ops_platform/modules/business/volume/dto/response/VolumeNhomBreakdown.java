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
public class VolumeNhomBreakdown {
    private UUID id;
    private String ma;
    private String ten;
    private BigDecimal thanhTien;
    @Builder.Default
    private List<VolumeHangMucBreakdown> hangMuc = new ArrayList<>();
}
