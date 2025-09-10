package com.course.app.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    
    private Long id;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String motherName;
    private String fatherName;
    private String address;
    private String phone;
    private LocalDate birthDate;
    private Long userId;
    private String username;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private List<CourseLocationDTO> courseLocations;
}
