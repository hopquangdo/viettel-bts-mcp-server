package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class KieuHopDongLuongTrangThaiResponse {

    private UUID kieuHopDongId;
    private UUID loaiHopDongId;
    private UUID luongTrangThaiId;
    private String luongTrangThaiTen;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
