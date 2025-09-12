package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailDTO {
    
    // Basic student information
    private Long id;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String motherName;
    private String fatherName;
    private String address;
    private String phone;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User account information
    private Long userId;
    private String username;
    
    // Course locations with their admins
    private List<CourseLocationDTO> courseLocations;
    
    // Courses the student is enrolled in
    private List<CourseDTO> courses;
    
    // Lesson notes for the student
    private List<LessonNoteDTO> lessonNotes;
    
    // Summary statistics
    private int totalCourses;
    private int totalLessons;
    private int passedLessons;
    private int failedLessons;
    private double averageScore;
    private Integer totalScore;
}
