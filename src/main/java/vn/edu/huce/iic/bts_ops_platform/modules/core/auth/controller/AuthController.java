package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.DangKyRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.DangNhapRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.DoiMatKhauRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.LamMoiTokenRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.DangNhapResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.AuthService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.services.NguoiDungService;

@RestController
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.XacThuc.TAG, description = ApiEntityCatalog.XacThuc.DESCRIPTION)
public class AuthController {

    private final AuthService authService;
    private final NguoiDungService nguoiDungService;

    @PostMapping("/api/v1/xac-thuc/dang-nhap")
    @SecurityRequirements
    @Operation(
            summary = "① Đăng nhập — lấy accessToken",
            description = """
                    **Public** — không cần Bearer.

                    Sau khi gọi thành công, copy `data.accessToken` trong response, \
                    bấm **Authorize** (góc phải Swagger UI) và dán token.

                    Request mẫu: `{ "tenDangNhap": "admin", "matKhau": "******" }`
                    """)
    public ResponseEntity<ApiResponse<DangNhapResponse>> login(@Valid @RequestBody DangNhapRequest request) {
        return ApiResponse.<DangNhapResponse>build()
                .withData(authService.login(request))
                .withMessage("Đăng nhập thành công")
                .toEntity();
    }

    @PostMapping("/api/v1/xac-thuc/dang-ky")
    @SecurityRequirements
    @Operation(summary = "Đăng ký tài khoản mới", description = "**Public** — không cần Bearer.")
    public ResponseEntity<ApiResponse<NguoiDungResponse>> register(@Valid @RequestBody DangKyRequest request) {
        return ApiResponse.<NguoiDungResponse>build()
                .withData(nguoiDungService.register(request))
                .withMessage("Đăng ký thành công")
                .toEntity();
    }

    @PostMapping("/api/v1/xac-thuc/lam-moi-token")
    @SecurityRequirements
    @Operation(summary = "Làm mới access token bằng refresh token", description = "**Public** — không cần Bearer.")
    public ResponseEntity<ApiResponse<DangNhapResponse>> refreshToken(@Valid @RequestBody LamMoiTokenRequest request) {
        return ApiResponse.<DangNhapResponse>build()
                .withData(authService.refreshToken(request))
                .withMessage("Đã làm mới token")
                .toEntity();
    }

    @GetMapping("/api/v1/xac-thuc/toi")
    @Operation(summary = "Thông tin người dùng đang đăng nhập")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    public ResponseEntity<ApiResponse<NguoiDungResponse>> getMe() {
        return ApiResponse.<NguoiDungResponse>build()
                .withData(authService.getMe())
                .toEntity();
    }

    @PostMapping("/api/v1/xac-thuc/dang-xuat")
    @Operation(summary = "Đăng xuất — ghi audit log thời gian đăng xuất")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ApiResponse.<Void>build()
                .withMessage("Đăng xuất thành công")
                .toEntity();
    }

    @PostMapping("/api/v1/xac-thuc/doi-mat-khau")
    @Operation(summary = "Đổi mật khẩu (tự phục vụ) — kiểm tra lịch sử và chu kỳ")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody DoiMatKhauRequest request) {
        authService.changePassword(request);
        return ApiResponse.<Void>build()
                .withMessage("Đã đổi mật khẩu thành công")
                .toEntity();
    }
}
