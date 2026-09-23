package vn.edu.huce.iic.bts_ops_platform.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.cache.RedisCacheService;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.JacksonConfig;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.McpToolConfig;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.McpApiKeyFilter;

@SpringBootApplication
// Quét toàn bộ package gốc (như backend) nhưng bỏ các cấu hình mcp-server không dùng: Kafka, upload file, OpenAPI, sự kiện bất đồng bộ.
@EnableJpaRepositories(basePackages = "vn.edu.huce.iic.bts_ops_platform")
@EntityScan(basePackages = "vn.edu.huce.iic.bts_ops_platform")
@ConfigurationPropertiesScan("vn.edu.huce.iic.bts_ops_platform.mcp.config")
@Import({McpToolConfig.class, McpApiKeyFilter.class,
        RedisCacheService.class, JacksonConfig.class})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}