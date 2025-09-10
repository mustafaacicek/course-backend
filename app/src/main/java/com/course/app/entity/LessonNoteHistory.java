package com.course.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_note_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonNoteHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer oldScore;
    
    private Boolean oldPassed;
    
    private String oldRemark;
    
    private LocalDateTime changeDate;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Relations
    @ManyToOne
    @JoinColumn(name = "lesson_note_id")
    private LessonNote lessonNote;
    
    @ManyToOne
    @JoinColumn(name = "modified_by_id")
    private User modifiedBy;
}
