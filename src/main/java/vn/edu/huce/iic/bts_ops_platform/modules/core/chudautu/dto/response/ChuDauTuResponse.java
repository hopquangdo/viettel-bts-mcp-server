package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ChuDauTuResponse {
    private UUID id;
    private String ma;
    private String ten;
    private String moTa;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
