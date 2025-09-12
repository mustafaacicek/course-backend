package com.course.app.controller;

import com.course.app.dto.LessonNoteCreateRequest;
import com.course.app.dto.LessonNoteDTO;
import com.course.app.dto.LessonNoteHistoryDTO;
import com.course.app.dto.LessonNoteUpdateRequest;
import com.course.app.service.LessonNoteService;
import com.course.app.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/lesson-notes")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class AdminLessonNoteController {

    private final LessonNoteService lessonNoteService;

    @Autowired
    public AdminLessonNoteController(LessonNoteService lessonNoteService) {
        this.lessonNoteService = lessonNoteService;
    }

    /**
     * Get all lesson notes
     */
    @GetMapping
    public ResponseEntity<List<LessonNoteDTO>> getAllLessonNotes() {
        String currentRole = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if ("ROLE_ADMIN".equals(currentRole)) {
            // Admin kullanıcısı için sadece kendi lokasyonlarındaki öğrencilerin notlarını getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesForAdmin(currentUserId);
            return ResponseEntity.ok(lessonNotes);
        } else {
            // Superadmin için tüm notları getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getAllLessonNotes();
            return ResponseEntity.ok(lessonNotes);
        }
    }

    /**
     * Get lesson notes by lesson ID
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonNoteDTO>> getLessonNotesByLessonId(@PathVariable Long lessonId) {
        String currentRole = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if ("ROLE_ADMIN".equals(currentRole)) {
            // Admin kullanıcısı için sadece kendi lokasyonlarındaki öğrencilerin notlarını getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesByLessonIdForAdmin(lessonId, currentUserId);
            return ResponseEntity.ok(lessonNotes);
        } else {
            // Superadmin için tüm notları getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesByLessonId(lessonId);
            return ResponseEntity.ok(lessonNotes);
        }
    }

    /**
     * Get lesson notes by student ID
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LessonNoteDTO>> getLessonNotesByStudentId(@PathVariable Long studentId) {
        String currentRole = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if ("ROLE_ADMIN".equals(currentRole)) {
            // Admin kullanıcısı için sadece kendi lokasyonlarındaki öğrencilerin notlarını getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesByStudentIdForAdmin(studentId, currentUserId);
            return ResponseEntity.ok(lessonNotes);
        } else {
            // Superadmin için tüm notları getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesByStudentId(studentId);
            return ResponseEntity.ok(lessonNotes);
        }
    }

    /**
     * Get lesson notes by course ID
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonNoteDTO>> getLessonNotesByCourseId(@PathVariable Long courseId) {
        String currentRole = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if ("ROLE_ADMIN".equals(currentRole)) {
            // Admin kullanıcısı için sadece kendi lokasyonlarındaki öğrencilerin notlarını getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesByCourseIdForAdmin(courseId, currentUserId);
            return ResponseEntity.ok(lessonNotes);
        } else {
            // Superadmin için tüm notları getir
            List<LessonNoteDTO> lessonNotes = lessonNoteService.getLessonNotesByCourseId(courseId);
            return ResponseEntity.ok(lessonNotes);
        }
    }

    /**
     * Get passed lesson notes by student ID
     */
    @GetMapping("/student/{studentId}/passed")
    public ResponseEntity<List<LessonNoteDTO>> getPassedLessonNotesByStudentId(@PathVariable Long studentId) {
        List<LessonNoteDTO> lessonNotes = lessonNoteService.getPassedLessonNotesByStudentId(studentId);
        return ResponseEntity.ok(lessonNotes);
    }

    /**
     * Get failed lesson notes by student ID
     */
    @GetMapping("/student/{studentId}/failed")
    public ResponseEntity<List<LessonNoteDTO>> getFailedLessonNotesByStudentId(@PathVariable Long studentId) {
        List<LessonNoteDTO> lessonNotes = lessonNoteService.getFailedLessonNotesByStudentId(studentId);
        return ResponseEntity.ok(lessonNotes);
    }

    /**
     * Get lesson note by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonNoteDTO> getLessonNoteById(@PathVariable Long id) {
        LessonNoteDTO lessonNote = lessonNoteService.getLessonNoteById(id);
        return ResponseEntity.ok(lessonNote);
    }

    /**
     * Create a new lesson note
     */
    @PostMapping
    public ResponseEntity<LessonNoteDTO> createLessonNote(@Valid @RequestBody LessonNoteCreateRequest request) {
        LessonNoteDTO createdLessonNote = lessonNoteService.createLessonNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLessonNote);
    }

    /**
     * Update an existing lesson note
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonNoteDTO> updateLessonNote(
            @PathVariable Long id, 
            @RequestBody LessonNoteUpdateRequest request) {
        
        // Get current user ID from security context
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        LessonNoteDTO updatedLessonNote = lessonNoteService.updateLessonNote(id, request, currentUserId);
        return ResponseEntity.ok(updatedLessonNote);
    }

    /**
     * Delete a lesson note
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLessonNote(@PathVariable Long id) {
        lessonNoteService.deleteLessonNote(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get history for a lesson note
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<LessonNoteHistoryDTO>> getLessonNoteHistory(@PathVariable Long id) {
        List<LessonNoteHistoryDTO> history = lessonNoteService.getLessonNoteHistory(id);
        return ResponseEntity.ok(history);
    }
}
