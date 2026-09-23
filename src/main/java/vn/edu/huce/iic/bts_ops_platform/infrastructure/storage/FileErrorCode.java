package vn.edu.huce.iic.bts_ops_platform.infrastructure.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ExceptionCode {
    FILE_UPLOAD_EMPTY(400, "FILE_UPLOAD_EMPTY", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_INVALID(400, "FILE_UPLOAD_INVALID", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(500, "FILE_UPLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
    private final int code;
    private final String type;
    private final HttpStatus httpStatus;
    @Override public Integer getCode() { return code; }
    @Override public String getType() { return type; }
    @Override public HttpStatus getHttpStatus() { return httpStatus; }
}
