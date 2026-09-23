package vn.edu.huce.iic.bts_ops_platform.mcp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.tinh.TinhInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.TinhThanhRepository;

import java.time.Duration;
import java.util.UUID;

/**
 * Lớp resolve CHUNG cho "tỉnh/thành" (bảng tinh_thanh) dùng bởi mọi tool AI — tránh mỗi handler tự
 * viết lại native query kiểm tra tồn tại theo mã/tên hoặc mã tỉnh cũ.
 */
@Service
@RequiredArgsConstructor
public class TinhComponent {

    private static final String CACHE_NAME = "mcp-tinh-thanh-resolve";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final TinhInfo EMPTY = new TinhInfo(null, null, null, null);

    private final TinhThanhRepository tinhThanhRepository;
    private final CacheService cache;

    /** true nếu tỉnh/thành tồn tại (khớp mã, tên hoặc mã tỉnh cũ, không phân biệt hoa thường). */
    public boolean exists(String tinh) {
        if (tinh == null || tinh.isBlank()) {
            return false;
        }
        String trimmed = tinh.trim();
        String cacheKey = "exists:" + trimmed.toLowerCase();
        return cache.get(CACHE_NAME, cacheKey, Boolean.class)
            .orElseGet(() -> {
                boolean exists = tinhThanhRepository.existsByMaOrTenOrLegacyMa(trimmed);
                cache.put(CACHE_NAME, cacheKey, exists, CACHE_TTL);
                return exists;
            });
    }

    public TinhInfo resolve(String tinh) {
        if (tinh == null || tinh.isBlank()) {
            return EMPTY;
        }
        String trimmed = tinh.trim();
        TinhInfo info = cache.get(CACHE_NAME, trimmed.toLowerCase(), TinhInfo.class)
            .orElseGet(() -> {
                UUID id = UuidHelp.tryParseUuid(trimmed);

                TinhInfo resolved = (id != null
                        ? tinhThanhRepository.findInfoById(id)
                        : tinhThanhRepository.findInfoByMaOrTen(trimmed))
                    .map(p -> new TinhInfo(p.getId(), p.getMa(), p.getTen(), tinhThanhRepository.findLegacyMaByTinhId(p.getId()).orElse(null)))
                    .orElseGet(() -> tinhThanhRepository.findInfoByLegacyMaOrTen(trimmed)
                        .map(p -> new TinhInfo(p.getId(), p.getMa(), p.getTen(), tinhThanhRepository.findLegacyMaByTinhId(p.getId()).orElse(null)))
                        .orElse(null));

                if (resolved != null) {
                    cache.put(CACHE_NAME, trimmed.toLowerCase(), resolved, CACHE_TTL);
                }
                return resolved;
            });
        if (info == null) {
            throw new AppException(ToolErrorCode.FILTER_NOT_FOUND,
                    "Không tìm thấy tỉnh/thành với id/mã/tên/mã tỉnh cũ '" + tinh + "'");
        }
        return info;
    }

    public UUID resolveId(String tinh) {
        return resolve(tinh).id();
    }
}
