package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPerformanceDTO {
    
    // Basic student information
    private String nationalId;
    private String firstName;
    private String lastName;
    private String motherName;
    private String fatherName;
    private LocalDate birthDate;
    
    // Course location information
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
    
    // Performance metrics
    private double attendanceRate; // Percentage of lessons attended
    private String performanceLevel; // "Excellent", "Good", "Average", "Needs Improvement"
    private String teacherComment; // General comment about student's performance
    
    // Attendance information
    private Map<String, Object> attendanceData; // Attendance statistics and records
}
