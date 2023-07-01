package com.example.awsdemo.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file);

    String getFile(String filePath);

    void deleteFile(String filePath);
}
