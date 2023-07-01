package com.example.awsdemo.service;

import com.example.awsdemo.entity.Homework;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

public interface HomeworkService {
    Homework getHomework(String trainerId, String homeworkId);

    List<Homework> getHomeworkByTrainerId(String trainerId);

    String createHomework(Homework homework);

    void deleteHomework(String trainerId, String homeworkId);

    String getHomeworkFile(String trainerId, String homeworkId);

    String uploadHomeworkFile(String trainerId, String homeworkId, MultipartFile file);

    void deleteHomeworkFile(String trainerId, String homeworkId);
}
