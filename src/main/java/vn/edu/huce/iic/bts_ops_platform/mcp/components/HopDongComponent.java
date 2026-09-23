package vn.edu.huce.iic.bts_ops_platform.mcp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.time.Duration;
import java.util.UUID;

/**
 * Resolve hợp đồng từ id/mã/tên. Thứ tự (rẻ trước): UUID (khoá chính) -> khớp ĐÚNG mã (index sẵn có) -> tìm gần đúng theo tên.
 * Tìm gần đúng mơ hồ thì báo lỗi kèm ứng viên, không chọn bừa (xem {@link ResolveSupport}).
 */
@Component
@RequiredArgsConstructor
public class HopDongComponent {

    private static final String CACHE_NAME = "mcp-hop-dong-resolve";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final HopDongInfo EMPTY = new HopDongInfo(null, null, null);

    private final HopDongRepository repository;
    private final CacheService cacheService;

    public HopDongInfo resolve(String value) {
        String keyword = ResolveSupport.normalize(value);
        if (keyword == null) {
            return EMPTY;
        }
        String cacheKey = keyword.toLowerCase();
        HopDongInfo info = cacheService.get(CACHE_NAME, cacheKey, HopDongInfo.class).orElseGet(() -> {
            HopDongInfo resolved = lookup(keyword);
            if (resolved != null) {
                cacheService.put(CACHE_NAME, cacheKey, resolved, CACHE_TTL);
            }
            return resolved;
        });
        if (info == null) {
            throw new AppException(ToolErrorCode.FILTER_NOT_FOUND,
                    "Không tìm thấy hợp đồng với id/mã/tên '" + value + "'");
        }
        return info;
    }

    private HopDongInfo lookup(String keyword) {
        UUID id = UuidHelp.tryParseUuid(keyword);
        if (id != null) {
            return repository.findInfoById(id).map(HopDongComponent::toInfo).orElse(null);
        }
        HopDongInfoProjection exact = ResolveSupport.pick("hợp đồng", keyword,
                repository.findByMaExact(keyword), p -> 0, HopDongComponent::label);
        if (exact != null) {
            return toInfo(exact);
        }
        String k = ResolveSupport.lower(keyword);
        HopDongRepository.UngVien picked = ResolveSupport.pick("hợp đồng", keyword,
                repository.searchGanDung(k, ResolveSupport.prefixPattern(k), ResolveSupport.containsPattern(k)),
                HopDongRepository.UngVien::getHang, HopDongComponent::label);
        return picked == null ? null : toInfo(picked);
    }

    private static HopDongInfo toInfo(HopDongInfoProjection p) {
        return new HopDongInfo(p.getId(), p.getMa(), p.getTen());
    }

    private static String label(HopDongInfoProjection p) {
        return p.getMa() + (p.getTen() != null ? " - " + p.getTen() : "");
    }
}
