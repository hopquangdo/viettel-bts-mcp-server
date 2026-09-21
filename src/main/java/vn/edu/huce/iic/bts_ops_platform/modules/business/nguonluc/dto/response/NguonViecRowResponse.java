package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NguonViecRowResponse {
    private String id;
    private int stt;
    private String chuongTrinh;
    private String maHD;
    /** Slug lĩnh vực (tu-van, kiem-dinh, …) — suy từ loại hợp đồng. */
    private String linhVuc;
    private String loaiCV;
    private String nhom;
    private String phapLyStage;
    private double giaTriHD;
    private double sxT1;
    private double sxT2;
    private double sxTong;
    private double dtT1;
    private double dtT2;
    private double dtTong;
    private double slConHD;
    private double dtConSL;
    private double chuaKhaThi;
    /** Số đối tượng / lượng hủy (từ trạng thái HĐ). */
    private double slHuy;
    /** Số vướng mắc đang mở theo HĐ. */
    private double slVuong;
    private String status;
    private String nhaThau;
    private String ghiChuKV;
    private String trungTam;
    private String ngayKyHD;
    private String ngayKetThuc;
    private String ngayCapNhat;
    private int soDoiKS;
    private int soDoiTC;
    private boolean manual;
}
