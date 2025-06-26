package com.mcm.backend.app.api.controllers.serverinstances.roles;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;

import java.util.*;

public class RoleUtil {

    public static List<ServerInstance> getServerInstances(Role role, UUID userId) throws NoSuchFieldException {

        List<ServerInstance> serverInstances = new ArrayList<>();
        List<UserRoleAssignment> assignments;

        try (DAO<UserRoleAssignment, UUID> uraDao = DAOFactory.createDAO(UserRoleAssignment.class)) {
             assignments = new QueryBuilder<>(uraDao)
                    .where(UserRoleAssignment.class.getDeclaredField("userId"), userId)
                    .get();
        }

        if (!assignments.isEmpty()) {
            try (
                    DAO<RoleEntity, String> roleDao = DAOFactory.createDAO(RoleEntity.class);
                    DAO<RoleInheritance, String> roleInheritanceDao = DAOFactory.createDAO(RoleInheritance.class);
                    DAO<ServerInstance, UUID> serverInstanceDao = DAOFactory.createDAO(ServerInstance.class);
            ){
                for (UserRoleAssignment assignment : assignments) {
                    List<RoleEntity> userRoleForAssignment;

                    RoleEntity assignedRole = roleDao.get(assignment.getRole());

                    // Get users roles for this server instance
                    userRoleForAssignment = fetchAllInheritedRoles(assignedRole, roleDao, roleInheritanceDao);

                    // For each role
                    for (RoleEntity roleEntity : userRoleForAssignment) {

                        // If one of the assigned roles matches the role we're searching for
                        if (role.toString().equals(roleEntity.name())) {

                            ServerInstance serverInstance = serverInstanceDao.get(assignment.getInstanceId());

                            // Add the server instance to the list of matches
                            serverInstances.add(serverInstance);

                            // Break if found
                            break;
                        }
                    }
                }
            }
        }

        return serverInstances;
    }

    public static UserRoleAssignment fetchUserRoleAssignment(UUID userId, UUID serverInstanceId) throws NoSuchFieldException {

        if (userId == null) throw new RuntimeException("userId cant be null");
        if (serverInstanceId == null) throw new RuntimeException("serverInstanceId cant be null");

        try (DAO<UserRoleAssignment, UUID> uraDao = DAOFactory.createDAO(UserRoleAssignment.class)) {

            List<UserRoleAssignment> assignments = new QueryBuilder<>(uraDao)
                    .where(UserRoleAssignment.class.getDeclaredField("userId"), userId)
                    .and(  UserRoleAssignment.class.getDeclaredField("instanceId"), serverInstanceId)
                    .get();

            if (assignments.isEmpty()) {
                return null;
            }
            return assignments.getFirst();
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
