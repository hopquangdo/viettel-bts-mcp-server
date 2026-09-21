package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record AppStorageProperties(
        String provider,
        String localBasePath,
        String publicUrlPrefix
) {
    public String publicUrlPrefix() {
        if (publicUrlPrefix == null || publicUrlPrefix.isBlank()) {
            return "/uploads";
        }
        return publicUrlPrefix.startsWith("/") ? publicUrlPrefix : "/" + publicUrlPrefix;
    }
}
