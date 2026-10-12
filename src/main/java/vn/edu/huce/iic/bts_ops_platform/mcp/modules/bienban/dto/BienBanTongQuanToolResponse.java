package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * bienban_tong_quan: số biên bản theo trạng thái duyệt và theo loại.
 *
 * @param theoLoai số biên bản theo loại (nhãn tiếng Việt)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BienBanTongQuanToolResponse(
        long tongSo,
        long choDuyet,
        long daDuyet,
        long tuChoi,
        Map<String, Long> theoLoai) {

    public static BienBanTongQuanToolResponse of(BienBanTongQuanToolItem tq) {
        return new BienBanTongQuanToolResponse(tq.getTotal(), tq.getChoDuyet(), tq.getDaDuyet(), tq.getTuChoi(), tq.getCountsByLoai());
    }
}
