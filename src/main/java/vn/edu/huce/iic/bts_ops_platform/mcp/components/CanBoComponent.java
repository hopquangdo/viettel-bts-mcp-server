package vn.edu.huce.iic.bts_ops_platform.mcp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.canbo.CanBoInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.phancong.NguoiDungRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.PhanCongToolRepository;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CanBoComponent {

    private static final String CACHE_NAME = "mcp-can-bo-resolve";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final CanBoInfo EMPTY = new CanBoInfo(null, null, null);

    private final PhanCongToolRepository repository;
    private final CacheService cacheService;

    public CanBoInfo resolve(String value) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }
        String keyword = value.trim();
        CanBoInfo info = cacheService.get(CACHE_NAME, keyword.toLowerCase(), CanBoInfo.class).orElseGet(() -> {
            UUID id = UuidHelp.tryParseUuid(keyword);
            NguoiDungRow row = id != null
                    ? repository.findNguoiDungById(id)
                    : repository.findNguoiDungByTen(keyword);
            CanBoInfo resolved = row == null ? null : new CanBoInfo(row.getId(), row.getHoTen(), row.getTenDangNhap());
            if (resolved != null) {
                cacheService.put(CACHE_NAME, keyword.toLowerCase(), resolved, CACHE_TTL);
            }
            return resolved;
        });
        if (info == null) {
            throw new AppException(ToolErrorCode.FILTER_NOT_FOUND,
                    "Không tìm thấy cán bộ với id/tên '" + value + "'");
        }
        return info;
    }
}
