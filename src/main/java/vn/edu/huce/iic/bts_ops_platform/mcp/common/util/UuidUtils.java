package vn.edu.huce.iic.bts_ops_platform.mcp.common.util;

import java.util.UUID;

/** Tiện ích thao tác UUID dùng chung. */
public final class UuidUtils {

    private UuidUtils() {
    }

    /** Parse UUID an toàn: trả về null nếu chuỗi rỗng/không hợp lệ. */
    public static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
