package com.mcm.backend.app.api.controllers.serverinstances.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Staging import endpoint for server instance exports.
 *
 * Accepts an `export.zip`, checks for an `mcm_metadata.json`,
 * extracts all content to a temporary staging directory, and returns
 * a summary of what was found so the caller can proceed to finalize
 * creation of a new ServerInstance.
 *
 * NOTE: This controller intentionally does not write to the database yet.
 * It prepares the files and exposes a staging token so the next step can
 * allocate ports, choose a final name, and create the DB row.
 */
@RestController
@RequestMapping(path = "/api/server-instances/import")
public class ImportController {

    //private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * POST /api/server-instances/import
     * Body: multipart/form-data with part name "file" containing export.zip
     */
//    @RequireRole(Admin.class)
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<JsonNode> importArchive(@RequestParam("file") MultipartFile archive) throws JsonErrorResponseException {
//        if (archive == null || archive.isEmpty()) {
//            throw new JsonErrorResponseException("No file uploaded", HttpStatus.BAD_REQUEST);
//        }
//        if (!isZip(archive)) {
//            throw new JsonErrorResponseException("File must be a .zip archive", HttpStatus.BAD_REQUEST);
//        }
//
//
//
//        Path serverPath = serverInstance.getPath();
//
//        if (Files.exists(serverPath)) {
//            throw new JsonErrorResponseException("Target location already exists", HttpStatus.CONFLICT);
//        }
//
//        // Unzip to the staging directory
//        try (InputStream in = archive.getInputStream();
//             ZipInputStream zis = new ZipInputStream(in)) {
//            ZipEntry entry;
//            while ((entry = zis.getNextEntry()) != null) {
//                if (entry.isDirectory()) {
//                    Files.createDirectories(serverPath);
//                } else {
//                    Files.createDirectories(serverPath.getParent());
//                    Files.copy(zis, serverPath, StandardCopyOption.REPLACE_EXISTING);
//                }
//                zis.closeEntry();
//            }
//        } catch (IOException e) {
//            cleanupQuietly(serverPath);
//            throw new JsonErrorResponseException("Failed to read ZIP: " + e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//
//        return ResponseEntity.ok().build();
//
//    }

    private static boolean isZip(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null && name.toLowerCase().endsWith(".zip")) return true;
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("application/zip") || contentType.equals("application/x-zip-compressed"));
    }

    private static void cleanupQuietly(Path p) {
        try {
            if (p != null && Files.exists(p)) {
                Files.walk(p)
                        .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (IOException ignored) {};
                        });
            }
        } catch (IOException ignored) {
        }
    }
}
