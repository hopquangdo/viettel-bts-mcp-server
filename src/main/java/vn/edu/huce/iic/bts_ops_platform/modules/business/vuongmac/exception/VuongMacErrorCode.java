package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum VuongMacErrorCode implements ExceptionCode {

    VUONG_MAC_NOT_FOUND(404, "VUONG_MAC_NOT_FOUND", HttpStatus.NOT_FOUND),
    VUONG_MAC_CODE_EXISTS(409, "VUONG_MAC_CODE_EXISTS", HttpStatus.CONFLICT),
    VUONG_MAC_INVALID(400, "VUONG_MAC_INVALID", HttpStatus.BAD_REQUEST),
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
