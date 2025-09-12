package com.course.app.repository;

import com.course.app.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    // Find lessons by course ID
    List<Lesson> findByCourseId(Long courseId);
    
    // Find lessons by course ID and order by date
    List<Lesson> findByCourseIdOrderByDateAsc(Long courseId);
    
    // Count lessons by course ID
    long countByCourseId(Long courseId);
    
    /**
     * Count lessons by location ID
     * @param locationId Location ID
     * @return Number of lessons in the location
     */
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.courseLocation.id = :locationId")
    int countByLocationId(@Param("locationId") Long locationId);
}
