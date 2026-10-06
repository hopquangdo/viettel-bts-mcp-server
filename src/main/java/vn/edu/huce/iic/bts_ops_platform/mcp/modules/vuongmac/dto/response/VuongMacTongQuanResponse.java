package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** AI-facing overview of VuongMac counts. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VuongMacTongQuanResponse(
        long tongSo,
        long choXuLy,
        long dangXuLy,
        long daXuLy,
        long tuChoi,
        long dangVuong,
        long quaHan30Ngay,
        Long daXuLyTrongKy,
        Map<String, Long> theoLoai,
        long soHopDongCoVuongMacMo,
        long soDoiTuongCoVuongMacMo) {
}