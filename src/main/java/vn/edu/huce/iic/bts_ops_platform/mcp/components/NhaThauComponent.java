package vn.edu.huce.iic.bts_ops_platform.mcp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.NhaThauRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NhaThauComponent {

    private static final String CACHE_NAME = "mcp-nha-thau-resolve";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final NhaThauInfo EMPTY = new NhaThauInfo(null, null);

    private final NhaThauRepository nhaThauRepository;
    private final CacheService cacheService;

    /**
     * Nhận 1 tham số duy nhất — UUID, tên đăng nhập (mã) hoặc họ tên. Thứ tự: UUID (khoá chính) -> khớp ĐÚNG tên đăng nhập
     * (unique index) -> tìm gần đúng theo họ tên. Tên mơ hồ thì báo lỗi kèm ứng viên thay vì chọn bừa.
     */
    public NhaThauInfo resolve(String nhaThau) {
        String trimmed = ResolveSupport.normalize(nhaThau);
        if (trimmed == null) {
            return EMPTY;
        }
        String cacheKey = trimmed.toLowerCase();
        NhaThauInfo info = cacheService.get(CACHE_NAME, cacheKey, NhaThauInfo.class).orElseGet(() -> {
            NhaThauInfo resolved = lookup(trimmed);
            if (resolved != null) {
                cacheService.put(CACHE_NAME, cacheKey, resolved, CACHE_TTL);
            }
            return resolved;
        });
        if (info == null) {
            throw new AppException(ToolErrorCode.FILTER_NOT_FOUND,
                    "Không tìm thấy nhà thầu với id/tên '" + nhaThau + "'");
        }
        return info;
    }

    private NhaThauInfo lookup(String keyword) {
        UUID directId = UuidHelp.tryParseUuid(keyword);
        if (directId != null) {
            return nhaThauRepository.findInfoById(directId).map(NhaThauComponent::toInfo).orElse(null);
        }
        NhaThauInfoProjection exact = ResolveSupport.pick("nhà thầu", keyword,
                nhaThauRepository.findByTenDangNhapExact(keyword), p -> 0, p -> p.getHoTen());
        if (exact != null) {
            return toInfo(exact);
        }
        String k = ResolveSupport.lower(keyword);
        NhaThauRepository.UngVien picked = ResolveSupport.pick("nhà thầu", keyword,
                nhaThauRepository.searchGanDung(k, ResolveSupport.prefixPattern(k), ResolveSupport.containsPattern(k)),
                NhaThauRepository.UngVien::getHang, p -> p.getHoTen());
        return picked == null ? null : toInfo(picked);
    }

    private static NhaThauInfo toInfo(NhaThauInfoProjection p) {
        return new NhaThauInfo(p.getId(), p.getHoTen());
    }

    public UUID resolveId(String nhaThau) {
        return resolve(nhaThau).id();
    }
}
