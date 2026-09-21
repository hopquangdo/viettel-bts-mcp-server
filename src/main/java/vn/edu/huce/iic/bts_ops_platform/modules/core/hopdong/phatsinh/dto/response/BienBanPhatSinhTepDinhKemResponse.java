package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BienBanPhatSinhTepDinhKemResponse {
    private UUID id;
    private String tenTep;
    private String url;
    private String loaiTaiLieu;
    private String ghiChu;
    private Instant ngayTao;
}
