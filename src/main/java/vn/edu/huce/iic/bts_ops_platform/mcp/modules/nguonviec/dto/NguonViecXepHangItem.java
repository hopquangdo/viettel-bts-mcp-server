package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1 dòng của nguonviec_xep_hang. Theo rủi ro: diemRuiRo; theo trung tâm: sanLuong, giaTriHopDong ("1,25 tỷ đồng"), soHopDong.
 *
 * @param ten "mã HĐ - chương trình" (rủi ro) hoặc tên trung tâm
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NguonViecXepHangItem(
        int hang,
        String ten,
        Double diemRuiRo,
        String sanLuong,
        String giaTriHopDong,
        Integer soHopDong) {
}
