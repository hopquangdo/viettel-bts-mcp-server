package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum ChuDauTuErrorCode implements ExceptionCode {

    CHU_DAU_TU_NOT_FOUND(404, "CHU_DAU_TU_NOT_FOUND", HttpStatus.NOT_FOUND),
    CHU_DAU_TU_CODE_EXISTS(409, "CHU_DAU_TU_CODE_EXISTS", HttpStatus.CONFLICT),
    ;

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
