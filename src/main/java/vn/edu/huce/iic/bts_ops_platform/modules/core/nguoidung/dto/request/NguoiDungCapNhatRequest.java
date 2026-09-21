package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.edu.huce.iic.bts_ops_platform.common.security.PasswordPolicy;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class NguoiDungCapNhatRequest {

    private UUID quyenId;
    private UUID khuVucId;

    @Size(max = 255)
    private String hoTen;

    @Size(max = 20)
    private String dienThoai;

    @Email
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

    private LocalDate ngayHetHan;
    private Boolean hoatDong;

    @Size(min = 6, max = 100)
    @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
    private String matKhauMoi;
}
