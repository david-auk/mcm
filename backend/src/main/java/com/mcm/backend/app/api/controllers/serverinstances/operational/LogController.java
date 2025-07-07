package com.mcm.backend.app.api.controllers.serverinstances.operational;

import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class LogController {
    @GetMapping("/api/server-instances/{id}/log")
    @RequireServerInstanceRole(Role.VIEWER)
    public ResponseEntity<List<String>> getLog(@PathVariable UUID id,
        @RequestParam(value = "fromHead", required = false) Integer fromHead) throws JsonErrorResponseException {

        List<String> logList;

        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server Instance not found", HttpStatus.NOT_FOUND);
            }

            Path logFile = ServerCoreUtil.getLogFilePath(serverInstance);

            List<String> allLines;
            try {
              allLines = Files.readAllLines(logFile);
            } catch (IOException e) {
              throw new JsonErrorResponseException("Could not read log file", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (fromHead != null) {
              int start = Math.min(fromHead, allLines.size());
              logList = new ArrayList<>(allLines.subList(start, allLines.size()));
            } else {
              logList = new ArrayList<>(allLines);
            }

            return ResponseEntity.ok(logList);

        }
    }
}
