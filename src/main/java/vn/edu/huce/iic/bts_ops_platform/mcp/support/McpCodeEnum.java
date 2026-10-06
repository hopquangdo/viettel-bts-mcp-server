package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import java.util.List;

/**
 * Enum tham số tool dạng mã: {@code code} ASCII snake_case LLM truyền vào, {@code label} tiếng Việt có dấu để trả ra,
 * {@code aliases} các cách viết cũ/đồng nghĩa vẫn chấp nhận. Parse bằng {@link McpCodeEnums#of}.
 */
public interface McpCodeEnum {

    String code();

    String label();

    List<String> aliases();
}
