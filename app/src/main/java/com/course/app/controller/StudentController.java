package com.course.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.course.app.dto.StudentCreateRequest;
import com.course.app.dto.StudentDTO;
import com.course.app.dto.StudentUpdateRequest;
import com.course.app.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        System.out.println("DEBUG StudentController: getAllStudents endpoint called");
        
        // Güvenlik bağlamından mevcut kullanıcı ID'sini ve rolünü al
        Long currentUserId = com.course.app.util.SecurityUtils.getCurrentUserId();
        String currentUserRole = com.course.app.util.SecurityUtils.getCurrentUserRole();
        
        System.out.println("DEBUG StudentController: currentUserId = " + currentUserId);
        System.out.println("DEBUG StudentController: currentUserRole = " + currentUserRole);
        
        // Eğer kullanıcı ADMIN ise, sadece kendi lokasyonlarındaki öğrencileri getir
        if (currentUserId != null && "ROLE_ADMIN".equals(currentUserRole)) {
            System.out.println("DEBUG StudentController: Admin user detected, calling getStudentsByAdminId");
            List<StudentDTO> adminStudents = studentService.getStudentsByAdminId(currentUserId);
            System.out.println("DEBUG StudentController: Admin students count = " + adminStudents.size());
            return ResponseEntity.ok(adminStudents);
        }
        
        // SUPERADMIN için tüm öğrencileri getir
        System.out.println("DEBUG StudentController: Superadmin or other role, calling getAllStudents");
        List<StudentDTO> allStudents = studentService.getAllStudents();
        System.out.println("DEBUG StudentController: All students count = " + allStudents.size());
        return ResponseEntity.ok(allStudents);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }
    
    @GetMapping("/national-id/{nationalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<StudentDTO> getStudentByNationalId(@PathVariable String nationalId) {
        return ResponseEntity.ok(studentService.getStudentByNationalId(nationalId));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        StudentDTO createdStudent = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        StudentDTO updatedStudent = studentService.updateStudent(id, request);
        return ResponseEntity.ok(updatedStudent);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<?> getStudentAdminId(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentAdmin(id));
    }
    
    @PutMapping("/{id}/teacher-comment")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<StudentDTO> updateTeacherComment(@PathVariable Long id, @RequestBody String teacherComment) {
        StudentDTO updatedStudent = studentService.updateTeacherComment(id, teacherComment);
        return ResponseEntity.ok(updatedStudent);
    }
}
