package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.util.UUID;
import java.time.Instant;

@Data
public class HopDongTepDinhKemResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID tepDinhKemId;
    private String loaiTaiLieu;
    private String ghiChu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
