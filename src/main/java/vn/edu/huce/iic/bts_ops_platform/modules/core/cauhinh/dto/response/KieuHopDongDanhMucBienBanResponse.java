package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class KieuHopDongDanhMucBienBanResponse {
    private UUID id;
    private UUID kieuHopDongId;
    private UUID danhMucBienBanId;
    private Integer thuTu;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean coMauWord;
    private String giaiDoan;
}
