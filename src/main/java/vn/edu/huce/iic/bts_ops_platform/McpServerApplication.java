package vn.edu.huce.iic.bts_ops_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import vn.edu.huce.iic.bts_ops_platform.config.McpToolConfig;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.McpApiKeyFilter;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl.PhanQuyenResolverServiceImpl;

@SpringBootApplication(scanBasePackages = {
    "vn.edu.huce.iic.bts_ops_platform.components",
    "vn.edu.huce.iic.bts_ops_platform.controller",
    "vn.edu.huce.iic.bts_ops_platform.definitions",
    "vn.edu.huce.iic.bts_ops_platform.handler",
    "vn.edu.huce.iic.bts_ops_platform.metrics",
    "vn.edu.huce.iic.bts_ops_platform.repository",
    "vn.edu.huce.iic.bts_ops_platform.security",
    "vn.edu.huce.iic.bts_ops_platform.support"
})
@EnableJpaRepositories(basePackages = {
        "vn.edu.huce.iic.bts_ops_platform.repository",
        "vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository"
})
@EntityScan(basePackages = "vn.edu.huce.iic.bts_ops_platform")
@EnableConfigurationProperties
@Import({McpToolConfig.class, McpApiKeyFilter.class, JwtService.class, PhanQuyenResolverServiceImpl.class})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}