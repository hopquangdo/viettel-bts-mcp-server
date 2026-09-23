package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.CommonErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.FORBIDDEN.getType());
        ApiResponse<Void> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.FORBIDDEN)
                .withCode(CommonErrorCode.FORBIDDEN.getCode())
                .withErrors(errors)
                .withMessage("Không đủ quyền truy cập.");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
