package com.course.app.dto;

import com.course.app.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate date;
    private Integer defaultScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private CourseDTO course;
    
    // Static method to convert entity to DTO
    public static LessonDTO fromEntity(Lesson lesson) {
        if (lesson == null) return null;
        
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setName(lesson.getName());
        dto.setDescription(lesson.getDescription());
        dto.setDate(lesson.getDate());
        dto.setDefaultScore(lesson.getDefaultScore());
        dto.setCreatedAt(lesson.getCreatedAt());
        dto.setUpdatedAt(lesson.getUpdatedAt());
        
        // Set related entities
        if (lesson.getCourse() != null) {
            dto.setCourse(CourseDTO.fromEntity(lesson.getCourse()));
        }
        
        return dto;
    }
    
    // Convert list of entities to list of DTOs
    public static List<LessonDTO> fromEntities(List<Lesson> lessons) {
        if (lessons == null) return new ArrayList<>();
        
        List<LessonDTO> dtos = new ArrayList<>();
        for (Lesson lesson : lessons) {
            dtos.add(fromEntity(lesson));
        }
        
        return dtos;
    }
}
