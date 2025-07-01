package com.mcm.backend.app.api.utils.service;

import com.mcm.backend.app.api.utils.security.SecurityContextUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;
import com.mcm.backend.app.api.controllers.serverinstances.roles.RoleUtil;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
// service that does all the “DAO + inheritance + membership” logic
public class AuthorizationService {

    /** check that the current user has the given global role (via your RequireRole). */
    // TODO Add extends User
    public void requireUserRole(Class<? extends TableEntity> roleEntityClass) throws JsonErrorResponseException {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        // Assumes all normalized/requireRole's will use UUID as the root User class does.
        try (DAO<?, UUID> dao = DAOFactory.createDAO(roleEntityClass)) {
            if (!dao.existsByPrimaryKey(userId)) {
                throw new JsonErrorResponseException(
                        "User is not authorized for role: " + roleEntityClass.getSimpleName(), HttpStatus.FORBIDDEN);
            }
        }
    }

    /** check that the current user has at least `baseRole` (and any inherited) on a given instance */
    public void requireInstanceRole(UUID serverInstanceId, String baseRoleName) throws JsonErrorResponseException, NoSuchFieldException {
        UUID userId = SecurityContextUtil.getCurrentUserId();

        // Admin bypass: global admins skip instance role checks
        if (isAdmin(userId)) {
            return;
        }

        // Collect inherited role‐names
        List<String> allowed;
        try (DAO<RoleEntity, String> RoleEntityDAO = DAOFactory.createDAO(RoleEntity.class);
             DAO<RoleInheritance, String> RoleInheritanceDAO = DAOFactory.createDAO(RoleInheritance.class)) {

            // Get originally assigned role
            RoleEntity base = RoleEntityDAO.get(baseRoleName);

            // Fetch child roles
            List<RoleEntity> inherited = RoleUtil.fetchAllInheritedRoles(base, RoleEntityDAO, RoleInheritanceDAO);
            allowed = inherited.stream().map(RoleEntity::name).toList();
        }

        // Load all user-instance assignments
        try (DAO<UserRoleAssignment, UUID> uraDao = DAOFactory.createDAO(UserRoleAssignment.class)) {
            List<UserRoleAssignment> assigns = new com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder<>(uraDao)
                    .where(UserRoleAssignment.class.getDeclaredField("userId"), userId)
                    .and(UserRoleAssignment.class.getDeclaredField("instanceId"), serverInstanceId)
                    .get();

            boolean allowedAccess = assigns.stream().map(UserRoleAssignment::getRole).anyMatch(allowed::contains);
            if (!allowedAccess) {
                throw new JsonErrorResponseException("User lacks required role " + baseRoleName, HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check whether the current user is a global ADMIN.
     * Admins bypass all instance-specific role checks.
     */
    private boolean isAdmin(UUID userId) {
        try (DAO<Admin, UUID> adminDao = DAOFactory.createDAO(Admin.class)) {
            return adminDao.existsByPrimaryKey(userId);
        }
    }
}