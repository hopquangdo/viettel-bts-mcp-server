package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.openapi.ApiEntityCatalog;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI btsOpsOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("BTS Ops Platform API")
                        .description(INFO_DESCRIPTION)
                        .version("v1"))
                .tags(ApiEntityCatalog.orderedTags())
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                .name(BEARER_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Dán accessToken nhận từ **POST /api/v1/xac-thuc/dang-nhap** (field `data.accessToken`). Không cần prefix \"Bearer \" — Swagger tự thêm.")));
        openAPI.addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
        openAPI.addExtension("x-tagGroups", ApiEntityCatalog.tagGroups());
        return openAPI;
    }

    private static final String INFO_DESCRIPTION = """
            REST API Quản lý Hợp đồng BTS V2 (VTK). API được nhóm theo **entity** (bảng dữ liệu).

            ### Đăng nhập trên Swagger (3 bước)
            1. Mở nhóm **Xác thực & Người dùng** → tag **Xác thực** → gọi **POST /api/v1/xac-thuc/dang-nhap**
            2. Copy giá trị `data.accessToken` trong response
            3. Bấm nút **Authorize** (góc phải) → dán token → **Authorize**

            Endpoint public (không cần token): đăng nhập, đăng ký, làm mới token, health.

            ### Phân quyền
            - Mỗi nhóm API entity gắn mã quyền hạn (`quyen_han.ma`), ví dụ `QUAN_LY_HOP_DONG`.
            - Quyền được gán cho vai trò (`quyen`) qua API **Gán quyền hạn** (`phan_quyen_quyen_han`).
            - JWT mang `quyenId`; server nạp danh sách `HAN_*` vào SecurityContext khi xác thực.
            - Endpoint có `@RequiresPermission` sẽ trả **403** nếu thiếu quyền — mã quyền hiển thị trong mô tả từng operation.

            ### Nhóm entity trên Swagger UI
            1. **Xác thực & Người dùng** — JWT, CRUD tài khoản
            2. **Phân quyền** — Quyền, quyền hạn, gán quyền
            3. **Thư viện** — Khu vực, tỉnh/thành
            4. **Cấu hình hợp đồng** — Loại/kiểu HĐ, thuộc tính, trạng thái
            5. **Hợp đồng** — HĐ và các entity con (đối tượng, tệp, nhóm ưu tiên…)
            6. **Hạng mục & Sản lượng** — Hạng mục thi công, sản lượng
            7. **Nghiệp vụ & Tệp** — Vướng mắc, tệp đính kèm""";
}
