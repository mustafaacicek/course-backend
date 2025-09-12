package com.course.app.service;

import com.course.app.dto.*;
import com.course.app.entity.*;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private final StudentRepository studentRepository;
    private final LessonNoteRepository lessonNoteRepository;
    private final LessonNoteHistoryRepository lessonNoteHistoryRepository;

    /**
     * Get detailed information about a student including courses, lesson notes, and statistics
     */
    @Transactional(readOnly = true)
    public StudentDetailDTO getStudentDetails(Long studentId) {
        // Find the student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        // Create the DTO
        StudentDetailDTO detailDTO = new StudentDetailDTO();
        
        // Set basic student information
        detailDTO.setId(student.getId());
        detailDTO.setNationalId(student.getNationalId());
        detailDTO.setFirstName(student.getFirstName());
        detailDTO.setLastName(student.getLastName());
        detailDTO.setMotherName(student.getMotherName());
        detailDTO.setFatherName(student.getFatherName());
        detailDTO.setAddress(student.getAddress());
        detailDTO.setPhone(student.getPhone());
        detailDTO.setBirthDate(student.getBirthDate());
        detailDTO.setCreatedAt(student.getCreatedAt());
        detailDTO.setUpdatedAt(student.getUpdatedAt());
        
        // Set user account information
        if (student.getUser() != null) {
            detailDTO.setUserId(student.getUser().getId());
            detailDTO.setUsername(student.getUser().getUsername());
        }
        
        // Get course locations with their admins
        List<CourseLocationDTO> locationDTOs = new ArrayList<>();
        if (student.getCourseLocations() != null && !student.getCourseLocations().isEmpty()) {
            locationDTOs = student.getCourseLocations().stream()
                    .map(scl -> {
                        CourseLocation location = scl.getCourseLocation();
                        CourseLocationDTO locationDTO = new CourseLocationDTO();
                        locationDTO.setId(location.getId());
                        locationDTO.setName(location.getName());
                        locationDTO.setAddress(location.getAddress());
                        locationDTO.setPhone(location.getPhone());
                        locationDTO.setCreatedAt(location.getCreatedAt());
                        locationDTO.setUpdatedAt(location.getUpdatedAt());
                        
                        // Convert admins to DTOs
                        if (location.getAdmins() != null) {
                            List<UserSummaryDTO> adminDTOs = location.getAdmins().stream()
                                    .map(admin -> {
                                        UserSummaryDTO adminDTO = new UserSummaryDTO();
                                        adminDTO.setId(admin.getId());
                                        adminDTO.setUsername(admin.getUsername());
                                        adminDTO.setRole(admin.getRole());
                                        adminDTO.setFirstName(admin.getFirstName());
                                        adminDTO.setLastName(admin.getLastName());
                                        adminDTO.setPhone(admin.getPhone());
                                        return adminDTO;
                                    })
                                    .collect(Collectors.toList());
                            locationDTO.setAdmins(adminDTOs);
                        }
                        
                        return locationDTO;
                    })
                    .collect(Collectors.toList());
        }
        detailDTO.setCourseLocations(locationDTOs);
        
        // Get courses the student is enrolled in
        Set<Course> studentCourses = new HashSet<>();
        
        // Get courses from the student's course locations
        if (student.getCourseLocations() != null) {
            for (StudentCourseLocation scl : student.getCourseLocations()) {
                CourseLocation location = scl.getCourseLocation();
                
                // Add courses from the many-to-many relationship
                if (location.getCourses() != null) {
                    studentCourses.addAll(location.getCourses());
                }
                
                // Add courses from the legacy one-to-many relationship
                if (location.getLegacyCourses() != null) {
                    studentCourses.addAll(location.getLegacyCourses());
                }
            }
        }
        
        // Convert courses to DTOs
        List<CourseDTO> courseDTOs = studentCourses.stream()
                .map(course -> {
                    CourseDTO courseDTO = new CourseDTO();
                    courseDTO.setId(course.getId());
                    courseDTO.setName(course.getName());
                    courseDTO.setDescription(course.getDescription());
                    courseDTO.setStartDate(course.getStartDate());
                    courseDTO.setEndDate(course.getEndDate());
                    
                    // Get lessons for this course
                    if (course.getLessons() != null) {
                        List<LessonDTO> lessonDTOs = course.getLessons().stream()
                                .map(lesson -> {
                                    LessonDTO lessonDTO = new LessonDTO();
                                    lessonDTO.setId(lesson.getId());
                                    lessonDTO.setName(lesson.getName());
                                    lessonDTO.setDescription(lesson.getDescription());
                                    lessonDTO.setDate(lesson.getDate());
                                    lessonDTO.setDefaultScore(lesson.getDefaultScore());
                                    return lessonDTO;
                                })
                                .collect(Collectors.toList());
                        courseDTO.setLessons(lessonDTOs);
                    }
                    
                    return courseDTO;
                })
                .collect(Collectors.toList());
        detailDTO.setCourses(courseDTOs);
        
        // Get lesson notes for the student
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentId(studentId);
        List<LessonNoteDTO> lessonNoteDTOs = lessonNotes.stream()
                .map(note -> {
                    LessonNoteDTO noteDTO = new LessonNoteDTO();
                    noteDTO.setId(note.getId());
                    noteDTO.setScore(note.getScore());
                    noteDTO.setPassed(note.getPassed());
                    noteDTO.setRemark(note.getRemark());
                    noteDTO.setCreatedAt(note.getCreatedAt());
                    noteDTO.setUpdatedAt(note.getUpdatedAt());
                    
                    // Set lesson information
                    if (note.getLesson() != null) {
                        LessonDTO lessonDTO = new LessonDTO();
                        lessonDTO.setId(note.getLesson().getId());
                        lessonDTO.setName(note.getLesson().getName());
                        lessonDTO.setDescription(note.getLesson().getDescription());
                        lessonDTO.setDate(note.getLesson().getDate());
                        lessonDTO.setDefaultScore(note.getLesson().getDefaultScore());
                        
                        // Set course information
                        if (note.getLesson().getCourse() != null) {
                            CourseDTO courseDTO = new CourseDTO();
                            courseDTO.setId(note.getLesson().getCourse().getId());
                            courseDTO.setName(note.getLesson().getCourse().getName());
                            lessonDTO.setCourse(courseDTO);
                        }
                        
                        noteDTO.setLesson(lessonDTO);
                    }
                    
                    // Fetch and set lesson note history
                    List<LessonNoteHistory> histories = lessonNoteHistoryRepository.findByLessonNoteIdOrderByChangeDateDesc(note.getId());
                    if (histories != null && !histories.isEmpty()) {
                        List<LessonNoteHistoryDTO> historyDTOs = LessonNoteHistoryDTO.fromEntities(histories);
                        noteDTO.setHistory(historyDTOs);
                    }
                    
                    return noteDTO;
                })
                .collect(Collectors.toList());
        detailDTO.setLessonNotes(lessonNoteDTOs);
        
        // Calculate summary statistics
        detailDTO.setTotalCourses(courseDTOs.size());
        
        int totalLessons = lessonNoteDTOs.size();
        detailDTO.setTotalLessons(totalLessons);
        
        long passedLessons = lessonNoteDTOs.stream()
                .filter(note -> Boolean.TRUE.equals(note.getPassed()))
                .count();
        detailDTO.setPassedLessons((int) passedLessons);
        
        long failedLessons = lessonNoteDTOs.stream()
                .filter(note -> Boolean.FALSE.equals(note.getPassed()))
                .count();
        detailDTO.setFailedLessons((int) failedLessons);
        
        // Calculate average score
        double averageScore = lessonNoteDTOs.stream()
                .filter(note -> note.getScore() != null)
                .mapToInt(LessonNoteDTO::getScore)
                .average()
                .orElse(0.0);
        detailDTO.setAverageScore(Math.round(averageScore * 100.0) / 100.0); // Round to 2 decimal places
        
        // Set total score from student entity
        detailDTO.setTotalScore(student.getTotalScore());
        
        return detailDTO;
    }
}
