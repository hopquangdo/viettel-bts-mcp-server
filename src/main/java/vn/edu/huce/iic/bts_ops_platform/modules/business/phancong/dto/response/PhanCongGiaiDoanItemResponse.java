package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PhanCongGiaiDoanItemResponse {
    private UUID trangThaiId;
    private String ma;
    private String ten;
    private String mauSac;
    private long tong;
    private long daPhan;
    private long chuaPhan;
    private double tyLePhanCong;
}
