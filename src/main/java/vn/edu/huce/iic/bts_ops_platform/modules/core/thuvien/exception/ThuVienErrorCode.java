package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum ThuVienErrorCode implements ExceptionCode {

    KHU_VUC_NOT_FOUND(404, "KHU_VUC_NOT_FOUND", HttpStatus.NOT_FOUND),
    KHU_VUC_CODE_EXISTS(409, "KHU_VUC_CODE_EXISTS", HttpStatus.CONFLICT),
    KHU_VUC_HAS_PROVINCES(409, "KHU_VUC_HAS_PROVINCES", HttpStatus.CONFLICT),
    TINH_THANH_NOT_FOUND(404, "TINH_THANH_NOT_FOUND", HttpStatus.NOT_FOUND);

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
