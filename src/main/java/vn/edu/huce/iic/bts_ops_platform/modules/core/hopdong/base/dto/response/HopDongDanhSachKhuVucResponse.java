package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HopDongDanhSachKhuVucResponse {
    private String id;
    private String ten;
    private String mauSac;
    private HopDongDanhSachKhuVucMetricsResponse metrics;
    private List<HopDongDanhSachTinhRowResponse> tinhRows = new ArrayList<>();
}
