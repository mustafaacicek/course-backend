package com.course.app.controller;

import com.course.app.dto.LessonCreateRequest;
import com.course.app.dto.LessonCreateRequestWithMultipleCourses;
import com.course.app.dto.LessonDTO;
import com.course.app.dto.LessonUpdateRequest;
import com.course.app.service.LessonService;
import com.course.app.service.SuperadminLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin/lessons")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperadminLessonController {

    private final LessonService lessonService;
    private final SuperadminLessonService superadminLessonService;

    @Autowired
    public SuperadminLessonController(LessonService lessonService, SuperadminLessonService superadminLessonService) {
        this.lessonService = lessonService;
        this.superadminLessonService = superadminLessonService;
    }

    /**
     * Get all lessons (superadmin access)
     */
    @GetMapping
    public ResponseEntity<List<LessonDTO>> getAllLessons() {
        List<LessonDTO> lessons = lessonService.getAllLessons();
        return ResponseEntity.ok(lessons);
    }

    /**
     * Get lessons by course ID (superadmin access)
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourseId(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Get lesson by ID (superadmin access)
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long id) {
        LessonDTO lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(lesson);
    }

    /**
     * Create a new lesson (superadmin access)
     */
    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(@Valid @RequestBody LessonCreateRequest request) {
        LessonDTO createdLesson = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }
    
    /**
     * Create a new lesson and assign it to multiple courses (superadmin access)
     */
    @PostMapping("/multiple-courses")
    public ResponseEntity<List<LessonDTO>> createLessonForMultipleCourses(
            @Valid @RequestBody LessonCreateRequestWithMultipleCourses request) {
        List<LessonDTO> createdLessons = superadminLessonService.createLessonForMultipleCourses(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLessons);
    }

    /**
     * Update an existing lesson (superadmin access)
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonDTO> updateLesson(@PathVariable Long id, @RequestBody LessonUpdateRequest request) {
        LessonDTO updatedLesson = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Delete a lesson (superadmin access)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Bulk delete lessons (superadmin access)
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkDeleteLessons(@RequestBody List<Long> lessonIds) {
        int deletedCount = lessonService.bulkDeleteLessons(lessonIds);
        Map<String, Object> response = Map.of(
            "deletedCount", deletedCount,
            "totalCount", lessonIds.size(),
            "message", String.format("%d of %d lessons deleted successfully", deletedCount, lessonIds.size())
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Bulk move lessons to another course (superadmin access)
     */
    @PutMapping("/bulk/move")
    public ResponseEntity<Map<String, Object>> bulkMoveLessons(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Long> lessonIds = (List<Long>) request.get("lessonIds");
        Long targetCourseId = Long.valueOf(request.get("targetCourseId").toString());
        
        int movedCount = lessonService.bulkMoveLessonsToCourse(lessonIds, targetCourseId);
        
        Map<String, Object> response = Map.of(
            "movedCount", movedCount,
            "totalCount", lessonIds.size(),
            "targetCourseId", targetCourseId,
            "message", String.format("%d of %d lessons moved successfully", movedCount, lessonIds.size())
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Count lessons by course ID (superadmin access)
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countLessonsByCourseId(@PathVariable Long courseId) {
        long count = lessonService.countLessonsByCourseId(courseId);
        return ResponseEntity.ok(count);
    }
}
