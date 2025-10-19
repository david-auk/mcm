package com.mcm.backend.app.middlewares.data.roles;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.daos.querying.QueryBuilder;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;

import java.sql.Connection;
import java.util.*;

public class RoleUtil {

    public static List<RoleEntity> getRoles(DAO<RoleEntity, String> roleDAO, DAO<RoleInheritance, String> roleInheritanceDAO, ServerInstance serverInstance, User user) throws JsonErrorResponseException, NoSuchFieldException {

        if (serverInstance == null) {
            throw new JsonErrorResponseException("Server instance is null");
        }

        // Get the current users role assignment for this serverInstance
        UserRoleAssignment userRoleAssignment = RoleUtil.fetchUserRoleAssignment(user, serverInstance);

        // If the user has no roles assigned; they are not permitted to view
        if (userRoleAssignment == null) {
            throw new JsonErrorResponseException("Server not found", HttpStatus.NOT_FOUND);
        }

        // Build the sting to role
        RoleEntity assignedRole;

        assignedRole = roleDAO.get(userRoleAssignment.getRole());

        // Get all the (Inherited) roles
        return RoleUtil.fetchAllInheritedRoles(assignedRole, roleDAO, roleInheritanceDAO);
    }

    public static UserRoleAssignment fetchUserRoleAssignment(User user, ServerInstance serverInstance) throws NoSuchFieldException {

        if (user == null) throw new RuntimeException("user cant be null");
        if (serverInstance == null) throw new RuntimeException("serverInstance cant be null");

        try (DAO<UserRoleAssignment, UUID> uraDao = DAOFactory.createDAO(UserRoleAssignment.class)) {

            List<UserRoleAssignment> assignments = new QueryBuilder<>(uraDao)
                    .where(UserRoleAssignment.class.getDeclaredField("user"), user)
                    .and(  UserRoleAssignment.class.getDeclaredField("serverInstance"), serverInstance)
                    .get();

            if (assignments.isEmpty()) {
                return null;
            }
            return assignments.getFirst(); // TODO Add security by db constraint. Only one rule per user server relation
        }
    }

    public static List<RoleEntity> fetchAllInheritedRoles(RoleEntity baseRole, DAO<RoleEntity, String> roleDao,
                                                          DAO<RoleInheritance, String> inheritanceDao) throws NoSuchFieldException {

        if (!roleDao.exists(baseRole)) throw new RuntimeException("Role " + baseRole + " not found");

        List<RoleEntity> roles = new ArrayList<>();
        roles.add(baseRole);

        Set<String> visited  = new HashSet<>();
        RoleEntity current = baseRole;


        while (visited.add(current.name())) {
            // find the next inherited role
            RoleEntity parent = findChildRole(current, inheritanceDao, roleDao);
            if (parent == null) {
                break;
            }
            roles.add(parent);
            current = parent;
        }

        return roles;
    }

    /**
     * Returns the single RoleEntity that `roleName` inherits from, or null if none.
     */
    private static RoleEntity findChildRole(RoleEntity role, DAO<RoleInheritance, String> roleInheritanceDAO, DAO<RoleEntity, String> roleDAO) throws NoSuchFieldException {
        List<RoleInheritance> roleInheritances = new QueryBuilder<>(roleInheritanceDAO)
                .where(RoleInheritance.class.getDeclaredField("roleName"), role.name())
                .get();

        if (roleInheritances.isEmpty()) {
            return null;
        }

        return roleDAO.get(roleInheritances.getFirst().inheritsRoleName());
    }
}
