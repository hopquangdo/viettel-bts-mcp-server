package vn.edu.huce.iic.bts_ops_platform.security;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.security.auth.AuthErrorCode;

/**
 * Runs a tool as the end user identified by the MCP request, so {@code DataScopeService} and
 * {@code @RequiresPermission} see a real principal. Fail-closed: a call without a verified user is
 * rejected instead of running with full access.
 */
public class UserScopedToolCallback implements ToolCallback {

    private final ToolCallback delegate;

    public UserScopedToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        throw unauthenticated();
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        JwtUserPrincipal user = McpUserContext.currentUser(toolContext).orElseThrow(UserScopedToolCallback::unauthenticated);

        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext scoped = SecurityContextHolder.createEmptyContext();
        scoped.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(scoped);
        try {
            return delegate.call(toolInput, toolContext);
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    private static AppException unauthenticated() {
        return new AppException(AuthErrorCode.KHONG_DU_QUYEN, "MCP tool calls require an authenticated user");
    }
}
