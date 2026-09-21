package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SanLuongCapNhatHangMucItem {
    private UUID id;
    private UUID hangMucCongViecId;
    private UUID hangMucChiTietId;
    private LocalDate date;
    private BigDecimal unitPrice;
    private String status;
    private BigDecimal amount;
}
