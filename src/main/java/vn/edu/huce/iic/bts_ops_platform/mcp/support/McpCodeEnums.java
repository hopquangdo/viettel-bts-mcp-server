package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.TextUtils;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.McpInvalidParamException;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Parse/liệt kê enum {@link McpCodeEnum}: so khớp không phân biệt hoa thường, dấu, khoảng trắng/gạch ngang/gạch dưới. */
public final class McpCodeEnums {

    private McpCodeEnums() {
    }

    /** "Đã báo" / "da-bao" / " DA_BAO " đều thành "da_bao"; "Tỉnh/thành" thành "tinh_thanh". */
    public static String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return TextUtils.normalizeLower(value).replace('đ', 'd')
                .replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    /**
     * Tìm hằng có {@code code} hoặc alias khớp {@code value}; rỗng hoặc không khớp thì ném {@link McpInvalidParamException}
     * kèm danh sách mã hợp lệ (và nghĩa tiếng Việt) để LLM gọi lại đúng.
     */
    public static <E extends Enum<E> & McpCodeEnum> E of(Class<E> type, String thamSo, String value) {
        String k = chuanHoa(value);
        if (k.isEmpty()) {
            throw new McpInvalidParamException("Thiếu tham số " + thamSo + ". Giá trị hợp lệ: " + moTa(type) + ".");
        }
        for (E e : type.getEnumConstants()) {
            if (chuanHoa(e.code()).equals(k) || e.aliases().stream().map(McpCodeEnums::chuanHoa).anyMatch(k::equals)) {
                return e;
            }
        }
        throw new McpInvalidParamException("Giá trị '" + value + "' không hợp lệ cho " + thamSo
                + ". Giá trị hợp lệ: " + moTa(type) + ".");
    }

    /** "nha_thau, khu_vuc, tinh". */
    public static <E extends Enum<E> & McpCodeEnum> String codes(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(McpCodeEnum::code).collect(Collectors.joining(", "));
    }

    /** "nha_thau (Nhà thầu), khu_vuc (Khu vực)". */
    public static <E extends Enum<E> & McpCodeEnum> String moTa(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(e -> e.code() + " (" + e.label() + ")")
                .collect(Collectors.joining(", "));
    }
}
