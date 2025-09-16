package com.course.app.controller;

import com.course.app.dto.AttendanceDTO;
import com.course.app.dto.AttendanceRequest;
import com.course.app.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<AttendanceDTO>> saveAttendanceRecords(@RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.saveAttendanceRecords(request));
    }

    @GetMapping("/location/{locationId}/date/{date}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByLocationAndDate(
            @PathVariable Long locationId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByLocationAndDate(locationId, date));
    }

    @GetMapping("/my-locations/date/{date}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByUserLocationsAndDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByUserLocationsAndDate(date));
    }

    @GetMapping("/my-locations/dates")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<List<LocalDate>> getAttendanceDatesByUserLocations() {
        return ResponseEntity.ok(attendanceService.getAttendanceDatesByUserLocations());
    }

    @GetMapping("/student/{studentId}/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<Map<String, Long>> getStudentAttendanceStats(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceStats(studentId));
    }
}
