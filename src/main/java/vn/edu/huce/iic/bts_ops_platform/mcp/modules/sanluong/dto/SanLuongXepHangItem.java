package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1 dòng xếp hạng của sanluong_xep_hang. {@code id} giữ lại vì các tool lọc (nhaThau, khuVuc, hopDong, doiTuong...)
 * nhận ID để gọi tiếp chính xác.
 *
 * @param giaTri        "1,25 tỷ đồng"
 * @param tyLeHoanThanh "82,5%"
 * @param soDoiTuong    số đối tượng trong nhóm (không có khi xếp hạng theo đối tượng)
 * @param soVuongMac    số đối tượng đang có vướng mắc mở
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanLuongXepHangItem(
        Integer hang,
        String id,
        String ten,
        String giaTri,
        String tyLeHoanThanh,
        Integer soDoiTuong,
        Integer soVuongMac) {
}
