package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1 dòng của vuongmac_xep_hang.
 *
 * @param ma           mã hợp đồng (chỉ khi xếp hạng theo hợp đồng; dùng được cho tham số hopDong của tool khác)
 * @param ten          tên hợp đồng hoặc tên khu vực
 * @param soVuongMacMo số vướng mắc đang mở (chờ xử lý hoặc đang xử lý)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VuongMacXepHangItem(int hang, String ma, String ten, long soVuongMacMo) {
}
