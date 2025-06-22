package com.mcm.backend.app.api.controllers.users.user;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.models.users.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/me")
public class UserMeController {

    @RequireRole(User.class)
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@CurrentUser User user) {
        return ResponseEntity.ok(user);
    }
}