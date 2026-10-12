package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;

/** sanluong_doi_tuong_chua_co: đối tượng chưa ghi nhận sản lượng hoàn thành nào trong khoảng ngày. */
public record SanLuongDoiTuongChuaCoResponse(
        PeriodInfo period,
        PagedResult<SanLuongDoiTuongChuaCoItem> doiTuong) {
}
