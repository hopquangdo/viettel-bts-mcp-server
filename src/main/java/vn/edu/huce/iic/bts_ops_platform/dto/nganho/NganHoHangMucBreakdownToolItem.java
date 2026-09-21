package vn.edu.huce.iic.bts_ops_platform.dto.nganho;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** 1 hạng mục trong breakdown nhóm của 1 hợp đồng — mirror VolumeHangMucBreakdown (module Volume). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganHoHangMucBreakdownToolItem {
    private UUID id;
    private String ma;
    private String ten;
    private BigDecimal khoiLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}
