package com.b4code.backend.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Cloudinary integration configuration.
 *
 * HOW IMAGE UPLOAD WORKS:
 * 1. Admin/owner uploads images via POST /api/images/upload (multipart)
 * 2. Backend receives the file and uploads it to Cloudinary via their SDK
 * 3. Cloudinary returns a secure URL (e.g., https://res.cloudinary.com/<cloud>/image/upload/v.../filename.jpg)
 * 4. Backend saves that URL into the database (Property.imageSrc, Room.imageSrc, etc.)
 * 5. Frontend fetches property data from API and renders the Cloudinary URL directly
 *
 * HOW TO ADD NEW PROPERTY IMAGES LATER (without changing frontend code):
 * 1. Upload the image file using POST /api/images/upload
 * 2. Receive the Cloudinary URL in the response
 * 3. Update the property record (via admin API) with the new URL
 * 4. Frontend automatically renders the new image on next load
 *
 * For the current seeder, we use Cloudinary's demo account public sample images.
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:demo}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Map<String, String> cloudinaryCredentials() {
        return Map.of(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        );
    }

    public String getCloudName() {
        return cloudName;
    }
}
