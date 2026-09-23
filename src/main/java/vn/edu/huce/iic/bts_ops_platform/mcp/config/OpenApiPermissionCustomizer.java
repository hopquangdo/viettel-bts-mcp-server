package vn.edu.huce.iic.bts_ops_platform.mcp.config;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.security.RequiresPermission;

@Component
public class OpenApiPermissionCustomizer implements OperationCustomizer {

    private static final String PERMISSION_PREFIX = "**Quyền hạn bắt buộc:** `";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        RequiresPermission permission = resolvePermission(handlerMethod);
        if (permission == null) {
            return operation;
        }
        String note = PERMISSION_PREFIX + permission.value() + "`";
        String existing = operation.getDescription();
        if (existing == null || existing.isBlank()) {
            operation.setDescription(note);
        } else if (!existing.contains(permission.value())) {
            operation.setDescription(existing + "\n\n" + note);
        }
        return operation;
    }

    private RequiresPermission resolvePermission(HandlerMethod handlerMethod) {
        RequiresPermission methodLevel = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        return handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
    }
}
