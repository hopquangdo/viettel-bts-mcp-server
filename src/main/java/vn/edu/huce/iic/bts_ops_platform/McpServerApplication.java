package vn.edu.huce.iic.bts_ops_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import vn.edu.huce.iic.bts_ops_platform.config.AsyncEventConfig;
import vn.edu.huce.iic.bts_ops_platform.config.KafkaTopicConfig;
import vn.edu.huce.iic.bts_ops_platform.config.LocalUploadResourceConfig;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiPermissionCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import vn.edu.huce.iic.bts_ops_platform.common.cache.RedisCacheService;
import vn.edu.huce.iic.bts_ops_platform.config.JacksonConfig;
import vn.edu.huce.iic.bts_ops_platform.config.McpToolConfig;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.McpApiKeyFilter;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl.PhanQuyenResolverServiceImpl;

@SpringBootApplication
// Quét toàn bộ package gốc (như backend) nhưng bỏ các cấu hình mcp-server không dùng: Kafka, upload file, OpenAPI, sự kiện bất đồng bộ.
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                KafkaTopicConfig.class, AsyncEventConfig.class, LocalUploadResourceConfig.class,
                OpenApiConfig.class, OpenApiPermissionCustomizer.class}),
        // Producer/consumer sự kiện (Kafka/nội bộ) của backend: mcp-server chỉ đọc dữ liệu, không phát/nhận sự kiện.
        @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "vn[.]edu[.]huce[.]iic[.]bts_ops_platform[.].*[.]event[.].*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Kafka.*")
})
@EnableJpaRepositories(basePackages = "vn.edu.huce.iic.bts_ops_platform")
@EntityScan(basePackages = "vn.edu.huce.iic.bts_ops_platform")
@ConfigurationPropertiesScan("vn.edu.huce.iic.bts_ops_platform.config")
@Import({McpToolConfig.class, McpApiKeyFilter.class, JwtService.class, PhanQuyenResolverServiceImpl.class,
        RedisCacheService.class, JacksonConfig.class})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}