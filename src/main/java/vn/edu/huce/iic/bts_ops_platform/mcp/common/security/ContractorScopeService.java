package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.phancong.PhanCong;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.PhanCongRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.QuyenRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractorScopeService {

    private static final String CACHE_NAME = "contractor-scope";

    private final PhanCongRepository phanCongRepository;
    private final QuyenRepository quyenRepository;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;

    @Transactional(readOnly = true)
    public boolean isCurrentUserContractor() {
        JwtUserPrincipal user = SecurityContextHelper.currentUserOrNull();
        if (user == null) {
            return false;
        }
        if (user.getAuthorities() != null && !user.getAuthorities().isEmpty()) {
            return user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(ContractorRoleCodes::isContractorRoleAuthority);
        }
        if (user.quyenId() == null) {
            return false;
        }
        return quyenRepository.findByIdAndNgayXoaIsNull(user.quyenId())
                .map(quyen -> ContractorRoleCodes.isContractor(quyen.getMa()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<ContractorScope> currentScope() {
        if (!isCurrentUserContractor()) {
            return Optional.empty();
        }
        JwtUserPrincipal user = SecurityContextHelper.requireCurrentUser();
        Optional<ContractorScope> cached = cacheService.get(CACHE_NAME, user.id().toString(), ContractorScope.class);
        if (cached.isPresent()) {
            return cached;
        }

        List<PhanCong> assignments = phanCongRepository.findActiveForContractor(
                user.id(),
                user.hoTen(),
                user.tenDangNhap());
        Set<UUID> hopDongIds = new HashSet<>();
        Set<UUID> hopDongDoiTuongIds = new HashSet<>();
        for (PhanCong assignment : assignments) {
            if (assignment.getHopDongId() != null) {
                hopDongIds.add(assignment.getHopDongId());
            }
            if (assignment.getHopDongDoiTuongId() != null) {
                hopDongDoiTuongIds.add(assignment.getHopDongDoiTuongId());
            }
        }
        ContractorScope scope = new ContractorScope(hopDongIds, hopDongDoiTuongIds);
        cacheService.put(CACHE_NAME, user.id().toString(), scope, cacheProperties.contractorScopeTtl());
        return Optional.of(scope);
    }

    public void evictScopeCache(UUID userId) {
        if (userId != null) {
            cacheService.evict(CACHE_NAME, userId.toString());
        }
    }

    public void assertHopDongAccessible(UUID hopDongId) {
        if (hopDongId == null) {
            return;
        }
        currentScope().ifPresent(scope -> {
            if (!scope.hopDongIds().contains(hopDongId)) {
                throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Không có quyền truy cập hợp đồng này");
            }
        });
    }

    /** Nhà thầu chỉ nhập liệu — không tự phê duyệt/nghiệm thu/duyệt chất lượng. */
    public void assertNotContractorForQualityAudit() {
        if (isCurrentUserContractor()) {
            throw new AppException(
                    AuthErrorCode.KHONG_DU_QUYEN,
                    "Nhà thầu không được thực hiện kiểm duyệt hoặc phê duyệt chất lượng");
        }
    }

    public void assertHopDongDoiTuongAccessible(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        currentScope().ifPresent(scope -> {
            if (!scope.hopDongDoiTuongIds().contains(hopDongDoiTuongId)) {
                throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Không có quyền truy cập trạm này");
            }
        });
    }

    public static boolean matchesContractorLabel(String nhaThau, String hoTen, String tenDangNhap) {
        if (nhaThau == null || nhaThau.isBlank()) {
            return false;
        }
        String normalized = nhaThau.trim().toLowerCase(Locale.ROOT);
        if (hoTen != null && normalized.equals(hoTen.trim().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return tenDangNhap != null && normalized.equals(tenDangNhap.trim().toLowerCase(Locale.ROOT));
    }
}
