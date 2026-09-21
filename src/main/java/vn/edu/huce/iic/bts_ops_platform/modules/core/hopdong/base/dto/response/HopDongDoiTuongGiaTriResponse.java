package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HopDongDoiTuongGiaTriResponse {
    private UUID id;
    private UUID hopDongDoiTuongId;
    private UUID thuocTinhId;
    private String tenThuocTinh;
    private String giaTri;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
