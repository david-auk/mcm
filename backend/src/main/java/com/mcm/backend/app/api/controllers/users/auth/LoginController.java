package com.mcm.backend.app.api.controllers.users.auth;

import com.mcm.backend.app.api.utils.RequestBodyUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, ?> requestBody) throws JsonErrorResponseException {

        // Build the RBU
        RequestBodyUtil requestBodyUtil = new RequestBodyUtil(requestBody);

        // Get values from request
        String providedUsername = requestBodyUtil.getField("username", String.class);
        String providedPasswordHash = requestBodyUtil.getField("username", String.class);

        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

            // Get user using username from DB
            User user = userDAO.getUnique(User.class.getDeclaredField("username"), providedUsername);

            // Check if user is found
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            // Check if password hash matches
            if (!user.getPasswordHash().equals(providedPasswordHash)) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            // Create new token
            String token = JwtUtil.generateToken(user.getId());

            // Check if user is admin
            boolean adminUser;
            try (DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {
                adminUser = adminDAO.existsByPrimaryKey(user.getId());
            }

            // Return session/user info
            return ResponseEntity.ok().body(Map.of(
                "token", token,
                "is_admin", adminUser
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }
}