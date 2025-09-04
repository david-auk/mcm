package com.mcm.backend.app.middlewares.data.roles;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;

import java.sql.Connection;
import java.util.List;

public class RoleDAO {
    public static List<RoleEntity> getRolesForInstance(DAO<RoleEntity, String> roleDAO, DAO<RoleInheritance, String> roleInheritanceDAO, ServerInstance serverInstance, User user) throws JsonErrorResponseException, NoSuchFieldException {
        return RoleUtil.getRoles(roleDAO, roleInheritanceDAO, serverInstance, user);
    }

    public static List<RoleEntity> getRolesForInstance(Connection connection, ServerInstance serverInstance, User user) throws JsonErrorResponseException, NoSuchFieldException {

        // Open DAO's and reuse method above
        try (DAO<RoleEntity, String> roleDAO = DAOFactory.createDAO(connection, RoleEntity.class);
             DAO<RoleInheritance, String> roleInheritanceDAO = DAOFactory.createDAO(connection, RoleInheritance.class)) {

            return getRolesForInstance(roleDAO, roleInheritanceDAO, serverInstance, user);
        }
    }
}
