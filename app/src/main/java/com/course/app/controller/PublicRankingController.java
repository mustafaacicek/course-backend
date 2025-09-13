package com.course.app.controller;

import com.course.app.dto.StudentRankingDTO;
import com.course.app.service.StudentRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/rankings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicRankingController {

    private final StudentRankingService studentRankingService;

    @GetMapping("/top-students")
    public ResponseEntity<List<StudentRankingDTO>> getTopStudents(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(studentRankingService.getTopStudents(limit));
    }

    @GetMapping("/top-students/location/{locationId}")
    public ResponseEntity<List<StudentRankingDTO>> getTopStudentsByLocation(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(studentRankingService.getTopStudentsByLocation(locationId, limit));
    }
}
