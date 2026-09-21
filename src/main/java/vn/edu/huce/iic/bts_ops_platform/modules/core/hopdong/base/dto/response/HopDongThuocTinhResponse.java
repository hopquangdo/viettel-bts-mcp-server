package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HopDongThuocTinhResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID thuocTinhHopDongId;
    private String tenThuocTinh;
    private String giaTri;
    private Short thuTu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
