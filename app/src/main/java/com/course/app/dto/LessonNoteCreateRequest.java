package com.course.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonNoteCreateRequest {
    
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score cannot be more than 100")
    private Integer score;
    
    private Boolean passed;
    
    private String remark;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Lesson ID is required")
    private Long lessonId;
}
