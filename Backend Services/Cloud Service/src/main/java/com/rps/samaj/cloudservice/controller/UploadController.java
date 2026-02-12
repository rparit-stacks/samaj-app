package com.rps.samaj.cloudservice.controller;

import com.rps.samaj.cloudservice.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Upload endpoint for any file type.
 * Same logic for generic uploads, profile images (avatar_url), and cover/background images (cover_image_url).
 * Returns the public URL of the uploaded file.
 */
@RestController
@RequestMapping("/api/cloud")
public class UploadController {

    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Generic file upload. Use for any file type.
     * Optional folder: "profile" | "cover" | "uploads" | custom.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        String url = storageService.upload(file, folder);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Upload for profile/avatar image. Frontend uses /api/cloud/profile-image.
     */
    @PostMapping("/profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        String url = storageService.upload(file, "profile");
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Upload for cover/background image. Same upload logic, folder = "cover".
     */
    @PostMapping("/upload/cover-image")
    public ResponseEntity<Map<String, String>> uploadCoverImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        String url = storageService.upload(file, "cover");
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Alias for background/cover image. Frontend uses /api/cloud/background-image.
     */
    @PostMapping("/background-image")
    public ResponseEntity<Map<String, String>> uploadBackgroundImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        String url = storageService.upload(file, "cover");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
