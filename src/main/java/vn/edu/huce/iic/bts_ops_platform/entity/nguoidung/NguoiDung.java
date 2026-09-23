package vn.edu.huce.iic.bts_ops_platform.entity.nguoidung;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "nguoi_dung")
public class NguoiDung extends AuditableEntity {

    @Column(name = "quyen_id", nullable = false)
    private UUID quyenId;

    @Column(name = "khu_vuc_id", nullable = false)
    private UUID khuVucId;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 100)
    private String tenDangNhap;

    @JsonIgnore
    @Column(name = "mat_khau", length = 255)
    private String matKhau;

    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @Column(name = "dien_thoai", length = 20)
    private String dienThoai;

    @Column(name = "email")
    private String email;

    @Column(name = "chuc_vu")
    private String chucVu;

    @Column(name = "noi_lam_viec")
    private String noiLamViec;

    /** Tên công ty — dùng khi tài khoản đại diện cho 1 tổ chức (Nhà thầu/Chủ đầu tư), không
     * áp dụng cho tài khoản cá nhân nội bộ. */
    @Column(name = "ten_cong_ty")
    private String tenCongTy;

    /** Các trường dưới đây lưu sẵn thông tin người ký biên bản theo từng vai trò cụ thể (khớp
     * 1-1 với BienBanNguoiKyRequest) để tự động điền khi xuất biên bản — tách bạch khỏi hoTen
     * (vốn là người dùng tài khoản, không nhất thiết là người ký). Chỉ áp dụng cho tài khoản
     * Nhà thầu (nt_*) hoặc Chủ đầu tư (cdt_*), tùy quyền hạn của tài khoản. */
    @Column(name = "nt_nguoi_phu_trach")
    private String ntNguoiPhuTrach;

    @Column(name = "nt_chuc_vu_phu_trach")
    private String ntChucVuPhuTrach;

    @Column(name = "nt_giam_doc")
    private String ntGiamDoc;

    @Column(name = "nt_chuc_vu_giam_doc")
    private String ntChucVuGiamDoc;

    @Column(name = "cdt_nguoi_giam_sat")
    private String cdtNguoiGiamSat;

    @Column(name = "cdt_chuc_vu_giam_sat")
    private String cdtChucVuGiamSat;

    @Column(name = "cdt_giam_doc")
    private String cdtGiamDoc;

    @Column(name = "cdt_chuc_vu_giam_doc")
    private String cdtChucVuGiamDoc;

    @Column(name = "cdt_nguoi_phu_trach")
    private String cdtNguoiPhuTrach;

    @Column(name = "cdt_chuc_vu_phu_trach")
    private String cdtChucVuPhuTrach;

    @Column(name = "sso")
    private String sso;

    @Column(name = "ngay_het_han")
    private LocalDate ngayHetHan;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    @Column(name = "ngay_doi_mat_khau")
    private Instant ngayDoiMatKhau;

    @Column(name = "bat_buoc_doi_mat_khau", nullable = false)
    private Boolean batBuocDoiMatKhau = false;
}
