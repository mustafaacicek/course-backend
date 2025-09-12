package com.course.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateRequestWithMultipleCourses {
    
    @NotBlank(message = "Lesson name is required")
    @Size(min = 3, max = 100, message = "Lesson name must be between 3 and 100 characters")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Date is required")
    private String date;
    
    private Integer defaultScore;
    
    @NotEmpty(message = "At least one course must be selected")
    private List<Long> courseIds;
}
