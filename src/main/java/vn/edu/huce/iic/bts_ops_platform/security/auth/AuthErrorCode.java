package vn.edu.huce.iic.bts_ops_platform.security.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ExceptionCode {
    TOKEN_KHONG_HOP_LE(401, "TOKEN_KHONG_HOP_LE", HttpStatus.UNAUTHORIZED),
    TOKEN_HET_HAN(401, "TOKEN_HET_HAN", HttpStatus.UNAUTHORIZED),
    KHONG_DU_QUYEN(403, "KHONG_DU_QUYEN", HttpStatus.FORBIDDEN);

    private final int code;
    private final String type;
    private final HttpStatus httpStatus;

    @Override public Integer getCode() { return code; }
    @Override public String getType() { return type; }
    @Override public HttpStatus getHttpStatus() { return httpStatus; }
}
