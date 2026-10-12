package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacXepHangItem;

import java.util.List;

/** vuongmac_xep_hang: 1 bảng xếp hạng số vướng mắc đang mở theo đúng 1 chiều. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VuongMacXepHangResponse(
        String xepHangTheo,
        String tieuChi,
        List<VuongMacXepHangItem> danhSach,
        String ghiChu) {
}
