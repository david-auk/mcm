package com.mcm.backend.app.database.models.server.backups.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.Backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class WriteBackup {

    static void writeServerFiles(Backup backup) {

        Set<String> filenamesToIgnore = new HashSet<>(Collections.singleton("backups"));

        Path backupDir = backup.getPath();

        // Backup target (minus files to ignore)
        Path serverDir = backup.serverInstance().getPath();

        try (Stream<Path> paths = Files.walk(serverDir)) {
            paths.filter(path -> {
                String name = path.getFileName().toString();
                return !filenamesToIgnore.contains(name);
            }).forEach(path -> {
                Path relative = serverDir.relativize(path);
                Path target = backupDir.resolve(relative);
                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy file: " + path, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to backup server files", e);
        }
    }

    static void writeMetadata(Backup backup, Path metadataFile) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(backup);
            Files.createDirectories(metadataFile.getParent());
            Files.writeString(metadataFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write metadata for backup", e);
        }
    }

}
