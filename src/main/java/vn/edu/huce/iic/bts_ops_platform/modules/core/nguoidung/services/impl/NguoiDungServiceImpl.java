package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.NameLookupResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.EntityFilter;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.cache.NguoiDungCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.DangKyRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.NguoiDungCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.TaoTaiKhoanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungThamChieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.mapper.NguoiDungMapper;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.repository.NguoiDungRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services.NguoiDungService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.QuyenService;
import vn.edu.huce.iic.bts_ops_platform.common.security.ContractorRoleCodes;
import vn.edu.huce.iic.bts_ops_platform.common.security.InvestorRoleCodes;
import vn.edu.huce.iic.bts_ops_platform.common.security.SensitiveDataMasker;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PasswordLifecycleService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NguoiDungServiceImpl implements NguoiDungService {

    private static final String NAME_LOOKUP_CACHE_KEY = "nha_thau";

    private final NguoiDungRepository nguoiDungRepository;
    private final QuyenService quyenService;
    private final QuyenRepository quyenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NguoiDungMapper nguoiDungMapper;
    private final AppEventContext appEventContext;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;
    private final PasswordLifecycleService passwordLifecycleService;
    private final SensitiveDataMasker sensitiveDataMasker;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> buildContractorNameLookup() {
        Optional<NameLookupResponse> cached = cacheService.get(
                NguoiDungCacheNames.CONTRACTOR_NAME_LOOKUP, NAME_LOOKUP_CACHE_KEY, NameLookupResponse.class);
        if (cached.isPresent()) {
            return cached.get().items();
        }

        Map<UUID, String> lookup = listThamChieu("nha_thau", null, true).stream()
                .filter(item -> item.getId() != null && item.getHoTen() != null && !item.getHoTen().isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        NguoiDungThamChieuResponse::getId,
                        item -> item.getHoTen().trim(),
                        (left, right) -> left));
        cacheService.put(
                NguoiDungCacheNames.CONTRACTOR_NAME_LOOKUP, NAME_LOOKUP_CACHE_KEY, new NameLookupResponse(lookup),
                cacheProperties.nameLookupTtl());
        return lookup;
    }

    /** Vai trò toàn quyền (SUPERADMIN/FULL_ACCESS) không bị giới hạn theo khu vực. */
    private static boolean laToanQuyen() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> vn.edu.huce.iic.bts_ops_platform.common.security.AuthorityPrefix
                        .isFullAccessAuthority(a.getAuthority())
                        || vn.edu.huce.iic.bts_ops_platform.common.security.FullAccessRoleCodes
                                .isFullAccessRoleAuthority(a.getAuthority()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NguoiDungResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID khuVucId) {
        String keyword = EntityFilter.normalizeSearch(search);
        JwtUserPrincipal viewer = SecurityContextHelper.requireCurrentUser();
        UUID scopeKhuVuc = khuVucId != null ? khuVucId : laToanQuyen() ? null : viewer.khuVucId();

        List<NguoiDung> source = includeDeleted ? nguoiDungRepository.findAll() : nguoiDungRepository.findByNgayXoaIsNull();
        return source.stream()
                .filter(user -> scopeKhuVuc == null || scopeKhuVuc.equals(user.getKhuVucId()))
                .filter(user -> EntityFilter.isActive(user, NguoiDung::getHoatDong, activeOnly))
                .filter(user -> matchesSearch(user, keyword))
                .sorted(Comparator.comparing(NguoiDung::getHoTen, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NguoiDungThamChieuResponse> listThamChieu(String nhom, String search, Boolean activeOnly) {
        String normalizedNhom = nhom == null ? "" : nhom.trim().toLowerCase();
        if (!"nha_thau".equals(normalizedNhom) && !"nhan_vien".equals(normalizedNhom)
                && !"chu_dau_tu".equals(normalizedNhom) && !"tat_ca".equals(normalizedNhom)) {
            throw new AppException(AuthErrorCode.YEU_CAU_KHONG_HOP_LE,
                    "Tham số nhom phải là nha_thau, chu_dau_tu, nhan_vien hoặc tat_ca");
        }
        UUID contractorQuyenId = quyenRepository.findByMaIgnoreCase(ContractorRoleCodes.MA)
                .map(Quyen::getId)
                .orElse(null);
        UUID investorQuyenId = quyenRepository.findByMaIgnoreCase(InvestorRoleCodes.MA)
                .map(Quyen::getId)
                .orElse(null);
        String keyword = EntityFilter.normalizeSearch(search);
        return nguoiDungRepository.findByNgayXoaIsNull().stream()
                .filter(user -> EntityFilter.isActive(user, NguoiDung::getHoatDong, activeOnly))
                .filter(user -> switch (normalizedNhom) {
                    case "nha_thau" -> contractorQuyenId != null && contractorQuyenId.equals(user.getQuyenId());
                    case "chu_dau_tu" -> investorQuyenId != null && investorQuyenId.equals(user.getQuyenId());
                    // "tat_ca" = mọi vị trí: phân công trạm cho bất kỳ tài khoản nào (không chỉ role
                    // nhà thầu). ganNhaThau không ràng buộc role nên gán cho vị trí khác vẫn hợp lệ.
                    case "tat_ca" -> true;
                    default -> (contractorQuyenId == null || !contractorQuyenId.equals(user.getQuyenId()))
                            && (investorQuyenId == null || !investorQuyenId.equals(user.getQuyenId()));
                })
                .filter(user -> matchesSearch(user, keyword))
                .sorted(Comparator.comparing(NguoiDung::getHoTen, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(user -> NguoiDungThamChieuResponse.builder()
                        .id(user.getId())
                        .tenDangNhap(user.getTenDangNhap())
                        .hoTen(user.getHoTen())
                        .tenCongTy(user.getTenCongTy())
                        .chucVu(user.getChucVu())
                        .ntNguoiPhuTrach(user.getNtNguoiPhuTrach())
                        .ntChucVuPhuTrach(user.getNtChucVuPhuTrach())
                        .ntGiamDoc(user.getNtGiamDoc())
                        .ntChucVuGiamDoc(user.getNtChucVuGiamDoc())
                        .cdtNguoiGiamSat(user.getCdtNguoiGiamSat())
                        .cdtChucVuGiamSat(user.getCdtChucVuGiamSat())
                        .cdtGiamDoc(user.getCdtGiamDoc())
                        .cdtChucVuGiamDoc(user.getCdtChucVuGiamDoc())
                        .cdtNguoiPhuTrach(user.getCdtNguoiPhuTrach())
                        .cdtChucVuPhuTrach(user.getCdtChucVuPhuTrach())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NguoiDungResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<NguoiDung> findEntityByTenDangNhap(String tenDangNhap) {
        return nguoiDungRepository.findByTenDangNhapIgnoreCase(tenDangNhap)
                .filter(user -> user.getNgayXoa() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<NguoiDung> findActiveEntityById(UUID id) {
        return nguoiDungRepository.findByIdAndNgayXoaIsNull(id);
    }

    @Override
    @Transactional
    public NguoiDungResponse register(DangKyRequest request) {
        validateQuyenExists(request.getQuyenId());
        String tenDangNhap = request.getTenDangNhap().trim();
        ensureUsernameAvailable(tenDangNhap);

        NguoiDung entity = new NguoiDung();
        entity.setQuyenId(request.getQuyenId());
        entity.setKhuVucId(request.getKhuVucId());
        entity.setTenDangNhap(tenDangNhap);
        entity.setHoTen(request.getHoTen().trim());
        entity.setDienThoai(EntityFilter.trimToNull(request.getDienThoai()));
        entity.setEmail(EntityFilter.trimToNull(request.getEmail()));
        entity.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        entity.setNgayDoiMatKhau(Instant.now());
        entity.setHoatDong(true);

        return toResponse(nguoiDungRepository.save(entity));
    }

    @Override
    @Transactional
    public NguoiDungResponse create(TaoTaiKhoanRequest request) {
        JwtUserPrincipal creator = SecurityContextHelper.requireCurrentUser();
        // Vai trò toàn quyền (SUPERADMIN/FULL_ACCESS) tạo được tài khoản ở mọi khu vực; người
        // thường vẫn chỉ tạo trong đúng khu vực được phân công của mình.
        if (!laToanQuyen() && (creator.khuVucId() == null || !creator.khuVucId().equals(request.getKhuVucId()))) {
            throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Chỉ được tạo tài khoản trong khu vực được phân công");
        }
        validateQuyenExists(request.getQuyenId());
        String tenDangNhap = request.getTenDangNhap().trim();
        ensureUsernameAvailable(tenDangNhap);

        NguoiDung entity = nguoiDungMapper.fromTaoTaiKhoanRequest(request);
        entity.setTenDangNhap(tenDangNhap);
        entity.setHoTen(request.getHoTen().trim());
        entity.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        entity.setNgayDoiMatKhau(Instant.now());
        entity.setHoatDong(request.getHoatDong() == null || request.getHoatDong());

        NguoiDung saved = nguoiDungRepository.save(entity);
        appEventContext.audit("TAO_TAI_KHOAN", "Tạo tài khoản " + saved.getTenDangNhap(), null);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public NguoiDungResponse update(UUID id, NguoiDungCapNhatRequest request) {
        NguoiDung entity = findById(id);
        if (request.getQuyenId() != null) {
            validateQuyenExists(request.getQuyenId());
            entity.setQuyenId(request.getQuyenId());
        }
        if (request.getKhuVucId() != null) {
            entity.setKhuVucId(request.getKhuVucId());
        }
        if (StringUtils.hasText(request.getHoTen())) {
            entity.setHoTen(request.getHoTen().trim());
        }
        if (request.getDienThoai() != null) {
            entity.setDienThoai(EntityFilter.trimToNull(request.getDienThoai()));
        }
        if (request.getEmail() != null) {
            entity.setEmail(EntityFilter.trimToNull(request.getEmail()));
        }
        if (request.getChucVu() != null) {
            entity.setChucVu(EntityFilter.trimToNull(request.getChucVu()));
        }
        if (request.getNoiLamViec() != null) {
            entity.setNoiLamViec(EntityFilter.trimToNull(request.getNoiLamViec()));
        }
        if (request.getTenCongTy() != null) {
            entity.setTenCongTy(EntityFilter.trimToNull(request.getTenCongTy()));
        }
        if (request.getNtNguoiPhuTrach() != null) {
            entity.setNtNguoiPhuTrach(EntityFilter.trimToNull(request.getNtNguoiPhuTrach()));
        }
        if (request.getNtChucVuPhuTrach() != null) {
            entity.setNtChucVuPhuTrach(EntityFilter.trimToNull(request.getNtChucVuPhuTrach()));
        }
        if (request.getNtGiamDoc() != null) {
            entity.setNtGiamDoc(EntityFilter.trimToNull(request.getNtGiamDoc()));
        }
        if (request.getNtChucVuGiamDoc() != null) {
            entity.setNtChucVuGiamDoc(EntityFilter.trimToNull(request.getNtChucVuGiamDoc()));
        }
        if (request.getCdtNguoiGiamSat() != null) {
            entity.setCdtNguoiGiamSat(EntityFilter.trimToNull(request.getCdtNguoiGiamSat()));
        }
        if (request.getCdtChucVuGiamSat() != null) {
            entity.setCdtChucVuGiamSat(EntityFilter.trimToNull(request.getCdtChucVuGiamSat()));
        }
        if (request.getCdtGiamDoc() != null) {
            entity.setCdtGiamDoc(EntityFilter.trimToNull(request.getCdtGiamDoc()));
        }
        if (request.getCdtChucVuGiamDoc() != null) {
            entity.setCdtChucVuGiamDoc(EntityFilter.trimToNull(request.getCdtChucVuGiamDoc()));
        }
        if (request.getCdtNguoiPhuTrach() != null) {
            entity.setCdtNguoiPhuTrach(EntityFilter.trimToNull(request.getCdtNguoiPhuTrach()));
        }
        if (request.getCdtChucVuPhuTrach() != null) {
            entity.setCdtChucVuPhuTrach(EntityFilter.trimToNull(request.getCdtChucVuPhuTrach()));
        }
        if (request.getNgayHetHan() != null) {
            entity.setNgayHetHan(request.getNgayHetHan());
        }
        if (request.getHoatDong() != null) {
            entity.setHoatDong(request.getHoatDong());
        }
        boolean doiMatKhau = StringUtils.hasText(request.getMatKhauMoi());
        if (doiMatKhau) {
            passwordLifecycleService.applyNewPassword(entity, request.getMatKhauMoi());
        }
        NguoiDungResponse response = toResponse(nguoiDungRepository.save(entity));
        appEventContext.audit(
                doiMatKhau ? "DOI_MAT_KHAU" : "CAP_NHAT_TAI_KHOAN",
                (doiMatKhau ? "Đổi mật khẩu tài khoản " : "Cập nhật tài khoản ") + entity.getTenDangNhap(),
                null);
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        NguoiDung entity = findById(id);
        entity.setNgayXoa(Instant.now());
        entity.setHoatDong(false);
        nguoiDungRepository.save(entity);
        appEventContext.audit("XOA_TAI_KHOAN", "Xóa tài khoản " + entity.getTenDangNhap(), null);
    }

    private NguoiDung findById(UUID id) {
        return nguoiDungRepository.findByIdAndNgayXoaIsNull(id)
                .orElseThrow(() -> new AppException(AuthErrorCode.NGUOI_DUNG_NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private NguoiDungResponse toResponse(NguoiDung entity) {
        NguoiDungResponse response = nguoiDungMapper.toNguoiDungResponse(entity);
        if (entity.getQuyenId() != null) {
            quyenRepository.findByIdAndNgayXoaIsNull(entity.getQuyenId()).ifPresent(quyen -> {
                response.setQuyenMa(quyen.getMa());
                response.setQuyenTen(quyen.getTen());
            });
        }
        response.setBatBuocDoiMatKhau(passwordLifecycleService.requiresChange(entity));
        response.setMatKhauSapHetHan(passwordLifecycleService.isExpiringSoon(entity));
        return sensitiveDataMasker.maskIfNeeded(response);
    }

    private void validateQuyenExists(UUID quyenId) {
        quyenService.getById(quyenId);
    }

    private void ensureUsernameAvailable(String tenDangNhap) {
        if (nguoiDungRepository.existsByTenDangNhapIgnoreCase(tenDangNhap)) {
            throw new AppException(AuthErrorCode.TEN_DANG_NHAP_DA_TON_TAI, "Tên đăng nhập đã tồn tại");
        }
    }

    private boolean matchesSearch(NguoiDung user, String keyword) {
        return EntityFilter.matchesKeyword(
                keyword,
                EntityFilter.nullToEmpty(user.getTenDangNhap()),
                EntityFilter.nullToEmpty(user.getHoTen()),
                EntityFilter.nullToEmpty(user.getEmail()),
                EntityFilter.nullToEmpty(user.getDienThoai()));
    }
}
