# MCP Server

Service Spring Boot tach cho MCP tools. Các package MCP nằm trực tiếp dưới
`vn.edu.huce.iic.bts_ops_platform` (`components`, `dto`, `handler`, `repository`,
`security`, `support`...), không còn namespace ngoài `mcp`.

Service vẫn dùng tạm entity/domain/security source từ `services/business-api` trong
giai đoạn chuyển tiếp.

## Build

```powershell
..\business-api\gradlew.bat -p . compileJava
..\business-api\gradlew.bat -p . bootJar
```

MCP endpoint: `POST/GET /mcp`. Bat buoc `X-API-Key` va token nguoi dung trong
`Authorization: Bearer ...`. Khong chay Flyway; schema do business-api so huu.