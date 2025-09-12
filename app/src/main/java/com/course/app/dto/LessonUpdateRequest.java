package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateRequest {
    
    private String name;
    
    private String description;
    
    private LocalDate date;
    
    private Integer defaultScore;
    
    private Long courseId;
}
