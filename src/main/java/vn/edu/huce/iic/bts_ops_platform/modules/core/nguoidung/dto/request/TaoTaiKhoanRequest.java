package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.edu.huce.iic.bts_ops_platform.common.security.PasswordPolicy;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TaoTaiKhoanRequest {

    @NotBlank
    @Size(max = 100)
    private String tenDangNhap;

    @NotBlank
    @Size(min = 6, max = 100)
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String matKhau;

    @NotBlank
    @Size(max = 255)
    private String hoTen;

    @Size(max = 20)
    private String dienThoai;

    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String chucVu;

    @Size(max = 255)
    private String noiLamViec;

    @Size(max = 255)
    private String tenCongTy;

    @Size(max = 255)
    private String ntNguoiPhuTrach;

    @Size(max = 255)
    private String ntChucVuPhuTrach;

    @Size(max = 255)
    private String ntGiamDoc;

    @Size(max = 255)
    private String ntChucVuGiamDoc;

    @Size(max = 255)
    private String cdtNguoiGiamSat;

    @Size(max = 255)
    private String cdtChucVuGiamSat;

    @Size(max = 255)
    private String cdtGiamDoc;

    @Size(max = 255)
    private String cdtChucVuGiamDoc;

    @Size(max = 255)
    private String cdtNguoiPhuTrach;

    @Size(max = 255)
    private String cdtChucVuPhuTrach;

    @NotNull
    private UUID quyenId;

    @NotNull
    private UUID khuVucId;

    private LocalDate ngayHetHan;

    private Boolean hoatDong = true;
}
