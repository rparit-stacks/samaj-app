package com.rps.samaj.cloudservice.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for storage backends (Cloudinary, S3, etc.).
 * Each implementation returns a public URL for the uploaded file.
 */
public interface StorageProvider {

    /**
     * Upload a file and return its public URL.
     *
     * @param file   the file to upload
     * @param folder optional folder/prefix (e.g. "profile", "cover", "uploads")
     * @return the public URL of the uploaded file
     */
    String upload(MultipartFile file, String folder);
}
