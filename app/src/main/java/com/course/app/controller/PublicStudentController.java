package com.course.app.controller;

import com.course.app.dto.StudentPerformanceDTO;
import com.course.app.service.PublicStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/students")
@RequiredArgsConstructor
public class PublicStudentController {

    private final PublicStudentService publicStudentService;
    
    /**
     * Get student performance details by national ID
     * This endpoint is publicly accessible for parents to check their children's performance
     */
    @GetMapping("/performance/{nationalId}")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(@PathVariable String nationalId) {
        StudentPerformanceDTO performance = publicStudentService.getStudentPerformanceByNationalId(nationalId);
        return ResponseEntity.ok(performance);
    }
}
