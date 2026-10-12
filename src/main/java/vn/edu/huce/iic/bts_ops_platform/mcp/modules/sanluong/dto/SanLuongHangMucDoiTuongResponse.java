package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import java.util.List;

/** sanluong_hang_muc_doi_tuong: hạng mục đã làm / chưa làm của từng đối tượng cụ thể (tối đa 5). */
public record SanLuongHangMucDoiTuongResponse(
        List<SanLuongHangMucDoiTuongDto> doiTuong) {
}
