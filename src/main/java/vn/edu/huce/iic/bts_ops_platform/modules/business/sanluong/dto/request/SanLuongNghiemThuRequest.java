package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanLuongNghiemThuRequest {
    /** dat | khong_dat */
    private String ketQua;
    private String lyDo;
}
