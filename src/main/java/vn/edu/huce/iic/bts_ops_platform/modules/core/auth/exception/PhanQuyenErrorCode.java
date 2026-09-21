package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum PhanQuyenErrorCode implements ExceptionCode {

    QUYEN_NOT_FOUND(404, "QUYEN_NOT_FOUND", HttpStatus.NOT_FOUND),
    QUYEN_CODE_EXISTS(409, "QUYEN_CODE_EXISTS", HttpStatus.CONFLICT),
    QUYEN_HAN_NOT_FOUND(404, "QUYEN_HAN_NOT_FOUND", HttpStatus.NOT_FOUND),
    QUYEN_HAN_CODE_EXISTS(409, "QUYEN_HAN_CODE_EXISTS", HttpStatus.CONFLICT),
    PHAN_QUYEN_NOT_FOUND(404, "PHAN_QUYEN_NOT_FOUND", HttpStatus.NOT_FOUND),
    PHAN_QUYEN_CODE_EXISTS(409, "PHAN_QUYEN_CODE_EXISTS", HttpStatus.CONFLICT);

    private final int code;
    private final String type;
    private final HttpStatus httpStatus;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
