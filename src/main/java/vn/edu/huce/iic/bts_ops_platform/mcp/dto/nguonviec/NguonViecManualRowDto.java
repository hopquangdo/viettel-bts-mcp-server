package vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec;

import lombok.Data;

@Data
public class NguonViecManualRowDto {
    private String id;
    private Integer stt;
    private String chuongTrinh;
    private String maHD;
    private String loaiCV;
    private String nhom;
    private String phapLyStage;
    private Double giaTriHD;
    private Double sxT1;
    private Double sxT2;
    private Double sxTong;
    private Double dtT1;
    private Double dtT2;
    private Double dtTong;
    private Double slConHD;
    private Double dtConSL;
    private Double chuaKhaThi;
    private String status;
    private String nhaThau;
    private String ghiChuKV;
    private String trungTam;
    private String ngayKyHD;
    private String ngayKetThuc;
    private String ngayCapNhat;
    private Integer soDoiKS;
    private Integer soDoiTC;
}
