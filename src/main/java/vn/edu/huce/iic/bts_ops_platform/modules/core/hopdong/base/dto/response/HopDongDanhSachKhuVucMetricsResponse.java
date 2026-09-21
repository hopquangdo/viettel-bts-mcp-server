package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class HopDongDanhSachKhuVucMetricsResponse {
    private long soDoiTuong;
    private long hoanThanh;
    private long daQuyetToan;
    private long chuaBatDau;
    /** Tư vấn thiết kế — chưa lên sản lượng. */
    private long chuaLamSanLuong;
    /** Tư vấn thiết kế — đã lên sản lượng, chưa hoàn thành. */
    private long dangLamSanLuong;
    /** Tư vấn thiết kế — đã hoàn thành theo ngày HT / bước hoàn thành luồng. */
    private long hoanThanhSanLuong;
    private double tyLeHoanThanh;
    private String tyLeMauSac;
    private String tienDoMauSac;
    private Map<String, Long> tienDoTheoTrangThai = new LinkedHashMap<>();
    private List<HopDongDanhSachTienDoBuocResponse> tienDoBuoc = new ArrayList<>();
}
