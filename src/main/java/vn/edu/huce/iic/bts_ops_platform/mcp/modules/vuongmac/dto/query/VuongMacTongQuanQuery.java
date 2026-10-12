package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.query;

import java.util.Map;

/** Raw typed aggregate values for calculating the VuongMac overview response. */
public record VuongMacTongQuanQuery(
        long tongSo,
        long choXuLy,
        long dangXuLy,
        long daXuLy,
        long tuChoi,
        long quaHan30Ngay,
        Long daXuLyTrongKy,
        long soHopDongCoVuongMacMo,
        long soDoiTuongCoVuongMacMo,
        Map<String, Long> soTheoKieu) {
}