package vn.edu.huce.iic.bts_ops_platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class LocalUploadResourceConfig implements WebMvcConfigurer {

    private final AppStorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = storageProperties.publicUrlPrefix().replaceAll("/+$", "");
        Path uploadDir = Path.of(storageProperties.localBasePath()).toAbsolutePath().normalize();
        registry.addResourceHandler(prefix + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
