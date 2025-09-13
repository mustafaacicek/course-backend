package com.course.app.repository;

import com.course.app.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRankingRepository extends JpaRepository<Student, Long> {

    @Query(value = "SELECT s.id, s.first_name, s.last_name, s.national_id, " +
            "COALESCE(SUM(ln.score), 0) as total_score, " +
            "COALESCE(AVG(ln.score), 0) as average_score " +
            "FROM students s " +
            "LEFT JOIN lesson_notes ln ON s.id = ln.student_id " +
            "GROUP BY s.id, s.first_name, s.last_name, s.national_id " +
            "ORDER BY total_score DESC, average_score DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopStudentsByScore(@Param("limit") int limit);

    @Query(value = "SELECT s.id, s.first_name, s.last_name, s.national_id, " +
            "COALESCE(SUM(ln.score), 0) as total_score, " +
            "COALESCE(AVG(ln.score), 0) as average_score, " +
            "cl.id as location_id, cl.name as location_name " +
            "FROM students s " +
            "LEFT JOIN lesson_notes ln ON s.id = ln.student_id " +
            "LEFT JOIN lessons l ON ln.lesson_id = l.id " +
            "LEFT JOIN courses c ON l.course_id = c.id " +
            "LEFT JOIN course_locations cl ON c.location_id = cl.id " +
            "WHERE cl.id = :locationId " +
            "GROUP BY s.id, s.first_name, s.last_name, s.national_id, cl.id, cl.name " +
            "ORDER BY total_score DESC, average_score DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopStudentsByScoreAndLocation(@Param("locationId") Long locationId, @Param("limit") int limit);
}
