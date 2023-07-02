package com.example.awsdemo.service;

import com.example.awsdemo.exception.StorageException;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Service
public class StorageServiceImpl implements StorageService {

    private final S3Template s3Template;
    private final String bucketName = "homeworkfiles3";

    @Autowired
    public StorageServiceImpl(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    @Override
    public String uploadFile(MultipartFile file) {
//        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String originalFilename = file.getOriginalFilename();
        Assert.notNull(originalFilename, "File name must not be null");
        String fileName = originalFilename.replaceAll("\\s", "_").replaceAll("[^a-zA-Z0-9.\\-_]", "").toLowerCase();

        if (fileName.isBlank()) {
            throw new StorageException("Invalid file name");
        }

        try {
            s3Template.upload(bucketName, fileName, file.getInputStream());
        } catch (IOException e) {
            throw new StorageException("Failed to upload file", e);
        }

        // Return the path of the stored file in S3 (without the host)
        return bucketName + "/" + fileName;
    }

    @Override
    public String getFile(String filePath) {
        // filePath is BucketName/FileName
        String bucket = filePath.split("/")[0];
        String filename = filePath.split("/")[1];
        return s3Template.createSignedGetURL(bucket, filename, Duration.ofMinutes(5)).toString();
    }

    @Override
    public void deleteFile(String filePath) {
        // filePath is BucketName/FileName
        String bucket = filePath.split("/")[0];
        String filename = filePath.split("/")[1];
        s3Template.deleteObject(bucket, filename);
    }
}
