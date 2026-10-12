package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;

/** sanluong_nghiem_thu: đạt / không đạt (kèm lý do) / chờ nghiệm thu; lọc ngày theo ngày nghiệm thu. */
public record SanLuongNghiemThuResponse(
        PeriodInfo period,
        SanLuongNghiemThuDto nghiemThu) {
}
