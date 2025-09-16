package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Long courseLocationId;
    private String courseLocationName;
    private LocalDate attendanceDate;
    private Boolean isPresent;
    private String notes;
    private Long createdById;
    private String createdByName;
}
