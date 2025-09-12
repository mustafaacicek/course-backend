package com.course.app.controller;

import com.course.app.dto.StudentDetailDTO;
import com.course.app.service.AdminStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;
    
    /**
     * Get detailed information about a student including courses, lesson notes, and statistics
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<StudentDetailDTO> getStudentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminStudentService.getStudentDetails(id));
    }
}
