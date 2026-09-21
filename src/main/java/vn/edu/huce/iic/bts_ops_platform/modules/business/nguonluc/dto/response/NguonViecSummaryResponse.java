package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NguonViecSummaryResponse {
    private double giaTriHD;
    private double sxTong;
    private double dtTong;
    private double dtConSL;
    private double chuaKhaThi;
    private double slConHD;
}
