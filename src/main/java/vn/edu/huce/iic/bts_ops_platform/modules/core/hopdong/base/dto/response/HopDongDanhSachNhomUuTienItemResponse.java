package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class HopDongDanhSachNhomUuTienItemResponse {
    private UUID id;
    private String ma;
    private String ten;
    private String mauSac;
    private Integer keHoach;
    private Integer hoanThanh;
    private Integer conLai;
    private BigDecimal phanTram;
    private Integer soNhan;
}
