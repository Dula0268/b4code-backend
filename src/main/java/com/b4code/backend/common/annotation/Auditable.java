package com.b4code.backend.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should be audited.
 * Used by AuditAspect to log method execution details.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    /**
     * Action description for the audit log.
     */
    String action() default "OPERATION";

    /**
     * Entity type being modified.
     */
    String entity() default "UNKNOWN";

    /**
     * Whether to include method parameters in the audit log.
     */
    boolean includeArgs() default true;

    /**
     * Whether to include return value in the audit log.
     */
    boolean includeResult() default true;
}
