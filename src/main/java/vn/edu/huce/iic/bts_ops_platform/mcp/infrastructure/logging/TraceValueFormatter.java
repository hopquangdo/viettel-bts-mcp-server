package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.logging;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ApiResponse;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Array;

final class TraceValueFormatter {

    private TraceValueFormatter() {}

    static String format(Object value, int maxLength) {
        if (value == null) {
            return "null";
        }
        String summary = summarize(value);
        return truncate(summary, maxLength);
    }

    private static String summarize(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Optional<?> optional) {
            return optional.isEmpty() ? "Optional.empty" : "Optional[" + summarize(optional.get()) + "]";
        }
        if (value instanceof ResponseEntity<?> response) {
            return "ResponseEntity["
                    + response.getStatusCode().value()
                    + ", body="
                    + summarize(response.getBody())
                    + "]";
        }
        if (value instanceof ApiResponse<?> apiResponse) {
            return "ApiResponse[success="
                    + apiResponse.getIsSuccess()
                    + ", data="
                    + summarize(apiResponse.getData())
                    + "]";
        }
        if (value instanceof Page<?> page) {
            return "Page[items=" + page.getNumberOfElements() + ", total=" + page.getTotalElements() + "]";
        }
        if (value instanceof Collection<?> collection) {
            return simpleTypeName(collection) + "(size=" + collection.size() + ")";
        }
        if (value instanceof Map<?, ?> map) {
            return "Map(size=" + map.size() + ")";
        }
        if (value.getClass().isArray()) {
            return value.getClass().getComponentType().getSimpleName() + "[length=" + Array.getLength(value) + "]";
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return value.getClass().getSimpleName() + "(" + String.valueOf(value) + ")";
    }

    private static String simpleTypeName(Collection<?> collection) {
        if (collection.isEmpty()) {
            return "Collection";
        }
        Object first = collection.iterator().next();
        return first == null ? "Collection" : first.getClass().getSimpleName() + "List";
    }

    private static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
