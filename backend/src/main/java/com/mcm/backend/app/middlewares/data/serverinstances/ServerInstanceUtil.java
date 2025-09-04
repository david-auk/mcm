package com.mcm.backend.app.middlewares.data.serverinstances;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;
import com.mcm.backend.app.middlewares.data.roles.RoleUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerInstanceUtil {

    static Role LIST_ROLE = Role.VIEWER;

    public static List<ServerInstance> getServerInstances(Connection connection, User user) {

        List<ServerInstance> serverInstances = new ArrayList<>();
        List<UserRoleAssignment> assignments;
        try {


            try (DAO<UserRoleAssignment, UUID> uraDao = DAOFactory.createDAO(UserRoleAssignment.class)) {
                assignments = new QueryBuilder<>(uraDao)
                        .where(UserRoleAssignment.class.getDeclaredField("user"), user)
                        .get();
            }

            if (!assignments.isEmpty()) {
                try (
                        DAO<RoleEntity, String> roleDao = DAOFactory.createDAO(connection, RoleEntity.class);
                        DAO<RoleInheritance, String> roleInheritanceDao = DAOFactory.createDAO(connection, RoleInheritance.class);
                ) {
                    for (UserRoleAssignment assignment : assignments) {
                        List<RoleEntity> userRoleForAssignment;

                        RoleEntity assignedRole = roleDao.get(assignment.getRole());

                        // Get users roles for this server instance
                        userRoleForAssignment = RoleUtil.fetchAllInheritedRoles(assignedRole, roleDao, roleInheritanceDao);

                        // For each role
                        for (RoleEntity roleEntity : userRoleForAssignment) {

                            // If one of the assigned roles matches the role we're searching for
                            if (LIST_ROLE.toString().equals(roleEntity.name())) {

                                // Add the server instance to the list of matches
                                serverInstances.add(assignment.getServerInstance());

                                // Break if found
                                break;
                            }
                        }
                    }
                }
            }

        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return serverInstances;
    }
}
