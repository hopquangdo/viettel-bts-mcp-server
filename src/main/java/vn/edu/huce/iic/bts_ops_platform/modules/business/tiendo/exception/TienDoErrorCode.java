package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum TienDoErrorCode implements ExceptionCode {

    DOI_TUONG_NOT_FOUND(404, "DOI_TUONG_NOT_FOUND", HttpStatus.NOT_FOUND),
    HOP_DONG_NOT_FOUND(404, "HOP_DONG_NOT_FOUND", HttpStatus.NOT_FOUND),
    KE_HOACH_NOT_FOUND(404, "KE_HOACH_NOT_FOUND", HttpStatus.NOT_FOUND),
    VAT_TU_NOT_FOUND(404, "VAT_TU_NOT_FOUND", HttpStatus.NOT_FOUND),
    KPI_NGUONG_NOT_FOUND(404, "KPI_NGUONG_NOT_FOUND", HttpStatus.NOT_FOUND),
    KHONG_DU_QUYEN_ADMIN(403, "KHONG_DU_QUYEN_ADMIN", HttpStatus.FORBIDDEN),
    KE_HOACH_TRANG_THAI_INVALID(400, "KE_HOACH_TRANG_THAI_INVALID", HttpStatus.BAD_REQUEST),
    CHECKLIST_MUC_NOT_FOUND(404, "CHECKLIST_MUC_NOT_FOUND", HttpStatus.NOT_FOUND),
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
