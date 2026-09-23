package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import jakarta.servlet.DispatcherType;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.ApiAccessDeniedHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.ApiAuthenticationEntryPoint;
import vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security.McpApiKeyFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUrlsProperties appUrls;
    private final Environment environment;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;
    private final McpApiKeyFilter mcpApiKeyFilter;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error",

            "/actuator/health",

            // MCP tool server: không qua Spring Security thường; McpApiKeyFilter bắt buộc X-API-Key
            // (fail-closed) và đọc danh tính người dùng từ header X-User-* (McpUserContext).
            "/mcp/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // SSE (SseEmitter) dùng async dispatch — request đầu đã xác thực; dispatch tiếp theo không gửi lại header.
                .securityContext(securityContext -> securityContext.requireExplicitSave(true))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(request -> DispatcherType.ASYNC.equals(request.getDispatcherType()))
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler))
                .addFilterBefore(mcpApiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> configuredOrigins = appUrls.corsAllowedOrigins();
        List<String> origins = configuredOrigins == null
                ? List.of()
                : configuredOrigins.stream()
                        .filter(origin -> origin != null && !origin.isBlank())
                        .collect(Collectors.toList());

        boolean devProfile = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equals("dev") || profile.equals("local"));

        if (devProfile) {
            List<String> patterns = new ArrayList<>(List.of(
                    "http://localhost:*",
                    "http://127.0.0.1:*",
                    "https://*.devtunnels.ms",
                    "https://*.asse.devtunnels.ms"));
            for (String origin : origins) {
                if (!patterns.contains(origin)) {
                    patterns.add(origin);
                }
            }
            configuration.setAllowedOriginPatterns(patterns);
        } else {
            configuration.setAllowedOrigins(origins);
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Cache-Control",
                "X-Requested-With",
                "X-Request-Id"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("X-Request-Id"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
