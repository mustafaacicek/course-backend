package com.course.app.dto;

import com.course.app.entity.LessonNote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonNoteDTO {
    private Long id;
    private Integer score;
    private Boolean passed;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private StudentDTO student;
    private LessonDTO lesson;
    private List<LessonNoteHistoryDTO> history;
    
    // Static method to convert entity to DTO
    public static LessonNoteDTO fromEntity(LessonNote lessonNote) {
        if (lessonNote == null) return null;
        
        LessonNoteDTO dto = new LessonNoteDTO();
        dto.setId(lessonNote.getId());
        dto.setScore(lessonNote.getScore());
        dto.setPassed(lessonNote.getPassed());
        dto.setRemark(lessonNote.getRemark());
        dto.setCreatedAt(lessonNote.getCreatedAt());
        dto.setUpdatedAt(lessonNote.getUpdatedAt());
        
        // Set related entities
        if (lessonNote.getStudent() != null) {
            dto.setStudent(StudentDTO.fromEntity(lessonNote.getStudent()));
        }
        
        if (lessonNote.getLesson() != null) {
            dto.setLesson(LessonDTO.fromEntity(lessonNote.getLesson()));
        }
        
        return dto;
    }
    
    // Convert list of entities to list of DTOs
    public static List<LessonNoteDTO> fromEntities(List<LessonNote> lessonNotes) {
        if (lessonNotes == null) return new ArrayList<>();
        
        return lessonNotes.stream()
                .map(LessonNoteDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
