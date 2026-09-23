package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    String value() default "";

    /** Nếu khai báo, user chỉ cần một trong các quyền hạn này. */
    String[] anyOf() default {};
}
