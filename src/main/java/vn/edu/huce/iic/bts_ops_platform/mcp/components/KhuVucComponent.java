package vn.edu.huce.iic.bts_ops_platform.mcp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.khuvuc.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.khuvuc.KhuVucInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.KhuVucRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.time.Duration;
import java.util.UUID;

/**
 * Lớp exists CHUNG cho "khu vực" dùng bởi mọi tool AI — tránh mỗi handler tự viết lại native query
 * kiểm tra tồn tại theo mã/tên. Chỉ phụ thuộc repository và cache service dùng chung của hệ thống.
 */
@Service
@RequiredArgsConstructor
public class KhuVucComponent {

    private static final String CACHE_NAME = "mcp-khu-vuc-resolve";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final KhuVucInfo EMPTY = new KhuVucInfo(null, null, null);

    private final KhuVucRepository khuVucRepository;
    private final CacheService cacheService;
    private final DataScopeService dataScopeService;

    /**
     * true nếu khu vực tồn tại (khớp mã HOẶC tên, không phân biệt hoa thường).
     */
    public boolean exists(String khuVuc) {
        if (khuVuc == null || khuVuc.isBlank()) {
            return false;
        }
        String trimmed = khuVuc.trim();
        String cacheKey = "exists:" + trimmed.toLowerCase();
        return cacheService.get(CACHE_NAME, cacheKey, Boolean.class)
                .orElseGet(() -> {
                    boolean exists = khuVucRepository.existsByMaOrTen(trimmed);
                    cacheService.put(CACHE_NAME, cacheKey, exists, CACHE_TTL);
                    return exists;
                });
    }

    /**
     * Resolves the area filter of a tool call <em>within the current user's data scope</em>: a user
     * bound to one area always gets that area (even when the filter is empty) and is rejected when
     * asking for another; users with full access get the requested area unchanged. Use this for the
     * "khu vực" argument; {@link #resolve} stays unscoped (it is also used for provinces).
     */
    public KhuVucInfo resolveInScope(String khuVuc) {
        KhuVucInfo requested = resolve(khuVuc);
        UUID allowed = dataScopeService.resolveKhuVucFilter(requested.id());
        if (allowed == null || allowed.equals(requested.id())) {
            return requested;
        }
        return resolve(allowed.toString());
    }

    public KhuVucInfo resolve(String khuVuc) {
        String trimmed = ResolveSupport.normalize(khuVuc);
        if (trimmed == null) {
            return EMPTY;
        }
        KhuVucInfo info = cacheService.get(CACHE_NAME, trimmed.toLowerCase(), KhuVucInfo.class)
            .orElseGet(() -> {
                KhuVucInfo resolved = lookup(trimmed);
                if (resolved != null) {
                    cacheService.put(CACHE_NAME, trimmed.toLowerCase(), resolved, CACHE_TTL);
                }
                return resolved;
            });
        if (info == null) {
            throw new AppException(ToolErrorCode.FILTER_NOT_FOUND,
                    "Không tìm thấy khu vực '" + khuVuc + "'");
        }
        return info;
    }

    /** UUID (khoá chính) -> khớp ĐÚNG mã (index) -> tìm gần đúng theo tên; mơ hồ thì báo lỗi kèm ứng viên. */
    private KhuVucInfo lookup(String keyword) {
        UUID id = UuidHelp.tryParseUuid(keyword);
        if (id != null) {
            return khuVucRepository.findInfoById(id).map(KhuVucComponent::toInfo).orElse(null);
        }
        KhuVucInfoProjection exact = ResolveSupport.pick("khu vực", keyword,
                khuVucRepository.findByMaExact(keyword), p -> 0, KhuVucComponent::label);
        if (exact != null) {
            return toInfo(exact);
        }
        String k = ResolveSupport.lower(keyword);
        KhuVucRepository.UngVien picked = ResolveSupport.pick("khu vực", keyword,
                khuVucRepository.searchGanDung(k, ResolveSupport.prefixPattern(k), ResolveSupport.containsPattern(k)),
                KhuVucRepository.UngVien::getHang, KhuVucComponent::label);
        return picked == null ? null : toInfo(picked);
    }

    private static KhuVucInfo toInfo(KhuVucInfoProjection p) {
        return new KhuVucInfo(p.getId(), p.getMa(), p.getTen());
    }

    private static String label(KhuVucInfoProjection p) {
        return p.getMa() + (p.getTen() != null ? " - " + p.getTen() : "");
    }
}
