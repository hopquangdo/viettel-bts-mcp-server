package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum HangMucErrorCode implements ExceptionCode {

    HANG_MUC_NHOM_NOT_FOUND(404, "HANG_MUC_NHOM_NOT_FOUND", HttpStatus.NOT_FOUND),
    HANG_MUC_NHOM_CODE_EXISTS(409, "HANG_MUC_NHOM_CODE_EXISTS", HttpStatus.CONFLICT),
    HANG_MUC_CHI_TIET_NOT_FOUND(404, "HANG_MUC_CHI_TIET_NOT_FOUND", HttpStatus.NOT_FOUND),
    HANG_MUC_CHI_TIET_CODE_EXISTS(409, "HANG_MUC_CHI_TIET_CODE_EXISTS", HttpStatus.CONFLICT),
    HANG_MUC_CONG_VIEC_NOT_FOUND(404, "HANG_MUC_CONG_VIEC_NOT_FOUND", HttpStatus.NOT_FOUND),
    HANG_MUC_CONG_VIEC_CODE_EXISTS(409, "HANG_MUC_CONG_VIEC_CODE_EXISTS", HttpStatus.CONFLICT),
    HANG_MUC_SAO_CHEP_INVALID(400, "HANG_MUC_SAO_CHEP_INVALID", HttpStatus.BAD_REQUEST),
    HANG_MUC_SAO_CHEP_NGUON_RONG(400, "HANG_MUC_SAO_CHEP_NGUON_RONG", HttpStatus.BAD_REQUEST),
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
