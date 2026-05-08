package com.hospitality.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Service for uploading images to Cloudinary.
 *
 * Uses Cloudinary's unsigned upload REST API (no SDK dependency needed).
 * For production use, switch to signed uploads with your API key/secret.
 *
 * Upload flow:
 * 1. Receive MultipartFile from controller
 * 2. POST to Cloudinary's upload endpoint
 * 3. Parse the returned JSON to extract `secure_url`
 * 4. Return the URL to the caller (who saves it in the DB)
 */
@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Map<String, String> credentials;

    public CloudinaryService(Map<String, String> cloudinaryCredentials) {
        this.credentials = cloudinaryCredentials;
    }

    /**
     * Uploads an image to Cloudinary and returns the secure URL.
     *
     * @param file   the image file
     * @param folder optional folder path in Cloudinary (e.g., "properties")
     * @return the Cloudinary secure URL of the uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) {
        String cloudName = credentials.get("cloud_name");

        if ("demo".equals(cloudName) || cloudName == null || cloudName.isBlank()) {
            log.warn("Cloudinary cloud name is 'demo' or not configured. "
                    + "Image upload is not available. Configure cloudinary.cloud-name in application properties.");
            throw new UnsupportedOperationException(
                "Cloudinary is not configured. Set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret.");
        }

        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        try {
            String boundary = UUID.randomUUID().toString();
            HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

                // Upload preset (for unsigned uploads, create an unsigned preset in Cloudinary dashboard)
                addFormField(writer, boundary, "upload_preset", "ml_default");

                if (folder != null && !folder.isBlank()) {
                    addFormField(writer, boundary, "folder", folder);
                }

                // File part
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                      .append(file.getOriginalFilename()).append("\"\r\n");
                writer.append("Content-Type: ").append(file.getContentType()).append("\r\n\r\n");
                writer.flush();
                os.write(file.getBytes());
                os.flush();
                writer.append("\r\n");

                // End boundary
                writer.append("--").append(boundary).append("--\r\n");
                writer.flush();
            }

            int status = conn.getResponseCode();
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String response = new String(is.readAllBytes());

            if (status >= 400) {
                log.error("Cloudinary upload failed with status {}: {}", status, response);
                throw new RuntimeException("Cloudinary upload failed: " + response);
            }

            // Extract secure_url from JSON response (simple parsing without JSON library)
            String secureUrl = extractJsonValue(response, "secure_url");
            log.info("Image uploaded to Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }

    private void addFormField(PrintWriter writer, String boundary, String name, String value) {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        writer.append(value).append("\r\n");
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) throw new RuntimeException("Key not found in response: " + key);
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end).replace("\\/", "/");
    }
}
