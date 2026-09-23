package vn.edu.huce.iic.bts_ops_platform.common.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.security.auth.AuthErrorCode;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final ContractorScopeService contractorScopeService;

    @Before("@annotation(vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission)"
            + " || @within(vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        RequiresPermission requiresPermission = resolveRequiresPermission(joinPoint);
        if (requiresPermission == null) {
            return;
        }
        if (requiresPermission.anyOf().length > 0) {
            boolean allowed = Arrays.stream(requiresPermission.anyOf())
                    .anyMatch(this::isAllowed);
            if (!allowed) {
                throw new AppException(
                        AuthErrorCode.KHONG_DU_QUYEN,
                        "Thiếu một trong các quyền: " + String.join(", ", requiresPermission.anyOf()));
            }
            return;
        }
        if (!isAllowed(requiresPermission.value())) {
            throw new AppException(
                    AuthErrorCode.KHONG_DU_QUYEN,
                    "Thiếu quyền: " + requiresPermission.value());
        }
    }

    private boolean isAllowed(String quyenHanMa) {
        if (ContractorAuditPermissions.isAuditPermission(quyenHanMa)
                && contractorScopeService.isCurrentUserContractor()) {
            return false;
        }
        return hasAnyPermission(quyenHanMa) || isContractorReadBypass(quyenHanMa);
    }

    private boolean hasAnyPermission(String quyenHanMa) {
        String required = AuthorityPrefix.quyenHan(quyenHanMa);
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> hasPermission(a.getAuthority(), required));
    }

    private boolean hasPermission(String granted, String required) {
        if (required.equals(granted)) {
            return true;
        }
        if (AuthorityPrefix.isFullAccessAuthority(granted)) {
            return true;
        }
        return FullAccessRoleCodes.isFullAccessRoleAuthority(granted);
    }

    private boolean isContractorReadBypass(String requiredPermission) {
        if (!"GET".equalsIgnoreCase(currentHttpMethod())) {
            return false;
        }
        if (!ContractorReadPermissions.allowsRead(requiredPermission)) {
            return false;
        }
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> AuthorityPrefix.quyenHan(QuyenHanMa.XEM_NHIEM_VU_NHA_THAU).equals(a.getAuthority()));
    }

    private String currentHttpMethod() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getMethod();
    }

    private RequiresPermission resolveRequiresPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission methodLevel = method.getAnnotation(RequiresPermission.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequiresPermission classLevel = targetClass.getAnnotation(RequiresPermission.class);
        if (classLevel != null) {
            return classLevel;
        }
        return targetClass.getSuperclass() != null
                ? targetClass.getSuperclass().getAnnotation(RequiresPermission.class)
                : null;
    }
}
