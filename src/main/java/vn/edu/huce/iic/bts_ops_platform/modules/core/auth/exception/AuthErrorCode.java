package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ExceptionCode {

    DANG_NHAP_THAT_BAI(401, "DANG_NHAP_THAT_BAI", HttpStatus.UNAUTHORIZED),
    TAI_KHOAN_VO_HIEU(403, "TAI_KHOAN_VO_HIEU", HttpStatus.FORBIDDEN),
    TAI_KHOAN_HET_HAN(403, "TAI_KHOAN_HET_HAN", HttpStatus.FORBIDDEN),
    CHUA_CO_MAT_KHAU(401, "CHUA_CO_MAT_KHAU", HttpStatus.UNAUTHORIZED),
    TOKEN_KHONG_HOP_LE(401, "TOKEN_KHONG_HOP_LE", HttpStatus.UNAUTHORIZED),
    TOKEN_HET_HAN(401, "TOKEN_HET_HAN", HttpStatus.UNAUTHORIZED),
    TEN_DANG_NHAP_DA_TON_TAI(409, "TEN_DANG_NHAP_DA_TON_TAI", HttpStatus.CONFLICT),
    NGUOI_DUNG_NOT_FOUND(404, "NGUOI_DUNG_NOT_FOUND", HttpStatus.NOT_FOUND),
    QUYEN_KHONG_TON_TAI(404, "QUYEN_KHONG_TON_TAI", HttpStatus.NOT_FOUND),
    KHONG_DU_QUYEN(403, "KHONG_DU_QUYEN", HttpStatus.FORBIDDEN),
    YEU_CAU_KHONG_HOP_LE(400, "YEU_CAU_KHONG_HOP_LE", HttpStatus.BAD_REQUEST),
    MAT_KHAU_CU_KHONG_DUNG(400, "MAT_KHAU_CU_KHONG_DUNG", HttpStatus.BAD_REQUEST),
    MAT_KHAU_DA_SU_DUNG(400, "MAT_KHAU_DA_SU_DUNG", HttpStatus.BAD_REQUEST),
    BAT_BUOC_DOI_MAT_KHAU(403, "BAT_BUOC_DOI_MAT_KHAU", HttpStatus.FORBIDDEN);

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
