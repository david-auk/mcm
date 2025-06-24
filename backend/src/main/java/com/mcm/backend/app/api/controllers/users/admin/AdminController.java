package com.mcm.backend.app.api.controllers.users.admin;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminController {


    /**
     * List all admins.
     */
    @GetMapping
    @RequireRole(Admin.class)
    public ResponseEntity<List<Admin>> getAllAdmins() {
        // Get all admins from the DB
        try (DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {
            return ResponseEntity.ok(new ArrayList<>(adminDAO.getAll()));
        }
    }

    /**
     * Demote admin to user.
     */
    @DeleteMapping("/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> demote(@CurrentUser User currentUser, @PathVariable UUID id) throws JsonErrorResponseException {
        // Get all users from the DB
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            User user = userDAO.get(id);
            if (user == null) {
                throw new JsonErrorResponseException("User with id " + id.toString() + " not found", HttpStatus.NOT_FOUND);
            }

            try (DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {

                Admin admin = adminDAO.get(id);

                if (admin == null) {
                    throw new JsonErrorResponseException("Admin with id " + id.toString() + " not found", HttpStatus.NOT_FOUND);
                }

                // Delete from admin table (demote to user)
                adminDAO.delete(admin.getId());

                // Log event
                LoggingUtil.log(ActionType.ADMIN_DEMOTE, currentUser, user);

                // Give response
                return ResponseEntity.ok("Demoted admin with id " + id.toString() + " to user");
            }
        }
    }

}
