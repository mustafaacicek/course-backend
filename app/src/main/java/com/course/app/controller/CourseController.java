package com.course.app.controller;

import com.course.app.dto.CourseCreateRequest;
import com.course.app.dto.CourseDTO;
import com.course.app.dto.CourseUpdateRequest;
import com.course.app.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<CourseDTO>> getCoursesForCurrentAdmin() {
        return ResponseEntity.ok(courseService.getCoursesForCurrentAdmin());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        CourseDTO createdCourse = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateRequest request) {
        CourseDTO updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updatedCourse);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    
    // Superadmin specific endpoints for multi-location operations
    
    /**
     * Get courses for a specific location
     */
    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<CourseDTO>> getCoursesByLocationId(@PathVariable Long locationId) {
        return ResponseEntity.ok(courseService.getCoursesByLocationId(locationId));
    }
    
    /**
     * Add a course to a specific location
     */
    @PostMapping("/{courseId}/locations/{locationId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<CourseDTO> addCourseToLocation(
            @PathVariable Long courseId,
            @PathVariable Long locationId) {
        CourseDTO updatedCourse = courseService.addCourseToLocation(courseId, locationId);
        return ResponseEntity.ok(updatedCourse);
    }
    
    /**
     * Remove a course from a specific location
     */
    @DeleteMapping("/{courseId}/locations/{locationId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<CourseDTO> removeCourseFromLocation(
            @PathVariable Long courseId,
            @PathVariable Long locationId) {
        CourseDTO updatedCourse = courseService.removeCourseFromLocation(courseId, locationId);
        return ResponseEntity.ok(updatedCourse);
    }
}
