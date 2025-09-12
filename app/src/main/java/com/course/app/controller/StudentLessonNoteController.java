package com.course.app.controller;

import com.course.app.dto.LessonNoteBatchUpdateRequest;
import com.course.app.dto.LessonNoteDTO;
import com.course.app.dto.StudentLessonNoteDTO;
import com.course.app.service.StudentLessonNoteService;
import com.course.app.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/student-lesson-notes")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class StudentLessonNoteController {

    private final StudentLessonNoteService studentLessonNoteService;

    @Autowired
    public StudentLessonNoteController(StudentLessonNoteService studentLessonNoteService) {
        this.studentLessonNoteService = studentLessonNoteService;
    }

    /**
     * Get students with their lesson notes for a specific course and lesson
     */
    @GetMapping("/course/{courseId}/lesson/{lessonId}")
    public ResponseEntity<List<StudentLessonNoteDTO>> getStudentsWithLessonNotes(
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        List<StudentLessonNoteDTO> students = studentLessonNoteService.getStudentsWithLessonNotes(courseId, lessonId);
        return ResponseEntity.ok(students);
    }

    /**
     * Update a single student's lesson note
     */
    @PutMapping("/student/{studentId}/lesson/{lessonId}")
    public ResponseEntity<LessonNoteDTO> updateStudentLessonNote(
            @PathVariable Long studentId,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonNoteBatchUpdateRequest.LessonNoteUpdateItem request) {
        
        // Get current user ID from security context
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // If current user ID is null, use a default value (1L for admin)
        if (currentUserId == null) {
            currentUserId = 1L; // Default to admin user ID
        }
        
        LessonNoteDTO updatedNote = studentLessonNoteService.updateStudentLessonNote(
            studentId, lessonId, request, currentUserId);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Batch update lesson notes
     */
    @PostMapping("/batch-update")
    public ResponseEntity<List<LessonNoteDTO>> batchUpdateLessonNotes(
            @Valid @RequestBody LessonNoteBatchUpdateRequest request) {
        
        // Get current user ID from security context
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // If current user ID is null, use a default value (1L for admin)
        if (currentUserId == null) {
            currentUserId = 1L; // Default to admin user ID
        }
        
        List<LessonNoteDTO> updatedNotes = studentLessonNoteService.batchUpdateLessonNotes(request, currentUserId);
        return ResponseEntity.ok(updatedNotes);
    }
}
