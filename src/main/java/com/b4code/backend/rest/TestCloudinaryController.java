package com.b4code.backend.rest;

import com.b4code.backend.infrastructure.storage.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Test controller to verify Cloudinary uploads.
 * Endpoint: POST /api/guest/test/upload
 */
@RestController
@RequestMapping("/api/guest/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TestCloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Upload to a test folder in Cloudinary
            String url = cloudinaryService.uploadImage(file, "test_uploads");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Image uploaded successfully to Cloudinary!",
                "url", url
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Upload failed: " + e.getMessage()
            ));
        }
    }
}



