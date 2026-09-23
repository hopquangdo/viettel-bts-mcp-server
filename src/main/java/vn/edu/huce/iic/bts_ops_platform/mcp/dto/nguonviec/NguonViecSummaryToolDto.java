package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec;

import lombok.Builder;
import lombok.Data;

/** Tổng quan giá trị/sản xuất/doanh thu nguồn việc — mirror NguonViecSummaryResponse (module NguonLuc). */
@Data
@Builder
public class NguonViecSummaryToolDto {
    private double giaTriHD;
    private double sxTong;
    private double dtTong;
    private double dtConSL;
    private double chuaKhaThi;
    private double slConHD;
}
