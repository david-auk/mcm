package com.mcm.backend.app.database.models.server.backups.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcm.backend.app.database.models.server.backups.Backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReadBackup {
    static Backup readMetadata(Path metadataFile) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = Files.readString(metadataFile, StandardCharsets.UTF_8);
            return mapper.readValue(json, Backup.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read metadata for backup: " + metadataFile, e);
        }
    }
}
