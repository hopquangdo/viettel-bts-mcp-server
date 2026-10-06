package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.query;

import java.math.BigDecimal;
import java.util.UUID;

/** Raw contract amounts needed to calculate a progress warning. */
public record HopDongTienDoQuery(
        UUID hopDongId,
        String maHopDong,
        String tenHopDong,
        BigDecimal tongKhoiLuong,
        BigDecimal sanLuongHoanThanh) {
}