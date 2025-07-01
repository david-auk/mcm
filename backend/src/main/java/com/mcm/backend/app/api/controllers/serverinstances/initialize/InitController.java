package com.mcm.backend.app.api.controllers.serverinstances.initialize;

import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.process.ProcessRegistry;
import com.mcm.backend.app.api.utils.process.ProcessStatus;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances")
public class InitController {

    private final ProcessRegistry registry;
    private final InitService initService;

    public InitController(ProcessRegistry registry, InitService initService) {
        this.registry = registry;
        this.initService = initService;
    }

    @PostMapping("/initialize/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<Map<String, UUID>> startInit(@PathVariable UUID id)
            throws JsonErrorResponseException
    {

        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);

            // Check if server instance exists
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server Instance not found", HttpStatus.NOT_FOUND);
            }

            // Check if server already initialized
            if (serverInstance.getEulaAccepted()) {
                throw new JsonErrorResponseException("Already initialized", HttpStatus.CONFLICT);
            }

            // sanity-check serverId exists, etc. (you can still do the DAO lookup here synchronously)
            UUID pid = registry.create();
            initService.doInitialization(serverInstance, pid);
            return ResponseEntity.accepted().body(Map.of("processId", pid));
        }
    }

    @GetMapping("/initialize/status/{processId}")
    public ResponseEntity<ProcessStatus> getStatus(@PathVariable UUID processId) {
        ProcessStatus status = registry.get(processId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}

