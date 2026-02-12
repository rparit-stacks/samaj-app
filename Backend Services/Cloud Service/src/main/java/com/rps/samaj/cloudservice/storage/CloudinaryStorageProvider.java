package com.rps.samaj.cloudservice.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rps.samaj.cloudservice.config.StorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
public class CloudinaryStorageProvider implements StorageProvider {

    private final Cloudinary cloudinary;

    public CloudinaryStorageProvider(StorageProperties props) {
        this.cloudinary = new Cloudinary(Map.of(
                "cloud_name", props.getCloudinaryCloudName(),
                "api_key", props.getCloudinaryApiKey(),
                "api_secret", props.getCloudinaryApiSecret(),
                "secure", true
        ));
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            String publicId = folder != null && !folder.isBlank()
                    ? folder + "/" + UUID.randomUUID()
                    : UUID.randomUUID().toString();
            Map<String, Object> opts = ObjectUtils.asMap("public_id", publicId);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), opts);
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }
}
