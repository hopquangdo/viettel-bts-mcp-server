package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class HopDongDanhSachTinhRowResponse {
    private String id;
    private int stt;
    private String tinh;
    /** Tên tỉnh sau sáp nhập (la_tinh_cu=false trong nhóm tinh_thanh). */
    private String tinhMoi;
    private String tinhCu;
    private String nhaThau;
    private long cc;
    private Object huy;
    private Object hoanThanh;
    private Object quyetToan;
    private Object chuaQuyetToan;
    private Object chuaThiCong;
    /** Tư vấn thiết kế — chưa lên sản lượng. */
    private long chuaLamSanLuong;
    /** Tư vấn thiết kế — đã lên sản lượng, chưa hoàn thành. */
    private long dangLamSanLuong;
    /** Tư vấn thiết kế — đã hoàn thành theo ngày HT / bước hoàn thành luồng. */
    private long hoanThanhSanLuong;
    private double tyLeHoanThanh;
    /** Tổng định mức HM theo tỉnh (sum planShare/trạm). */
    private BigDecimal giaTriKeHoach = BigDecimal.ZERO;
    /** Tổng sản lượng thi công hiệu lực theo tỉnh. */
    private BigDecimal sanLuongThiCong = BigDecimal.ZERO;
    private List<HopDongDanhSachTienDoBuocResponse> tienDoTheoTrangThai = new ArrayList<>();
}
