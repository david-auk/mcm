package com.mcm.backend.app.api.utils.security;


import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.api.utils.aop.JoinPointUtils;
import com.mcm.backend.app.api.utils.service.AuthorizationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class SecurityAspects {

    private final AuthorizationService auth;

    public SecurityAspects(AuthorizationService auth) {
        this.auth = auth;
    }

    @Around("@annotation(rr)")
    public Object globalRole(ProceedingJoinPoint jp, RequireRole rr) throws Throwable {
        auth.requireUserRole(rr.value());
        return jp.proceed();
    }

    @Around("@annotation(rr)")
    public Object instanceRole(ProceedingJoinPoint jp, RequireServerInstanceRole rr) throws Throwable {
        UUID serverInstanceId = JoinPointUtils.extractPathVariable(jp, "serverInstanceId", UUID.class);
        auth.requireInstanceRole(serverInstanceId, rr.value().name());
        return jp.proceed();
    }
}
