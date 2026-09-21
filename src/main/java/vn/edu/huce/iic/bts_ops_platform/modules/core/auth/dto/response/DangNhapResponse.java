package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;

@Data
@Builder
public class DangNhapResponse {

    private String accessToken;
    private String refreshToken;
    private String loaiToken;
    private long hetHanGiay;
    private NguoiDungResponse nguoiDung;
    private boolean batBuocDoiMatKhau;
    private boolean matKhauSapHetHan;
}
