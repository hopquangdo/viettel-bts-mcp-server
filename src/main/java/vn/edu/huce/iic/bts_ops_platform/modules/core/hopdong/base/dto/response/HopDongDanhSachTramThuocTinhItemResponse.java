package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class HopDongDanhSachTramThuocTinhItemResponse {
    private UUID thuocTinhId;
    private String tenThuocTinh;
    private String giaTri;
    private Integer thuTu;
    private Boolean laKhoaChinh;
}
