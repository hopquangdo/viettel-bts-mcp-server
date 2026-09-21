package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DanhMucDuLieuTrongYeuResponse {

    private UUID id;
    private String ma;
    private String ten;
    private String bangDuLieu;
    private String cotDuLieu;
    private String mucDo;
    private String chuSoHuu;
    private String moTa;
    private boolean hoatDong;
}
