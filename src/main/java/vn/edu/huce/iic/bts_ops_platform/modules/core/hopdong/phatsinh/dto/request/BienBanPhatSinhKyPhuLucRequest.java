package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BienBanPhatSinhKyPhuLucRequest {
    /** Ngày ký phụ lục HĐ thực tế — bỏ trống lấy ngày hiện tại. */
    private LocalDate ngayKy;
}
