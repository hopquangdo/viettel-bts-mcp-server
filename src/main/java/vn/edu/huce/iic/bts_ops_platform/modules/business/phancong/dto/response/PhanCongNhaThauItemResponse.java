package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PhanCongNhaThauItemResponse {
    private UUID nhaThauId;
    private String ten;
    private String tenDangNhap;
    private String maVung;
    private String mauSac;
    private long soTram;
    private long hoanThanh;
    private long vuongMac;
    private long ton;
    private BigDecimal sanLuong;
    private double tienDoPercent;
}
