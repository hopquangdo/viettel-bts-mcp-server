package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gom danh sách strategy (Spring inject) thành EnumMap theo chiều, báo lỗi ngay lúc khởi động nếu 1 chiều có 2 strategy
 * hoặc thiếu strategy cho 1 chiều — tránh lỗi chỉ lộ ra khi LLM gọi đúng chiều đó.
 */
public final class McpStrategyRegistries {

    private McpStrategyRegistries() {
    }

    public static <E extends Enum<E>, S> Map<E, S> gom(Class<E> type, List<S> strategies, Function<S, E> chieuCua) {
        EnumMap<E, S> map = new EnumMap<>(type);
        for (S s : strategies) {
            E chieu = chieuCua.apply(s);
            S cu = map.putIfAbsent(chieu, s);
            if (cu != null) {
                throw new IllegalStateException("Trùng strategy cho " + type.getSimpleName() + "." + chieu + ": "
                        + cu.getClass().getName() + " và " + s.getClass().getName());
            }
        }
        List<E> thieu = Arrays.stream(type.getEnumConstants()).filter(e -> !map.containsKey(e)).toList();
        if (!thieu.isEmpty()) {
            throw new IllegalStateException("Thiếu strategy cho " + type.getSimpleName() + ": "
                    + thieu.stream().map(Enum::name).collect(Collectors.joining(", ")));
        }
        return map;
    }
}
