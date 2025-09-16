package com.course.app.controller;

import com.course.app.dto.StudentDTO;
import com.course.app.service.StudentService;
import com.course.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course-locations")
@RequiredArgsConstructor
public class CourseLocationStudentController {

    private final StudentService studentService;

    /**
     * Get students for a specific location
     * Admin users can only access their own locations
     */
    @GetMapping("/{locationId}/students")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<StudentDTO>> getStudentsByLocationId(@PathVariable Long locationId) {
        String currentRole = SecurityUtils.getCurrentUserRole();
        
        if ("ROLE_ADMIN".equals(currentRole)) {
            // Admin kullanıcısı için sadece kendi lokasyonlarındaki öğrencileri getir
            Long currentUserId = SecurityUtils.getCurrentUserId();
            
            // Admin'in bu lokasyona erişim yetkisi var mı kontrol et
            boolean hasAccess = studentService.adminHasAccessToLocation(currentUserId, locationId);
            
            if (!hasAccess) {
                throw new AccessDeniedException("Bu lokasyona erişim yetkiniz yok: " + locationId);
            }
            
            // Erişim yetkisi varsa öğrencileri getir
            return ResponseEntity.ok(studentService.getStudentsByLocationId(locationId));
        } else {
            // Superadmin için tüm öğrencileri getir
            return ResponseEntity.ok(studentService.getStudentsByLocationId(locationId));
        }
    }
}
