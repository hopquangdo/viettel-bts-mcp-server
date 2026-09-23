package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password")
public record AppPasswordProperties(
        /** Số mật khẩu gần nhất không được tái sử dụng. */
        int historyCount,
        /** Số ngày bắt buộc đổi mật khẩu (0 = tắt). */
        int expiryDays,
        /** Cảnh báo trước khi hết hạn (ngày). */
        int warningDays) {}
