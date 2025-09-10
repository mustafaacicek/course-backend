package com.course.app.controller;

import com.course.app.dto.CourseLocationCreateRequest;
import com.course.app.dto.CourseLocationResponse;
import com.course.app.dto.CourseLocationUpdateRequest;
import com.course.app.service.CourseLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class CourseLocationController {

    private final CourseLocationService courseLocationService;

    @GetMapping
    public ResponseEntity<List<CourseLocationResponse>> getAllLocations() {
        return ResponseEntity.ok(courseLocationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseLocationResponse> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(courseLocationService.getLocationById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CourseLocationResponse> createLocation(@Valid @RequestBody CourseLocationCreateRequest request) {
        CourseLocationResponse response = courseLocationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CourseLocationResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody CourseLocationUpdateRequest request) {
        return ResponseEntity.ok(courseLocationService.updateLocation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        courseLocationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/admins")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CourseLocationResponse> assignAdmins(
            @PathVariable Long id,
            @RequestBody List<Long> adminIds) {
        return ResponseEntity.ok(courseLocationService.assignAdminsToLocation(id, adminIds));
    }

    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<CourseLocationResponse>> getLocationsByAdminId(@PathVariable Long adminId) {
        return ResponseEntity.ok(courseLocationService.getLocationsByAdminId(adminId));
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseLocationResponse>> getLocationsForCurrentAdmin() {
        return ResponseEntity.ok(courseLocationService.getLocationsForCurrentAdmin());
    }
}
