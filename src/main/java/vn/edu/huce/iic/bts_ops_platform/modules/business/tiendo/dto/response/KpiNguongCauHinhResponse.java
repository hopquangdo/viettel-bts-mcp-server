package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class KpiNguongCauHinhResponse {

    private UUID id;
    private String ma;
    private String ten;
    private Integer soNgayNguong;
    private Integer uuTien;
    private Boolean hoatDong;
}
