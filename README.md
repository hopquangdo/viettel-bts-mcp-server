# MCP Server

Service Spring Boot tach cho MCP tools. Các package MCP nằm trực tiếp dưới
`vn.edu.huce.iic.bts_ops_platform` (`components`, `dto`, `handler`, `repository`,
`security`, `support`...), không còn namespace ngoài `mcp`.

MCP giữ bản sao các entity/domain/security source cần thiết để build độc lập.

## Build

```powershell
.\gradlew.bat compileJava
.\gradlew.bat bootJar
```

MCP endpoint: `POST/GET /mcp`. Bat buoc `X-API-Key` va token nguoi dung trong
`Authorization: Bearer ...`. Khong chay Flyway; schema do business-api so huu.