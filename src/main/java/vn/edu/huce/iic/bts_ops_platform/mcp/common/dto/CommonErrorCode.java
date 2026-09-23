package vn.edu.huce.iic.bts_ops_platform.mcp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ExceptionCode {
    BAD_REQUEST(400, "BAD_REQUEST"),
    VALIDATION_ERROR(400, "VALIDATION_ERROR"),
    UNAUTHORIZED(401, "UNAUTHORIZED"),
    FORBIDDEN(403, "FORBIDDEN"),
    PAYLOAD_TOO_LARGE(413, "PAYLOAD_TOO_LARGE"),
    INTERNAL_ERROR(500, "INTERNAL_ERROR");

    private final Integer code;
    private final String type;
}
