package com.example.awsdemo.service;

import com.example.awsdemo.entity.Homework;
import com.example.awsdemo.exception.ResourceNotFoundException;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private final DynamoDbTemplate dynamoDbTemplate;
    private final StorageService storageService;

    @Autowired
    public HomeworkServiceImpl(DynamoDbTemplate dynamoDbTemplate, StorageService storageService) {
        this.dynamoDbTemplate = dynamoDbTemplate;
        this.storageService = storageService;
    }

    @Override
    public Homework getHomework(String trainerId, String homeworkId) {
        return requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
    }

    @Override
    public List<Homework> getHomeworkByTrainerId(String trainerId) {
        QueryEnhancedRequest queryConditional = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(trainerId).build()))
                .build();

        // Query DynamoDb
        Iterable<Homework> results = dynamoDbTemplate.query(queryConditional, Homework.class).items();

        // Convert Iterable to List
        List<Homework> homeworks = StreamSupport.stream(results.spliterator(), false).toList();
        if (homeworks.isEmpty()) {
            throw new ResourceNotFoundException("Homework not found");
        }
        return homeworks;
    }

    @Override
    public String createHomework(Homework homework) {
        // Generate the UUID
        String homeworkId = UUID.randomUUID().toString();

        // Generate the creation date in the "yyyy-M-d" format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        String createdAt = LocalDate.now().format(formatter);

        // Set them on the homework object
        homework.setHomeworkId(homeworkId);
        homework.setCreatedAt(createdAt);

        // Use the DynamoDbEnhancedClient to put the item in the table
        dynamoDbTemplate.save(homework);
        // return uuid
        return homeworkId;
    }

    @Override
    public void deleteHomework(String trainerId, String homeworkId) {
        Homework homework = requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        if (homework.getPath() != null && !homework.getPath().isEmpty()) {
            storageService.deleteFile(homework.getPath());
        }
        dynamoDbTemplate.delete(homework);
    }

    @Override
    public String getHomeworkFile(String trainerId, String homeworkId) {
        Homework homework = requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        if (homework.getPath() == null || homework.getPath().isEmpty()) {
            throw new ResourceNotFoundException("Homework file not found");
        }
        return storageService.getFile(homework.getPath());
    }

    @Override
    public String uploadHomeworkFile(String trainerId, String homeworkId, MultipartFile file) {
        String path = storageService.uploadFile(file);
        Homework homework = requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        homework.setPath(path);
        dynamoDbTemplate.update(homework);
        return path;
    }

    @Override
    public void deleteHomeworkFile(String trainerId, String homeworkId) {
        Homework homework = requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        storageService.deleteFile(homework.getPath());
        homework.setPath("");
        dynamoDbTemplate.update(homework);
    }

    private Optional<Homework> requireOne(String trainerId, String homeworkId) {
        Homework homework = dynamoDbTemplate.load(Key.builder().partitionValue(trainerId).sortValue(homeworkId).build(), Homework.class);
        return Optional.ofNullable(homework);
    }

}
