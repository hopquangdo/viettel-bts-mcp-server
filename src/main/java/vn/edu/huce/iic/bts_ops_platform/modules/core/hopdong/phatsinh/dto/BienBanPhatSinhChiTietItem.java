package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** 1 dòng khối lượng trong biên bản phát sinh — serialize vào chi_tiet_json. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BienBanPhatSinhChiTietItem {
    private String tenDauViec;
    private BigDecimal khoiLuong;
    private String donVi;
    private String ghiChu;
}
