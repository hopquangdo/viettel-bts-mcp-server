package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ContractWorkItem(
        UUID catalogKey,
        UUID hangMucCongViecId,
        UUID hangMucChiTietId,
        String name,
        String code,
        BigDecimal unitPrice,
        boolean chiTietLeaf) {
}
