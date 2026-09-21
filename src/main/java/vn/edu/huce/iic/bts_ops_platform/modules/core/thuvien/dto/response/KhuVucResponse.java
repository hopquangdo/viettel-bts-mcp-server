package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class KhuVucResponse {

    private UUID id;
    private String ma;
    private String ten;
    private String moTa;
    private long soTinhThanh;
    private boolean hoatDong;
}
