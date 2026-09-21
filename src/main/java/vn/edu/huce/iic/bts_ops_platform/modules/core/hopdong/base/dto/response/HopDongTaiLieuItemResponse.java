package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HopDongTaiLieuItemResponse {
    private UUID id;
    private UUID tepDinhKemId;
    private String loaiTaiLieu;
    private String ghiChu;
    private String tenTep;
    private String tenTepGoc;
    private String url;
    private Instant ngayTao;
}
