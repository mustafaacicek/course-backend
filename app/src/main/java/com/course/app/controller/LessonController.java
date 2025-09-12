package com.course.app.controller;

import com.course.app.dto.LessonCreateRequest;
import com.course.app.dto.LessonDTO;
import com.course.app.dto.LessonUpdateRequest;
import com.course.app.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * Get all lessons
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<LessonDTO>> getAllLessons() {
        List<LessonDTO> lessons = lessonService.getAllLessons();
        return ResponseEntity.ok(lessons);
    }

    /**
     * Get lessons by course ID
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourseId(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Get lesson by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long id) {
        LessonDTO lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(lesson);
    }

    /**
     * Create a new lesson
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LessonDTO> createLesson(@Valid @RequestBody LessonCreateRequest request) {
        LessonDTO createdLesson = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    /**
     * Update an existing lesson
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LessonDTO> updateLesson(@PathVariable Long id, @RequestBody LessonUpdateRequest request) {
        LessonDTO updatedLesson = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Delete a lesson
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Count lessons by course ID
     */
    @GetMapping("/course/{courseId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Long> countLessonsByCourseId(@PathVariable Long courseId) {
        long count = lessonService.countLessonsByCourseId(courseId);
        return ResponseEntity.ok(count);
    }
}
