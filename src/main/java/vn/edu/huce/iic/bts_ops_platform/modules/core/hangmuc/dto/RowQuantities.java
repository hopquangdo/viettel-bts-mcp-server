package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto;

import java.math.BigDecimal;

public record RowQuantities(
        BigDecimal donGia,
        BigDecimal khoiLuong,
        String donVi,
        String viTri,
        Short thuTu) {
}
