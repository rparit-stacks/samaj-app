package com.rps.samaj.cloudservice.service;

import com.rps.samaj.cloudservice.storage.CloudinaryStorageProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final CloudinaryStorageProvider cloudinary;

    public StorageService(CloudinaryStorageProvider cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file, String folder) {
        return cloudinary.upload(file, folder);
    }
}
