package com.course.app.service;

import com.course.app.dto.AttendanceDTO;
import com.course.app.dto.AttendanceRequest;
import com.course.app.entity.*;
import com.course.app.repository.AttendanceRepository;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.CourseRepository;
import com.course.app.repository.StudentRepository;
import com.course.app.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseLocationRepository courseLocationRepository;

    @Transactional
    public List<AttendanceDTO> saveAttendanceRecords(AttendanceRequest request) {
        // Get current user
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = getUserById(currentUserId);
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        
        CourseLocation courseLocation = courseLocationRepository.findById(request.getCourseLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Course location not found"));
        
        List<Attendance> attendances = new ArrayList<>();
        
        for (AttendanceRequest.StudentAttendanceRecord record : request.getStudentRecords()) {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + record.getStudentId()));
            
            // Check if attendance record already exists for this student, course, and date
            Optional<Attendance> existingAttendance = attendanceRepository
                    .findByStudentAndCourseAndAttendanceDate(student, course, request.getAttendanceDate());
            
            Attendance attendance;
            if (existingAttendance.isPresent()) {
                // Update existing record
                attendance = existingAttendance.get();
                attendance.setIsPresent(record.getIsPresent());
                attendance.setNotes(request.getNotes());
            } else {
                // Create new record
                attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setCourse(course);
                attendance.setCourseLocation(courseLocation);
                attendance.setAttendanceDate(request.getAttendanceDate());
                attendance.setIsPresent(record.getIsPresent());
                attendance.setNotes(request.getNotes());
                attendance.setCreatedBy(currentUser);
            }
            
            attendances.add(attendance);
        }
        
        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendances);
        return savedAttendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceDTO> getAttendanceByLocationAndDate(Long locationId, LocalDate date) {
        // Get current user role and ID
        String currentRole = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        CourseLocation location = courseLocationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Course location not found"));
        
        // Check if admin has access to this location
        if ("ROLE_ADMIN".equals(currentRole)) {
            boolean hasAccess = false;
            
            // Check if admin is assigned to this location
            if (location.getAdmins() != null) {
                hasAccess = location.getAdmins().stream()
                        .anyMatch(admin -> admin.getId().equals(currentUserId));
            }
            
            if (!hasAccess) {
                throw new org.springframework.security.access.AccessDeniedException("Bu lokasyona erişim yetkiniz yok: " + locationId);
            }
        }
        
        List<Attendance> attendances = attendanceRepository.findByCourseLocationAndAttendanceDate(location, date);
        return attendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceDTO> getAttendanceByUserLocationsAndDate(LocalDate date) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<Long> locationIds = courseLocationRepository.findAllByAdminId(currentUserId)
                .stream()
                .map(CourseLocation::getId)
                .collect(Collectors.toList());
        
        if (locationIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Attendance> attendances = attendanceRepository.findByLocationIdsAndDate(locationIds, date);
        return attendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<LocalDate> getAttendanceDatesByUserLocations() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<Long> locationIds = courseLocationRepository.findAllByAdminId(currentUserId)
                .stream()
                .map(CourseLocation::getId)
                .collect(Collectors.toList());
        
        if (locationIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return attendanceRepository.findDistinctAttendanceDatesByLocationIds(locationIds);
    }
    
    public Map<String, Long> getStudentAttendanceStats(Long studentId) {
        Long presentDays = attendanceRepository.countPresentDaysByStudentId(studentId);
        Long absentDays = attendanceRepository.countAbsentDaysByStudentId(studentId);
        
        return Map.of(
            "presentDays", presentDays,
            "absentDays", absentDays,
            "totalDays", presentDays + absentDays
        );
    }
    
    /**
     * Get detailed attendance records for a student
     * @param studentId The student ID
     * @return Map containing attendance statistics and records grouped by course
     */
    public Map<String, Object> getStudentAttendanceDetails(Long studentId) {
        // Get all attendance records for the student
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        
        // Convert to DTOs
        List<AttendanceDTO> attendanceDTOs = attendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Calculate overall statistics
        long totalDays = attendanceDTOs.size();
        long presentDays = attendanceDTOs.stream().filter(AttendanceDTO::getIsPresent).count();
        long absentDays = totalDays - presentDays;
        
        // Group by course
        Map<Long, List<AttendanceDTO>> attendanceByCoursesMap = attendanceDTOs.stream()
                .collect(Collectors.groupingBy(AttendanceDTO::getCourseId));
        
        // Create course-based attendance records
        List<Map<String, Object>> courseAttendance = new ArrayList<>();
        
        attendanceByCoursesMap.forEach((courseId, records) -> {
            // Skip if no records
            if (records.isEmpty()) {
                return;
            }
            
            // Get course name from first record
            String courseName = records.get(0).getCourseName();
            
            // Calculate course statistics
            long courseTotalDays = records.size();
            long coursePresentDays = records.stream().filter(AttendanceDTO::getIsPresent).count();
            long courseAbsentDays = courseTotalDays - coursePresentDays;
            double attendanceRate = courseTotalDays > 0 ? (double) coursePresentDays / courseTotalDays * 100 : 0;
            
            // Sort records by date (newest first)
            records.sort((a, b) -> b.getAttendanceDate().compareTo(a.getAttendanceDate()));
            
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("courseId", courseId);
            courseData.put("courseName", courseName);
            courseData.put("totalDays", courseTotalDays);
            courseData.put("presentDays", coursePresentDays);
            courseData.put("absentDays", courseAbsentDays);
            courseData.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0); // Round to 2 decimal places
            courseData.put("records", records);
            
            courseAttendance.add(courseData);
        });
        
        // Sort courses by name
        courseAttendance.sort(Comparator.comparing(m -> (String) m.get("courseName")));
        
        // Create result map
        Map<String, Object> result = new HashMap<>();
        result.put("totalDays", totalDays);
        result.put("presentDays", presentDays);
        result.put("absentDays", absentDays);
        result.put("attendanceRate", totalDays > 0 ? Math.round((double) presentDays / totalDays * 100 * 100.0) / 100.0 : 0);
        result.put("courseAttendance", courseAttendance);
        
        return result;
    }
    
    private AttendanceDTO convertToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setStudentId(attendance.getStudent().getId());
        dto.setStudentName(attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName());
        dto.setCourseId(attendance.getCourse().getId());
        dto.setCourseName(attendance.getCourse().getName());
        dto.setCourseLocationId(attendance.getCourseLocation().getId());
        dto.setCourseLocationName(attendance.getCourseLocation().getName());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setIsPresent(attendance.getIsPresent());
        dto.setNotes(attendance.getNotes());
        
        if (attendance.getCreatedBy() != null) {
            dto.setCreatedById(attendance.getCreatedBy().getId());
            dto.setCreatedByName(attendance.getCreatedBy().getFirstName() + " " + attendance.getCreatedBy().getLastName());
        }
        
        return dto;
    }
    
    // Helper method to get user by ID
    private User getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("User ID is null");
        }
        
        // In a real application, you would inject UserRepository and use it here
        // For simplicity, we're using a dummy implementation
        User user = new User();
        user.setId(userId);
        return user;
    }
}
