package com.course.app.dto;

import com.course.app.entity.LessonNoteHistory;
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
public class LessonNoteHistoryDTO {
    private Long id;
    private Integer oldScore;
    private Boolean oldPassed;
    private String oldRemark;
    private LocalDateTime changeDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private UserDTO modifiedBy;
    
    // Static method to convert entity to DTO
    public static LessonNoteHistoryDTO fromEntity(LessonNoteHistory history) {
        if (history == null) return null;
        
        LessonNoteHistoryDTO dto = new LessonNoteHistoryDTO();
        dto.setId(history.getId());
        dto.setOldScore(history.getOldScore());
        dto.setOldPassed(history.getOldPassed());
        dto.setOldRemark(history.getOldRemark());
        dto.setChangeDate(history.getChangeDate());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setUpdatedAt(history.getUpdatedAt());
        
        // Set related entities
        if (history.getModifiedBy() != null) {
            dto.setModifiedBy(UserDTO.fromEntity(history.getModifiedBy()));
        }
        
        return dto;
    }
    
    // Convert list of entities to list of DTOs
    public static List<LessonNoteHistoryDTO> fromEntities(List<LessonNoteHistory> histories) {
        if (histories == null) return new ArrayList<>();
        
        return histories.stream()
                .map(LessonNoteHistoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
