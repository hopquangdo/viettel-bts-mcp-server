package vn.edu.huce.iic.bts_ops_platform.modules.core.aiassistant.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum AiAssistantErrorCode implements ExceptionCode {

    INVALID_SESSION_ID(400, "INVALID_SESSION_ID", HttpStatus.BAD_REQUEST),
    CONVERSATION_NOT_FOUND(404, "CONVERSATION_NOT_FOUND", HttpStatus.NOT_FOUND),
    CONVERSATION_FORBIDDEN(403, "CONVERSATION_FORBIDDEN", HttpStatus.FORBIDDEN),
    STREAM_NOT_FOUND(404, "STREAM_NOT_FOUND", HttpStatus.NOT_FOUND),
    CHATBOT_REJECTED(400, "CHATBOT_REJECTED", HttpStatus.BAD_REQUEST),
    CHATBOT_UNAVAILABLE(502, "CHATBOT_UNAVAILABLE", HttpStatus.BAD_GATEWAY),
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
