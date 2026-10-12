package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto;

import java.math.BigDecimal;

public record NganHoTramVuotNguongItem(
        String ma,
        String maHopDong,
        String tenHopDong,
        String khuVuc,
        BigDecimal sanLuong,
        BigDecimal binhQuan,
        BigDecimal heSo,
        BigDecimal nguong,
        String tyLe,
        boolean daQuyetToan,
        boolean daKiemTra) {
}