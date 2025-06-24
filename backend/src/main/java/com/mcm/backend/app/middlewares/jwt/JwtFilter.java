package com.mcm.backend.app.middlewares.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTH_HEADER = "Authorization";
    public static final String PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(PREFIX)) {
            String token = authHeader.substring(PREFIX.length());

            try {
                UUID userId = JwtUtil.validateTokenAndGetUserId(token);
                request.setAttribute("authenticatedUserId", userId);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}

