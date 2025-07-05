package com.mcm.backend.app.api.controllers.users.auth;

import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.api.utils.requestbody.RequestBodyUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.middlewares.jwt.JwtUtil;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> login(RequestBodyUtil requestBodyUtil) throws JsonErrorResponseException, NoSuchFieldException {

        // Get values from the request
        String providedUsername = requestBodyUtil.getField("username", String.class);
        String providedPassword = requestBodyUtil.getField("password", String.class);

        // Hash password
        String providedPasswordHash = PasswordHashUtil.hashPassword(providedPassword);

        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

            // Get user using username from DB
            User user = userDAO.getUnique(User.class.getDeclaredField("username"), providedUsername);

            // Check if user is found
            if (user == null) {
                throw new JsonErrorResponseException("User not found", HttpStatus.NOT_FOUND);
            }

            // Check if the password hash matches
            if (!user.getPasswordHash().equals(providedPasswordHash)) {
                throw new JsonErrorResponseException("Invalid credentials", HttpStatus.UNAUTHORIZED);
            }

            // Create a new token
            String token = JwtUtil.generateToken(user.getId());

            // Check if the user is admin
            boolean adminUser;
            try (DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {
                adminUser = adminDAO.existsByPrimaryKey(user.getId());
            }

            // TODO Redirect to next

            // Return session/user info
            return ResponseEntity.ok().body(Map.of(
                "token", token,
                "is_admin", adminUser,
                "user_id", user.getId()
            ));

        }
    }
}