package ru.optimusmac.timetracker.controllers;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import ru.optimusmac.timetracker.response.UploadResponse;
import ru.optimusmac.timetracker.service.UserService;

@RestController
@RequestMapping("/profile")
public class ProfileController {

  @Value("${image.upload-dir}")
  private String uploadDir;


  public UserService service;

  public ProfileController(UserService service) {
    this.service = service;
  }

  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal User user) {
    Map<String, Object> response = new HashMap<>();
    ru.optimusmac.timetracker.model.User us = service.findByEmail(user.getUsername());
    long userId = us.getId();
    try {
      String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
      String uniqueFileName = userId + fileExtension;

      Path path = Paths.get(uploadDir + "/images/" + uniqueFileName);
      Files.createDirectories(path.getParent());
      file.transferTo(path);

      response.put("success", true);
      response.put("fileName", uniqueFileName);
      response.put("userId", userId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Ошибка при загрузке изображения.");
      return ResponseEntity.status(500).body(response);
    }
  }
  @GetMapping("/get-profile-photo/{userId}")
  public ResponseEntity<Resource> getProfilePhoto(@PathVariable("userId") Long userId) {
    try {
      Path dirPath = Paths.get(uploadDir + "/images/");
      File dir = new File(dirPath.toString());
      String[] files = dir.list((dir1, name) -> name.startsWith(String.valueOf(userId)));

      if (files != null && files.length > 0) {
        Path filePath = Paths.get(uploadDir + "/images/" + files[0]);
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
          return ResponseEntity.ok()
              .contentType(MediaType.IMAGE_PNG)
              .body(resource);
        }
      }
      Path defaultPath = Paths.get(uploadDir + "/images/defaultProfile.png");
      Resource resource = new UrlResource(defaultPath.toUri());
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(resource);
    } catch (MalformedURLException e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("/user/profile")
  public String profile() {
    return "profile"; // Возвращаем страницу с профилем
  }
}
