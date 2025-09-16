package com.course.app.repository;

import com.course.app.entity.CourseLocation;
import com.course.app.entity.Student;
import com.course.app.entity.StudentCourseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseLocationRepository extends JpaRepository<StudentCourseLocation, Long> {
    
    List<StudentCourseLocation> findByStudent(Student student);
    
    List<StudentCourseLocation> findByCourseLocation(CourseLocation courseLocation);
    
    Optional<StudentCourseLocation> findByStudentAndCourseLocation(Student student, CourseLocation courseLocation);
    
    boolean existsByStudentAndCourseLocation(Student student, CourseLocation courseLocation);
    
    /**
     * Count students in a specific location
     * @param locationId Location ID
     * @return Number of students in the location
     */
    @Query("SELECT COUNT(scl) FROM StudentCourseLocation scl WHERE scl.courseLocation.id = :locationId")
    int countByLocationId(@Param("locationId") Long locationId);
    
    /**
     * Find all student-course location mappings for a specific location
     * @param courseLocationId Location ID
     * @return List of student-course location mappings
     */
    @Query("SELECT scl FROM StudentCourseLocation scl WHERE scl.courseLocation.id = :courseLocationId")
    List<StudentCourseLocation> findByCourseLocationId(@Param("courseLocationId") Long courseLocationId);
}
