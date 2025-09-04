package com.mcm.backend.app.api.controllers.serverinstances.export;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.Database;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances/{serverInstanceId}/export")
public class ExportController {
    @GetMapping
    @RequireServerInstanceRole(Role.MAINTAINER)
    public ResponseEntity<?> exportServerInstance(@PathVariable UUID serverInstanceId, @CurrentUser User user) throws JsonErrorResponseException, NoSuchFieldException, SQLException, IOException {
        try (
                Connection connection = Database.getConnection();
                DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(connection, ServerInstance.class);
        ) {
            ServerInstance serverInstance = serverInstanceDAO.get(serverInstanceId);
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server Instance Not Found", HttpStatus.NOT_FOUND);
            }

            byte[] zipBytes = createExportZip(serverInstance.getPath(), serverInstance);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.zip\"");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
            // LoggingUtil.log(ActionType.EXPORT_SERVER_INSTANCE); TODO Add log
        }
    }

    /**
     * Creates an in-memory ZIP archive containing:
     *  - All files under the given server instance directory
     *  - A metadata file named "mcm_metadata.json" with a JSON serialization of the ServerInstance object
     *
     * The resulting archive is returned as a byte array suitable for sending as a download.
     */
    private static byte[] createExportZip(Path serverPath, ServerInstance serverInstance) throws IOException {
        if (serverPath == null) {
            throw new IOException("Server instance path is null");
        }

        // Prepare JSON serializer
        ObjectMapper mapper = new ObjectMapper();
        // Register modules if present (e.g., JavaTimeModule)
        mapper.findAndRegisterModules();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        // Build ZIP in memory
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 1) Add directory contents
            if (Files.exists(serverPath)) {
                Files.walk(serverPath)
                        .filter(Files::isRegularFile)
                        .filter(p -> {
                            // Do not include an existing export.zip if it lives inside the folder
                            return !p.getFileName().toString().equalsIgnoreCase("export.zip");
                        })
                        .forEach(p -> {
                            Path relative = serverPath.relativize(p);
                            String entryName = relative.toString().replace('\\', '/');
                            try {
                                ZipEntry entry = new ZipEntry(entryName);
                                zos.putNextEntry(entry);
                                Files.copy(p, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            // 2) Add metadata JSON as mcm_metadata.json at the root of the zip
            ZipEntry metaEntry = new ZipEntry("mcm_metadata.json");
            zos.putNextEntry(metaEntry);
            byte[] json = writer.writeValueAsBytes(serverInstance);
            zos.write(json);
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        }
    }
}
