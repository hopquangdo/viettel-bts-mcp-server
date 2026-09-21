package vn.edu.huce.iic.bts_ops_platform.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.dto.CommonErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.UNAUTHORIZED.getType());
        ApiResponse<Void> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.UNAUTHORIZED)
                .withCode(CommonErrorCode.UNAUTHORIZED.getCode())
                .withErrors(errors)
                .withMessage("Yêu cầu đăng nhập hoặc token không hợp lệ.");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
