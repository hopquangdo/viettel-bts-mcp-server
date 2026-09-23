package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.McpUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.KhuVucRepository;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Ép phạm vi dữ liệu theo khu vực của JWT — ngăn bypass bằng cách gọi thẳng API với
 * {@code khuVucId} / {@code trungTam} khác phạm vi được phân công.
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final KhuVucRepository khuVucRepository;

    public boolean isFullAccess() {
        return SecurityContextHelper.currentUserOrNull() == null || laToanQuyen();
    }

    public Optional<UUID> currentKhuVucId() {
        McpUserPrincipal user = SecurityContextHelper.currentUserOrNull();
        if (user == null || user.khuVucId() == null) {
            return Optional.empty();
        }
        return Optional.of(user.khuVucId());
    }

    /** Trả về khu vực được phép lọc: toàn quyền giữ request, còn lại ép theo JWT. */
    public UUID resolveKhuVucFilter(UUID requestedKhuVucId) {
        if (isFullAccess()) {
            return requestedKhuVucId;
        }
        UUID scope = currentKhuVucId().orElse(null);
        if (scope == null) {
            return requestedKhuVucId;
        }
        if (requestedKhuVucId != null && !scope.equals(requestedKhuVucId)) {
            throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Không có quyền truy cập dữ liệu khu vực này");
        }
        return scope;
    }

    /** Trung tâm (TramTon): toàn quyền giữ filter, còn lại ép tên khu vực của user. */
    public String resolveTrungTamFilter(String requestedTrungTam) {
        if (isFullAccess()) {
            return requestedTrungTam;
        }
        UUID scope = currentKhuVucId().orElse(null);
        if (scope == null) {
            return requestedTrungTam;
        }
        var khuVuc = khuVucRepository.findInfoById(scope).orElse(null);
        String forced = khuVuc == null ? "" : firstNonBlank(khuVuc.getTen(), khuVuc.getMa());
        if (requestedTrungTam != null && !requestedTrungTam.isBlank()
                && !"all".equalsIgnoreCase(requestedTrungTam.trim())) {
            String req = requestedTrungTam.trim().toLowerCase(Locale.ROOT);
            String allowed = forced.toLowerCase(Locale.ROOT);
            if (!allowed.contains(req) && !req.contains(allowed)) {
                throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Không có quyền truy cập dữ liệu trung tâm này");
            }
        }
        return forced;
    }

    public void assertKhuVucAccessible(UUID khuVucId) {
        if (khuVucId == null || isFullAccess()) {
            return;
        }
        currentKhuVucId().ifPresent(scope -> {
            if (!scope.equals(khuVucId)) {
                throw new AppException(AuthErrorCode.KHONG_DU_QUYEN, "Không có quyền truy cập dữ liệu khu vực này");
            }
        });
    }

    public void assertDoiTuongAccessible(HopDongDoiTuongResponse doiTuong) {
        if (doiTuong == null) {
            return;
        }
        assertKhuVucAccessible(doiTuong.getKhuVucId());
    }

    public boolean matchesKhuVuc(UUID khuVucId) {
        if (isFullAccess() || khuVucId == null) {
            return true;
        }
        return currentKhuVucId().map(scope -> scope.equals(khuVucId)).orElse(true);
    }

    public <T> List<T> filterByKhuVuc(Collection<T> items, java.util.function.Function<T, UUID> khuVucExtractor) {
        if (isFullAccess() || items == null || items.isEmpty()) {
            return items == null ? List.of() : List.copyOf(items);
        }
        return items.stream()
                .filter(item -> matchesKhuVuc(khuVucExtractor.apply(item)))
                .toList();
    }

    public long scopedKhuVucCount(long totalActiveKhuVuc) {
        if (isFullAccess()) {
            return totalActiveKhuVuc;
        }
        return currentKhuVucId().isPresent() ? 1L : totalActiveKhuVuc;
    }

    private static boolean laToanQuyen() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> AuthorityPrefix.isFullAccessAuthority(a)
                        || FullAccessRoleCodes.isFullAccessRoleAuthority(a));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
