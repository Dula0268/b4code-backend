package com.b4code.backend.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final String uploadPreset;

    public CloudinaryService(Map<String, String> credentials) {
        String cloudName = credentials.getOrDefault("cloud_name", "demo");
        String apiKey = credentials.getOrDefault("api_key", "");
        String apiSecret = credentials.getOrDefault("api_secret", "");
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
        this.uploadPreset = credentials.getOrDefault("upload_preset", "");
        
        log.info("Cloudinary initialized for cloud: {}", cloudName);
    }

    /**
     * Uploads an image to Cloudinary.
     * @param file The file to upload
     * @param folder Optional folder name
     * @return The secure URL of the uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) {
        String cloudName = cloudinary.config.cloudName;
        if (cloudName == null || "demo".equalsIgnoreCase(cloudName)) {
            throw new UnsupportedOperationException(
                "Cloudinary is not configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in .env");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder != null ? folder : "general");
            params.put("resource_type", "auto"); // Better than just "image" for flexibility
            
            // If we have API key/secret, it will be a signed upload.
            // If we only want unsigned, we'd need to use only the preset.
            // But since we have credentials in .env, signed is better.
            if (uploadPreset != null && !uploadPreset.isEmpty()) {
                params.put("upload_preset", uploadPreset);
            }

            log.info("Starting Cloudinary upload: filename={}, folder={}", file.getOriginalFilename(), folder);
            
            // Use getInputStream for efficiency
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Cloudinary upload successful: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("IO Error during Cloudinary upload: {}", e.getMessage());
            throw new RuntimeException("Cloudinary IO Error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Cloudinary upload: {}", e.getClass().getName() + " - " + e.getMessage());
            throw new RuntimeException("Cloudinary Upload Failed: " + e.getMessage(), e);
        }
    }
}
