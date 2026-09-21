package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 1 nhóm chi phí trong breakdown chi tiết hợp đồng — mirror VolumeNhomBreakdown (module Volume). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoNhomBreakdownToolItem {
    private UUID id;
    private String ma;
    private String ten;
    private BigDecimal thanhTien;
    @Builder.Default
    private List<NganHoHangMucBreakdownToolItem> hangMuc = new ArrayList<>();
}
