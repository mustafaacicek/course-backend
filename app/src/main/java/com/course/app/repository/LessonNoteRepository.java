package com.course.app.repository;

import com.course.app.entity.LessonNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonNoteRepository extends JpaRepository<LessonNote, Long> {
    
    /**
     * Delete all lesson notes for a specific student
     */
    void deleteByStudentId(Long studentId);
    
    /**
     * Find all lesson notes for a specific student
     */
    List<LessonNote> findByStudentId(Long studentId);
    
    /**
     * Find all lesson notes for a specific lesson
     */
    List<LessonNote> findByLessonId(Long lessonId);
    
    /**
     * Find a specific lesson note for a student and lesson
     */
    LessonNote findByStudentIdAndLessonId(Long studentId, Long lessonId);
    
    /**
     * Find all lesson notes for a specific course (via lesson's course)
     */
    List<LessonNote> findByLessonCourseId(Long courseId);
    
    /**
     * Count lesson notes by lesson ID
     */
    long countByLessonId(Long lessonId);
    
    /**
     * Count lesson notes by student ID
     */
    long countByStudentId(Long studentId);
    
    /**
     * Find all passed lesson notes for a student
     */
    List<LessonNote> findByStudentIdAndPassedTrue(Long studentId);
    
    /**
     * Find all failed lesson notes for a student
     */
    List<LessonNote> findByStudentIdAndPassedFalse(Long studentId);
    
    /**
     * Count lesson notes by location ID
     * @param locationId Location ID
     * @return Number of lesson notes in the location
     */
    @Query("SELECT COUNT(ln) FROM LessonNote ln WHERE ln.lesson.course.courseLocation.id = :locationId")
    int countByLocationId(@Param("locationId") Long locationId);
}
