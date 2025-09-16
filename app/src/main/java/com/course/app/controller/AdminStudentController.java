package com.course.app.controller;

import com.course.app.dto.StudentDetailDTO;
import com.course.app.service.AdminStudentService;
import com.course.app.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;
    private final AttendanceService attendanceService;
    
    /**
     * Get detailed information about a student including courses, lesson notes, and statistics
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<StudentDetailDTO> getStudentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminStudentService.getStudentDetails(id));
    }
    
    /**
     * Get attendance details for a student
     */
    @GetMapping("/{id}/attendance")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceDetails(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceDetails(id));
    }
}
