package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum LuuTruErrorCode implements ExceptionCode {

    HOP_DONG_NOT_FOUND(404, "HOP_DONG_NOT_FOUND", HttpStatus.NOT_FOUND),
    LUU_TRU_NOT_FOUND(404, "LUU_TRU_NOT_FOUND", HttpStatus.NOT_FOUND),
    CHUA_DU_DIEU_KIEN_ARCHIVE(400, "CHUA_DU_DIEU_KIEN_ARCHIVE", HttpStatus.BAD_REQUEST),
    DA_ARCHIVE(409, "DA_ARCHIVE", HttpStatus.CONFLICT),
    CHUA_ARCHIVE(409, "CHUA_ARCHIVE", HttpStatus.CONFLICT),
    HOP_DONG_DANG_ARCHIVE(403, "HOP_DONG_DANG_ARCHIVE", HttpStatus.FORBIDDEN),
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
