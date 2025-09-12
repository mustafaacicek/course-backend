package com.course.app.repository;

import com.course.app.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentLessonNoteRepository extends StudentRepository {
    
    /**
     * Find students by course location ID
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.courseLocations scl WHERE scl.courseLocation.id = :courseLocationId")
    List<Student> findByCourseLocationId(@Param("courseLocationId") Long courseLocationId);
    
    /**
     * Find students by course ID
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.courseLocations scl JOIN scl.courseLocation cl JOIN cl.courses c WHERE c.id = :courseId")
    List<Student> findByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Find students by course ID with their lesson notes for a specific lesson
     */
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.lessonNotes ln LEFT JOIN FETCH ln.lesson l " +
           "JOIN s.courseLocations scl JOIN scl.courseLocation cl JOIN cl.courses c WHERE c.id = :courseId AND (l.id = :lessonId OR l IS NULL)")
    List<Student> findByCourseIdWithLessonNotes(@Param("courseId") Long courseId, @Param("lessonId") Long lessonId);
}
