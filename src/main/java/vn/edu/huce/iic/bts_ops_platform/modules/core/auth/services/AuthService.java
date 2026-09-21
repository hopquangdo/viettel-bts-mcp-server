package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.DangNhapRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.DoiMatKhauRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.LamMoiTokenRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.DangNhapResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;

public interface AuthService {

    DangNhapResponse login(DangNhapRequest request);

    DangNhapResponse refreshToken(LamMoiTokenRequest request);

    NguoiDungResponse getMe();

    void logout();

    void changePassword(DoiMatKhauRequest request);
}
