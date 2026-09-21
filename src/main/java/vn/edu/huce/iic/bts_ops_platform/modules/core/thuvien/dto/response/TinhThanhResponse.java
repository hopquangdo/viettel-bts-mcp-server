package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class TinhThanhResponse {

    private UUID id;
    private UUID tinhThanhId;
    private String ma;
    private String ten;
    private UUID khuVucId;
    private String tenKhuVuc;
    private Boolean laTinhCu;
    private boolean hoatDong;
}
