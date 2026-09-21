package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.security.SensitiveDataMasker;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.DangNhapRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.DoiMatKhauRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.LamMoiTokenRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.DangNhapResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.mapper.NguoiDungMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.AuthService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenResolverService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.LoginAttemptService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PasswordLifecycleService;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.logging.RequestMdcLoggingFilter;
import org.slf4j.MDC;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services.NguoiDungService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final NguoiDungService nguoiDungService;
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PhanQuyenResolverService phanQuyenResolverService;
    private final NguoiDungMapper authMapper;
    private final LoginAttemptService loginAttemptService;
    private final AppEventContext appEventContext;
    private final PasswordLifecycleService passwordLifecycleService;
    private final SensitiveDataMasker sensitiveDataMasker;

    @Override
    @Transactional(readOnly = true)
    public DangNhapResponse login(DangNhapRequest request) {
        String tenDangNhap = request.getTenDangNhap().trim();
        String ip = MDC.get(RequestMdcLoggingFilter.MDC_CLIENT_IP);
        if (loginAttemptService.isBlocked(tenDangNhap, ip)) {
            long mins = loginAttemptService.remainingLockMinutes(tenDangNhap, ip);
            appEventContext.audit("DANG_NHAP_BI_KHOA", "Đăng nhập bị khóa tạm thời",
                    "Còn " + mins + " phút", Map.of("tenDangNhap", tenDangNhap));
            throw new AppException(AuthErrorCode.DANG_NHAP_THAT_BAI,
                    "Tài khoản tạm khóa do sai nhiều lần, vui lòng thử lại sau " + mins + " phút");
        }
        try {
            NguoiDung nguoiDung = nguoiDungService
                    .findEntityByTenDangNhap(tenDangNhap)
                    .orElseThrow(() -> new AppException(
                            AuthErrorCode.DANG_NHAP_THAT_BAI, "Tên đăng nhập hoặc mật khẩu không đúng"));

            validateAccount(nguoiDung);

            if (!StringUtils.hasText(nguoiDung.getMatKhau())) {
                throw new AppException(
                        AuthErrorCode.CHUA_CO_MAT_KHAU,
                        "Tài khoản chỉ hỗ trợ đăng nhập SSO, chưa thiết lập mật khẩu");
            }

            if (!passwordEncoder.matches(request.getMatKhau(), nguoiDung.getMatKhau())) {
                throw new AppException(
                        AuthErrorCode.DANG_NHAP_THAT_BAI, "Tên đăng nhập hoặc mật khẩu không đúng");
            }

            if (passwordLifecycleService.isExpired(nguoiDung)) {
                nguoiDung.setBatBuocDoiMatKhau(true);
            }

            DangNhapResponse response = buildLoginResponse(nguoiDung);
            loginAttemptService.recordSuccess(tenDangNhap, ip);
            appEventContext.audit("DANG_NHAP", "Đăng nhập thành công", null,
                    Map.of("actorId", nguoiDung.getId(), "actorName", tenDangNhap));
            return response;
        } catch (AppException ex) {
            boolean justLocked = loginAttemptService.recordFailure(tenDangNhap, ip);
            appEventContext.audit(
                    justLocked ? "KHOA_DANG_NHAP" : "DANG_NHAP_THAT_BAI",
                    justLocked ? "Khóa đăng nhập do sai quá số lần cho phép" : "Đăng nhập thất bại",
                    ex.getMessage(), Map.of("tenDangNhap", tenDangNhap));
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DangNhapResponse refreshToken(LamMoiTokenRequest request) {
        var principal = jwtService.verifyToken(request.getRefreshToken(), JwtService.LOAI_REFRESH);
        NguoiDung nguoiDung = findUserById(principal.id());
        validateAccount(nguoiDung);
        return buildLoginResponse(nguoiDung);
    }

    @Override
    @Transactional(readOnly = true)
    public NguoiDungResponse getMe() {
        var principal = SecurityContextHelper.requireCurrentUser();
        return enrichUserResponse(authMapper.toNguoiDungResponse(findUserById(principal.id())));
    }

    @Override
    @Transactional
    public void logout() {
        var principal = SecurityContextHelper.requireCurrentUser();
        appEventContext.audit("DANG_XUAT", "Đăng xuất", null,
                Map.of("actorId", principal.id(), "actorName", principal.tenDangNhap()));
    }

    @Override
    @Transactional
    public void changePassword(DoiMatKhauRequest request) {
        var principal = SecurityContextHelper.requireCurrentUser();
        NguoiDung nguoiDung = findUserById(principal.id());
        passwordLifecycleService.changePassword(nguoiDung, request.getMatKhauCu(), request.getMatKhauMoi());
        nguoiDungRepository.save(nguoiDung);
        appEventContext.audit("DOI_MAT_KHAU", "Đổi mật khẩu tự phục vụ", null,
                Map.of("actorId", nguoiDung.getId(), "actorName", nguoiDung.getTenDangNhap()));
    }

    private DangNhapResponse buildLoginResponse(NguoiDung nguoiDung) {
        boolean batBuoc = passwordLifecycleService.requiresChange(nguoiDung);
        boolean sapHetHan = passwordLifecycleService.isExpiringSoon(nguoiDung);
        NguoiDungResponse userResponse = enrichUserResponse(authMapper.toNguoiDungResponse(nguoiDung));
        userResponse.setBatBuocDoiMatKhau(batBuoc);
        userResponse.setMatKhauSapHetHan(sapHetHan);
        return DangNhapResponse.builder()
            .accessToken(jwtService.createAccessToken(nguoiDung,
                phanQuyenResolverService.resolveTokenScope(nguoiDung.getQuyenId())))
                .refreshToken(jwtService.createRefreshToken(nguoiDung))
                .loaiToken("Bearer")
                .hetHanGiay(jwtService.getAccessTokenExpirationSeconds())
                .nguoiDung(userResponse)
                .batBuocDoiMatKhau(batBuoc)
                .matKhauSapHetHan(sapHetHan)
                .build();
    }

    private NguoiDungResponse enrichUserResponse(NguoiDungResponse response) {
        if (response == null) {
            return null;
        }
        return sensitiveDataMasker.maskIfNeeded(response);
    }

    private void validateAccount(NguoiDung nguoiDung) {
        if (!Boolean.TRUE.equals(nguoiDung.getHoatDong())) {
            throw new AppException(AuthErrorCode.TAI_KHOAN_VO_HIEU, "Tài khoản đã bị vô hiệu hóa");
        }
        if (nguoiDung.getNgayHetHan() != null && nguoiDung.getNgayHetHan().isBefore(LocalDate.now())) {
            throw new AppException(AuthErrorCode.TAI_KHOAN_HET_HAN, "Tài khoản đã hết hạn sử dụng");
        }
    }

    private NguoiDung findUserById(UUID id) {
        return nguoiDungService.findActiveEntityById(id)
                .orElseThrow(() -> new AppException(AuthErrorCode.TOKEN_KHONG_HOP_LE, "Người dùng không tồn tại"));
    }
}
