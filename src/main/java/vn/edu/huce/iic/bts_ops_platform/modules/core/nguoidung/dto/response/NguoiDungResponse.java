package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class NguoiDungResponse {

    private UUID id;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private String dienThoai;
    private String chucVu;
    private String noiLamViec;
    private String tenCongTy;
    private String ntNguoiPhuTrach;
    private String ntChucVuPhuTrach;
    private String ntGiamDoc;
    private String ntChucVuGiamDoc;
    private String cdtNguoiGiamSat;
    private String cdtChucVuGiamSat;
    private String cdtGiamDoc;
    private String cdtChucVuGiamDoc;
    private String cdtNguoiPhuTrach;
    private String cdtChucVuPhuTrach;
    private LocalDate ngayHetHan;
    private UUID quyenId;
    private UUID khuVucId;
    private String quyenMa;
    private String quyenTen;
    private boolean hoatDong;
    private Instant ngayDoiMatKhau;
    private boolean batBuocDoiMatKhau;
    private boolean matKhauSapHetHan;
}
