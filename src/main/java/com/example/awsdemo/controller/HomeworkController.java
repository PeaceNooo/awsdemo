package com.example.awsdemo.controller;

import com.example.awsdemo.entity.Homework;
import com.example.awsdemo.service.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/homeworks")
public class HomeworkController {

    private final HomeworkService homeworkService;

    @Autowired
    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @GetMapping("/{trainerId}/{homeworkId}")
    public ResponseEntity<Homework> getHomework(@PathVariable String trainerId, @PathVariable String homeworkId) {
        return ResponseEntity.ok(homeworkService.getHomework(trainerId, homeworkId));
    }

    @GetMapping("/{trainerId}")
    public ResponseEntity<List<Homework>> getHomeworkByTrainerId(@PathVariable String trainerId) {
        return ResponseEntity.ok(homeworkService.getHomeworkByTrainerId(trainerId));
    }

    @PostMapping("/{trainerId}")
    public ResponseEntity<String> createHomework(@PathVariable String trainerId, @RequestBody Homework homework) {
        homework.setTrainerId(trainerId);
        String homeworkId = homeworkService.createHomework(homework);

        // Build the URI of the created resource
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{homeworkId}")
                .buildAndExpand(homeworkId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{trainerId}/{homeworkId}")
    public ResponseEntity<Void> deleteHomework(@PathVariable String trainerId, @PathVariable String homeworkId) {
        homeworkService.deleteHomework(trainerId, homeworkId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{trainerId}/{homeworkId}/download")
    public ResponseEntity<String> getHomeworkFile(@PathVariable String trainerId, @PathVariable String homeworkId) {
        String path = homeworkService.getHomeworkFile(trainerId, homeworkId);
        // get URI from the path (homework file path), path = "https://s3.amazonaws.com/..."
        URI location = URI.create(path);
        // redirect to the homework file
        return ResponseEntity.status(302).location(location).build();
    }

    @PostMapping("/{trainerId}/{homeworkId}/upload")
    public ResponseEntity<Homework> uploadHomeworkFile(
            @PathVariable String trainerId,
            @PathVariable String homeworkId,
            @RequestParam("file") MultipartFile file) {

        String path = homeworkService.uploadHomeworkFile(trainerId, homeworkId, file);
        // get URI from the path (homework file path), path = "https://s3.amazonaws.com/..."
        URI location = URI.create(path);

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{trainerId}/{homeworkId}/delete")
    public ResponseEntity<Void> deleteHomeworkFile(@PathVariable String trainerId, @PathVariable String homeworkId) {
        homeworkService.deleteHomeworkFile(trainerId, homeworkId);
        return ResponseEntity.noContent().build();
    }

}
