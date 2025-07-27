package com.mcm.backend.app.database.models.server.backups.utils;

import com.mcm.backend.app.database.models.server.backups.Backup;
import com.mcm.backend.app.database.models.server.backups.utils.ReadBackup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class LoadBackup {

    /**
     * Retrieves the most recently modified backup directory for the given server instance.
     * @param serverInstancePath the server dir whose backups to scan
     * @return the latest Backup object
     */
     static Backup getLatestBackup(Path serverInstancePath) {
        try (Stream<Path> dirs = Files.list(serverInstancePath)) {
            Optional<Path> latest = dirs.filter(Files::isDirectory)
                    .max(Comparator.comparing(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }));
            if (latest.isPresent()) {
                Path latestPath = latest.get();
                UUID id = UUID.fromString(latestPath.getFileName().toString());

                Path metadataFile = BackupUtil.getMetadataFile(serverInstancePath, id);

                return ReadBackup.readMetadata(metadataFile);
            } else {
                throw new RuntimeException("No backups found in " + serverInstancePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list backups directory: " + serverInstancePath, e);
        }
    }
}
