package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;

/** sanluong_danh_sach_doi_tuong: bảng từng đối tượng (SL tổng, SL hôm nay, thi công gần nhất, hạng mục...). */
public record SanLuongDanhSachDoiTuongResponse(
        PeriodInfo period,
        PagedResult<SanLuongDoiTuongItem> doiTuong) {
}
