package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateRequest {
    
    @NotBlank(message = "Lesson name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Lesson date is required")
    private LocalDate date;
    
    private Integer defaultScore;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
}
