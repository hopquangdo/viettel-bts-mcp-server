package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhanCongTongQuanResponse {
    private long tongTram;
    private long soNhaThau;
    private double tyLeDaPhanNhaThau;
    private double tyLeDaPhanGiaiDoan;
    private long tongDaPhanNhaThau;
    private long tongHoanThanh;
    private long tongVuongMac;
    private long tongTon;
}
