package com.mcm.backend.app.api.utils.security;

import jakarta.servlet.http.HttpServletRequest;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.UUID;

public final class SecurityContextUtil {

    private SecurityContextUtil() {}

    public static UUID getCurrentUserId() throws JsonErrorResponseException {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        HttpServletRequest req = attrs.getRequest();
        UUID userId = (UUID) req.getAttribute("authenticatedUserId");
        if (userId == null) {
            throw new JsonErrorResponseException("No authentication info found", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }
}