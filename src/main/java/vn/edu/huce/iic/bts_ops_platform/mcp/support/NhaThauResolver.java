package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import java.util.UUID;
import java.util.function.Function;

/**
 * Chuẩn hóa logic "xác định nhà thầu" mà gần như mọi tool handler đều cần: ưu tiên id được truyền
 * thẳng, nếu không có thì tra theo tên gần đúng (LLM hay chỉ biết tên, không biết UUID). Mỗi
 * handler tự truyền vào cách tra cứu (repository/query riêng của nó) vì không phải domain nào
 * cũng dùng chung 1 repository cho việc này.
 */
public final class NhaThauResolver {

    private NhaThauResolver() {
    }

    /** nhaThauId nếu đã có sẵn, không thì tra theo tenNhaThau qua {@code lookupByTen}. */
    public static UUID resolveId(UUID nhaThauId, String tenNhaThau, Function<String, UUID> lookupByTen) {
        if (nhaThauId != null) {
            return nhaThauId;
        }
        if (tenNhaThau != null && !tenNhaThau.isBlank()) {
            return lookupByTen.apply(tenNhaThau);
        }
        return null;
    }

    /** tenNhaThau nếu đã có sẵn hoặc tra được theo nhaThauId qua {@code lookupTenById}. */
    public static String resolveTen(UUID nhaThauId, String tenNhaThau, Function<UUID, String> lookupTenById) {
        if (nhaThauId != null) {
            String hoTen = lookupTenById.apply(nhaThauId);
            return hoTen != null ? hoTen : null;
        }
        if (tenNhaThau != null && !tenNhaThau.isBlank()) {
            return tenNhaThau;
        }
        return null;
    }

    /**
     * true nếu chuỗi truyền vào là 1 UUID hợp lệ — dùng để phân biệt id/tên trong tham số gộp
     * "nhaThau" (xem {@code mcp.services.NhaThauService.resolveId}, nguồn resolve chính thức nay
     * đã chuyển sang đó để có cache; hàm này chỉ còn phục vụ soạn message lỗi rõ ràng theo id/tên).
     */
    public static boolean looksLikeUuid(String value) {
        return value != null && tryParseUuid(value.trim()) != null;
    }

    private static UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
