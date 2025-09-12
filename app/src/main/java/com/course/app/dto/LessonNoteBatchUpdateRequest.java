package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonNoteBatchUpdateRequest {
    private List<LessonNoteUpdateItem> notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonNoteUpdateItem {
        private Long studentId;
        private Long lessonId;
        private Integer score;
        private Boolean passed;
        private String remark;
    }
}
