package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class HopDongDanhSachThuocTinhColumnResponse {
    private UUID thuocTinhId;
    private String ten;
    private Integer thuTu;
    private Boolean laKhoaChinh;
}
