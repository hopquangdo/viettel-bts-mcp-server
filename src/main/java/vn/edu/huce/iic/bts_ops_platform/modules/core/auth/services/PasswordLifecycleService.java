package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.config.AppPasswordProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.MatKhauLichSu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.MatKhauLichSuRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordLifecycleService {

    private final MatKhauLichSuRepository matKhauLichSuRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppPasswordProperties passwordProperties;

    public boolean isExpired(NguoiDung user) {
        if (passwordProperties.expiryDays() <= 0 || user.getNgayDoiMatKhau() == null) {
            return false;
        }
        Instant deadline = user.getNgayDoiMatKhau().plus(passwordProperties.expiryDays(), ChronoUnit.DAYS);
        return Instant.now().isAfter(deadline);
    }

    public boolean isExpiringSoon(NguoiDung user) {
        if (passwordProperties.expiryDays() <= 0 || user.getNgayDoiMatKhau() == null) {
            return false;
        }
        int warningDays = Math.max(passwordProperties.warningDays(), 0);
        Instant warningAt = user.getNgayDoiMatKhau()
                .plus(passwordProperties.expiryDays() - warningDays, ChronoUnit.DAYS);
        return !isExpired(user) && Instant.now().isAfter(warningAt);
    }

    public boolean requiresChange(NguoiDung user) {
        return Boolean.TRUE.equals(user.getBatBuocDoiMatKhau()) || isExpired(user);
    }

    @Transactional
    public void changePassword(NguoiDung user, String matKhauCu, String matKhauMoi) {
        if (!StringUtils.hasText(user.getMatKhau())) {
            throw new AppException(AuthErrorCode.CHUA_CO_MAT_KHAU, "Tài khoản chưa thiết lập mật khẩu");
        }
        if (!passwordEncoder.matches(matKhauCu, user.getMatKhau())) {
            throw new AppException(AuthErrorCode.MAT_KHAU_CU_KHONG_DUNG, "Mật khẩu hiện tại không đúng");
        }
        applyNewPassword(user, matKhauMoi);
    }

    @Transactional
    public void applyNewPassword(NguoiDung user, String matKhauMoi) {
        assertNotReused(user, matKhauMoi);
        saveHistory(user);
        user.setMatKhau(passwordEncoder.encode(matKhauMoi));
        user.setNgayDoiMatKhau(Instant.now());
        user.setBatBuocDoiMatKhau(false);
    }

    private void assertNotReused(NguoiDung user, String plainPassword) {
        if (passwordEncoder.matches(plainPassword, user.getMatKhau())) {
            throw new AppException(AuthErrorCode.MAT_KHAU_DA_SU_DUNG, "Mật khẩu mới không được trùng mật khẩu hiện tại");
        }
        int limit = Math.max(passwordProperties.historyCount(), 0);
        if (limit == 0) {
            return;
        }
        List<MatKhauLichSu> history = matKhauLichSuRepository.findTop5ByNguoiDungIdOrderByNgayTaoDesc(user.getId());
        int checked = 0;
        for (MatKhauLichSu entry : history) {
            if (checked >= limit) {
                break;
            }
            if (passwordEncoder.matches(plainPassword, entry.getMatKhauHash())) {
                throw new AppException(
                        AuthErrorCode.MAT_KHAU_DA_SU_DUNG,
                        "Mật khẩu mới không được trùng " + limit + " mật khẩu gần nhất");
            }
            checked++;
        }
    }

    private void saveHistory(NguoiDung user) {
        if (!StringUtils.hasText(user.getMatKhau())) {
            return;
        }
        MatKhauLichSu entry = new MatKhauLichSu();
        entry.setNguoiDungId(user.getId());
        entry.setMatKhauHash(user.getMatKhau());
        matKhauLichSuRepository.save(entry);
    }
}
