package com.mcm.backend.app.api.controllers.users.auth;

import com.mcm.backend.app.middlewares.jwt.JwtFilter;
import com.mcm.backend.app.middlewares.jwt.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(JwtFilter.AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(JwtFilter.PREFIX)) {
            return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(JwtFilter.PREFIX.length());
        TokenBlacklist.blacklist(token);

        return ResponseEntity.ok("Successfully logged out");
    }
}
