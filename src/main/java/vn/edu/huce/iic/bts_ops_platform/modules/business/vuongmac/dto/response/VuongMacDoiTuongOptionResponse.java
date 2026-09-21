package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VuongMacDoiTuongOptionResponse {
    private UUID id;
    private UUID hopDongId;
    private String maTram;
    private String tenDoiTuong;
    private String maHopDong;
    private String tenHopDong;
    private String region;
    private String province;
    private String nhaThau;
}
