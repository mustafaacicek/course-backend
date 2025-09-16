package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {
    private Long courseId;
    private Long courseLocationId;
    private LocalDate attendanceDate;
    private List<StudentAttendanceRecord> studentRecords;
    private String notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceRecord {
        private Long studentId;
        private Boolean isPresent;
    }
}
