package vn.edu.huce.iic.bts_ops_platform.mcp.common.dto;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private String message;
    private final ExceptionCode exceptionCode;

    public AppException(ExceptionCode exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public AppException(ExceptionCode exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.message = message;
    }
}
