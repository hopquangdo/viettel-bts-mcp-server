package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Hỗ trợ resolve "từ khoá -> 1 bản ghi" cho các *Component. Thứ tự tra cứu (rẻ trước):
 * UUID (khoá chính) -> khớp ĐÚNG mã (index sẵn có) -> tìm gần đúng (chỉ khi 2 bước trước không ra).
 * Tìm gần đúng trả tối đa {@link #MAX_UNG_VIEN} ứng viên kèm hạng (1 = khớp đúng, 2 = bắt đầu bằng, 3 = chứa);
 * chỉ chọn khi hạng tốt nhất có DUY NHẤT 1 ứng viên, ngược lại báo mơ hồ thay vì chọn bừa.
 */
public final class ResolveSupport {

    public static final int MAX_UNG_VIEN = 5;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private ResolveSupport() {
    }

    /** Từ khoá đã cắt khoảng trắng và giới hạn độ dài; null nếu rỗng. */
    public static String normalize(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > MAX_KEYWORD_LENGTH ? trimmed.substring(0, MAX_KEYWORD_LENGTH) : trimmed;
    }

    public static String lower(String keyword) {
        return keyword.toLowerCase(Locale.ROOT);
    }

    /** Escape ký tự đại diện của LIKE (dùng cùng {@code ESCAPE ''} trong SQL). */
    public static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * Từ khoá tìm kiếm chữ (danh sách) -> mẫu LIKE "chứa" đã chuẩn hoá (chữ thường, escape ký tự đại diện);
     * null nếu rỗng. SQL dùng cùng {@code ESCAPE '\\'}. Handler không tự bọc {@code %} nữa.
     */
    public static String likePattern(String keyword) {
        String normalized = normalize(keyword);
        return normalized == null ? null : containsPattern(lower(normalized));
    }

    public static String prefixPattern(String lowerKeyword) {
        return escapeLike(lowerKeyword) + "%";
    }

    public static String containsPattern(String lowerKeyword) {
        return "%" + escapeLike(lowerKeyword) + "%";
    }

    /**
     * Chọn 1 ứng viên: danh sách rỗng -> null; hạng tốt nhất có đúng 1 ứng viên -> ứng viên đó;
     * còn lại -> AppException(FILTER_AMBIGUOUS) kèm danh sách để LLM/người dùng chọn lại.
     */
    public static <T> T pick(String entity, String keyword, List<T> candidates,
                             ToIntFunction<T> hang, Function<T, String> label) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        int best = candidates.stream().mapToInt(hang).min().orElse(0);
        List<T> top = candidates.stream().filter(c -> hang.applyAsInt(c) == best).toList();
        if (top.size() == 1) {
            return top.get(0);
        }
        String list = top.stream().map(label).limit(MAX_UNG_VIEN).collect(Collectors.joining("; "));
        throw new AppException(ToolErrorCode.FILTER_AMBIGUOUS,
                "Có nhiều " + entity + " khớp '" + keyword + "': " + list + ". Hãy truyền id hoặc mã chính xác.");
    }

    /**
     * Tham số dạng liệt kê (tab, trạng thái…): chuẩn hoá về chữ thường, null nếu rỗng; giá trị ngoài tập cho phép thì báo
     * FILTER_INVALID kèm danh sách giá trị hợp lệ để LLM sửa lại thay vì trả kết quả rỗng.
     */
    public static String enumValue(String name, String value, String... allowed) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String k = lower(normalized);
        for (String a : allowed) {
            if (a.equals(k)) {
                return a;
            }
        }
        throw new AppException(ToolErrorCode.FILTER_INVALID,
                "Giá trị '" + value + "' không hợp lệ cho " + name + ". Giá trị hợp lệ: " + String.join(", ", allowed) + ".");
    }
}
