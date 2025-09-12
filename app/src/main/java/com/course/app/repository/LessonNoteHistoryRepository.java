package com.course.app.repository;

import com.course.app.entity.LessonNoteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonNoteHistoryRepository extends JpaRepository<LessonNoteHistory, Long> {
    
    /**
     * Delete all history entries for a specific lesson note
     */
    void deleteByLessonNoteId(Long lessonNoteId);
    
    /**
     * Find all history entries for a specific lesson note
     */
    List<LessonNoteHistory> findByLessonNoteId(Long lessonNoteId);
    
    /**
     * Find all history entries modified by a specific user
     */
    List<LessonNoteHistory> findByModifiedById(Long userId);
    
    /**
     * Find all history entries for a specific lesson note ordered by change date descending
     */
    List<LessonNoteHistory> findByLessonNoteIdOrderByChangeDateDesc(Long lessonNoteId);
    
    /**
     * Count history entries for a specific lesson note
     */
    long countByLessonNoteId(Long lessonNoteId);
}
