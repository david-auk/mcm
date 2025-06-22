package com.mcm.backend.app.api.utils.components;

import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;

import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;


import java.util.Objects;
import java.util.UUID;

@Aspect
@Component
public class RequireRoleAspect {

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        Class<? extends TableEntity> entityClass = requireRole.value();

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        UUID userId = (UUID) request.getAttribute("authenticatedUserId");

        if (userId == null) {
            throw new JsonErrorResponseException("No authentication bearer/info found", HttpStatus.UNAUTHORIZED);
        }

        // Assumes all normalized/requireRole's will use UUID as the root User class does.
        try (DAO<?, UUID> dao = DAOFactory.createDAO(entityClass)) {
            if (!dao.existsByPrimaryKey(userId)) {
                throw new JsonErrorResponseException("User is not authorized for role: " + entityClass.getSimpleName(), HttpStatus.UNAUTHORIZED);
            }
        }

        return joinPoint.proceed();
    }
}

