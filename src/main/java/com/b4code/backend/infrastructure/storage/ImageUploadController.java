package com.b4code.backend.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST controller for image uploads.
 *
 * POST /api/images/upload
 *   - Accepts a multipart file and an optional folder parameter
 *   - Uploads the image to Cloudinary
 *   - Returns the secure URL
 *
 * This endpoint can be used by admin/owner dashboards to upload property images.
 * The returned URL should then be saved to the property/roomType record.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadController.class);
    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "properties") String folder) {

        log.info("POST /api/images/upload - file={}, folder={}", file.getOriginalFilename(), folder);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are accepted"));
        }

        // Max 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 10MB limit"));
        }

        try {
            String url = cloudinaryService.uploadImage(file, folder);
            return ResponseEntity.ok(Map.of(
                "url", url,
                "message", "Image uploaded successfully"
            ));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(503).body(Map.of(
                "error", "Image upload not available",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Image upload failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Upload failed",
                "message", e.getMessage()
            ));
        }
    }
}
