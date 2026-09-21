package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum SanLuongErrorCode implements ExceptionCode {

    SAN_LUONG_NOT_FOUND(404, "SAN_LUONG_NOT_FOUND", HttpStatus.NOT_FOUND),
    SAN_LUONG_CODE_EXISTS(409, "SAN_LUONG_CODE_EXISTS", HttpStatus.CONFLICT),
    SAN_LUONG_HOP_DONG_CANCELLED(400, "SAN_LUONG_HOP_DONG_CANCELLED", HttpStatus.BAD_REQUEST),
    SAN_LUONG_DOI_TUONG_COMPLETED(400, "SAN_LUONG_DOI_TUONG_COMPLETED", HttpStatus.BAD_REQUEST),
    SAN_LUONG_DOI_TUONG_CO_VUONG_MAC(400, "SAN_LUONG_DOI_TUONG_CO_VUONG_MAC", HttpStatus.BAD_REQUEST),
    SAN_LUONG_NGHIEM_THU_INVALID(400, "SAN_LUONG_NGHIEM_THU_INVALID", HttpStatus.BAD_REQUEST),
    SAN_LUONG_NGHIEM_THU_LOCKED(400, "SAN_LUONG_NGHIEM_THU_LOCKED", HttpStatus.BAD_REQUEST),
    SAN_LUONG_ROUTE_DESIGN_EXCEEDS_SURVEY(400, "SAN_LUONG_ROUTE_DESIGN_EXCEEDS_SURVEY", HttpStatus.BAD_REQUEST),
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
