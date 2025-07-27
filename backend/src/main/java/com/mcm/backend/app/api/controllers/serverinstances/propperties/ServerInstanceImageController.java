package com.mcm.backend.app.api.controllers.serverinstances.propperties;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances/{serverId}/image")
public class ServerInstanceImageController {

  // Retrieve the icon image for the specified server instance
  @GetMapping
  public ResponseEntity<Resource> getImage(@PathVariable UUID serverId) throws IOException, JsonErrorResponseException {
    try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
      // Fetch server instance; return 404 if not found
      ServerInstance serverInstance = serverInstanceDAO.get(serverId);
      if (serverInstance == null) {
        throw new JsonErrorResponseException("Server instance with id " + serverId + " not found", HttpStatus.NOT_FOUND);
      }

      Path imagePath = getImagePath(serverInstance);

      // Check if image exists
      if (!Files.exists(imagePath)) {
        return ResponseEntity.notFound().build();
      }

      // Respond with image
      byte[] data = Files.readAllBytes(imagePath);
      Resource resource = new ByteArrayResource(data);
      return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .body(resource);
    }
  }

  // Upload or replace the icon image for the specified server instance
  @PostMapping
  public ResponseEntity<Void> uploadImage(@PathVariable UUID serverId,
                                          @RequestParam("file") MultipartFile file) {
    try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

      ServerInstance serverInstance = serverInstanceDAO.get(serverId);

      if (serverInstance == null) {
        return ResponseEntity.notFound().build();
      }

      // Ensure the server directory exists and save the uploaded image as icon.png
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, getImagePath(serverInstance), StandardCopyOption.REPLACE_EXISTING);
      }
      return ResponseEntity.ok().build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Delete the icon image for the specified server instance
  @DeleteMapping
  public ResponseEntity<Void> deleteImage(@PathVariable UUID serverId) {
    try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

      ServerInstance serverInstance = serverInstanceDAO.get(serverId);

      if (serverInstance == null) {
        return ResponseEntity.notFound().build();
      }

      // Remove icon.png if it exists
      Files.deleteIfExists(getImagePath(serverInstance));
      return ResponseEntity.noContent().build();
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private Path getImagePath(ServerInstance serverInstance) {
    return serverInstance.getPath().resolve("server-icon.png");
  }
}
