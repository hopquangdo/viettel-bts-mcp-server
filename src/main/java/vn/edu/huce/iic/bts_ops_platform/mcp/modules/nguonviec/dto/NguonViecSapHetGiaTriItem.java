package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1 hợp đồng sắp hết giá trị được giao.
 *
 * @param giaTriConLai  "820 triệu đồng"
 * @param giaTriHopDong "12,5 tỷ đồng"
 * @param tyLeConLai    "6,6%"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NguonViecSapHetGiaTriItem(
        int hang,
        String maHopDong,
        String chuongTrinh,
        String giaTriConLai,
        String giaTriHopDong,
        String tyLeConLai,
        String trungTam) {
}
