package com.example.awsdemo.service;

import com.example.awsdemo.entity.Homework;
import com.example.awsdemo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Homework> homeworkTable;
    private final StorageService storageService;

    @Autowired
    public HomeworkServiceImpl(DynamoDbEnhancedClient enhancedClient, StorageService storageService) {
        this.enhancedClient = enhancedClient;
        this.homeworkTable = enhancedClient.table("Homework", TableSchema.fromBean(Homework.class));
        this.storageService = storageService;
    }

    @Override
    public Homework getHomework(String trainerId, String homeworkId) {
        Homework homework = homeworkTable.getItem(Key.builder().partitionValue(trainerId).sortValue(homeworkId).build());
        if (homework == null) {
            throw new ResourceNotFoundException("Homework not found");
        }
        return homework;
    }

    @Override
    public List<Homework> getHomeworkByTrainerId(String trainerId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(trainerId).build());

        // Query DynamoDb
        Iterable<Homework> results = homeworkTable.query(queryConditional).items();

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
        homeworkTable.putItem(homework);
        // return uuid
        return homeworkId;
    }

    @Override
    public void deleteHomework(String trainerId, String homeworkId) {
        Homework homework = requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        if (homework.getPath() != null && !homework.getPath().isEmpty()) {
            storageService.deleteFile(homework.getPath());
        }
        homeworkTable.deleteItem(Key.builder().partitionValue(trainerId).sortValue(homeworkId).build());
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
        homeworkTable.updateItem(homework);
        return path;
    }

    @Override
    public void deleteHomeworkFile(String trainerId, String homeworkId) {
        Homework homework = requireOne(trainerId, homeworkId).orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        storageService.deleteFile(homework.getPath());
        homework.setPath("");
        homeworkTable.updateItem(homework);
    }

    private Optional<Homework> requireOne(String trainerId, String homeworkId) {
        Homework homework = homeworkTable.getItem(Key.builder().partitionValue(trainerId).sortValue(homeworkId).build());
        return Optional.ofNullable(homework);
    }

}
