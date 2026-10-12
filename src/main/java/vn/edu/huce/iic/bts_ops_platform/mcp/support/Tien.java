package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu trường số tiền VNĐ trong DTO trả cho tool: khi trả ra JSON tự định dạng "1,25 tỷ đồng" ({@link McpFormat#tien(Number)}),
 * trong code vẫn giữ kiểu số để tính toán. Dùng cho mọi kiểu Number (BigDecimal, Long, long, Double...).
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = TienJsonSerializer.class)
public @interface Tien {
}
