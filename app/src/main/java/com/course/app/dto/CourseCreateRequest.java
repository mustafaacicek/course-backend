package com.course.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRequest {
    
    @NotBlank(message = "Kurs adı boş olamaz")
    private String name;
    
    private String description;
    
    @NotNull(message = "Başlangıç tarihi boş olamaz")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    // For backward compatibility with admin operations
    private Long courseLocationId;
    
    // For superadmin operations with multiple locations
    private List<Long> courseLocationIds;
}
