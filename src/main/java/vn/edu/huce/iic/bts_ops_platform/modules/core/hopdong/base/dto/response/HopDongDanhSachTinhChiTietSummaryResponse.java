package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HopDongDanhSachTinhChiTietSummaryResponse {
    private long tongTram;
    private long huy;
    private long hoanThanh;
    private long daQuyetToan;
    private long chuaQuyetToan;
    private long vuongMac;
    private long hoanThanhDo;
    private long chuaThiCong;
    private double tyLeHoanThanh;
    private BigDecimal giaTriKeHoach = BigDecimal.ZERO;
    private BigDecimal sanLuongThiCong = BigDecimal.ZERO;
}
