package com.b4code.backend.modules.admin.common.aop;

import com.b4code.backend.modules.admin.common.annotation.Auditable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @AfterReturning(value = "@annotation(com.b4code.backend.modules.admin.common.annotation.Auditable)", returning = "result")
    public void logAction(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            // Fetch IP Address
            String ipAddress = "127.0.0.1";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
            }

            String action = auditable.action();
            String entity = auditable.entity();
            String methodName = method.getName();

            // Log audit information
            log.info("AUDIT LOG - Action: {}, Entity: {}, Method: {}, IP: {}",
                    action, entity, methodName, ipAddress);

        } catch (Exception e) {
            log.error("Failed to record audit log via AOP", e);
        }
    }
}
