package com.mcm.backend.app.api.utils.components;

import com.mcm.backend.app.api.controllers.serverinstances.roles.RoleUtil;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Aspect
@Component
public class RequireServerInstanceRoleAspect {

    @Around("@annotation(requireRole)")
    public Object checkServerInstanceRole(
            ProceedingJoinPoint joinPoint,
            RequireServerInstanceRole requireRole) throws Throwable {

        // --- 1) Get authenticated userId from request
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest();
        UUID userId = (UUID) request.getAttribute("authenticatedUserId");
        if (userId == null) {
            throw new JsonErrorResponseException(
                    "No authentication info found", HttpStatus.UNAUTHORIZED);
        }

        // --- 2) Extract @PathVariable("id") UUID as serverInstanceId
        UUID serverInstanceId = extractServerInstanceId(joinPoint);

        // --- 3) Compute allowed roles (base + inherited)
        List<String> allowedRoleNames;
        try (DAO<RoleEntity, String> roleDao =
                     DAOFactory.createDAO(RoleEntity.class);
             DAO<RoleInheritance, String> inhDao =
                     DAOFactory.createDAO(RoleInheritance.class)) {

            RoleEntity base = roleDao.get(requireRole.value().name());
            List<RoleEntity> all = RoleUtil.fetchAllInheritedRoles(base, roleDao, inhDao);

            allowedRoleNames = all.stream()
                    .map(RoleEntity::name)
                    .toList();
        }

        // --- 4) Query your user_role_assignments junction table
        boolean hasRole;
        try (DAO<UserRoleAssignment, UUID> uraDao = DAOFactory.createDAO(UserRoleAssignment.class)) {

            // find all assignments for this user+instance
            List<UserRoleAssignment> assignments = new QueryBuilder<>(uraDao)
                    .where(UserRoleAssignment.class.getDeclaredField("userId"), userId)
                    .and(UserRoleAssignment.class.getDeclaredField("instanceId"), serverInstanceId)
                    .get();

            // check if any assignment’s role is in allowedRoleNames
            hasRole = assignments.stream()
                    .map(UserRoleAssignment::getRole)
                    .anyMatch(allowedRoleNames::contains);
        }

        if (!hasRole) {
            throw new JsonErrorResponseException(
                    "User lacks required role " + requireRole.value(),
                    HttpStatus.FORBIDDEN);
        }

        // --- 5) Proceed!
        return joinPoint.proceed();
    }

    private static UUID extractServerInstanceId(ProceedingJoinPoint joinPoint) {
        UUID serverInstanceId = null;
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();
        Annotation[][] paramAnns = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramAnns.length; i++) {
            for (Annotation ann : paramAnns[i]) {
                if (ann instanceof PathVariable) {
                    PathVariable pv = (PathVariable) ann;
                    if ("id".equals(pv.value()) || args[i] instanceof UUID) {
                        serverInstanceId = (UUID) args[i];
                        break;
                    }
                }
            }
            if (serverInstanceId != null) break;
        }
        if (serverInstanceId == null) {
            throw new IllegalStateException(
                    "Could not resolve @PathVariable UUID 'id' in " + method);
        }
        return serverInstanceId;
    }
}