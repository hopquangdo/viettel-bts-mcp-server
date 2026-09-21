package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class DanhMucBienBanResponse {
    private UUID id;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean coMauWord;
    private String giaiDoan;
    private Integer thuTuMacDinh;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
