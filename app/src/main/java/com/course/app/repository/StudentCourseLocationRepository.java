package com.course.app.repository;

import com.course.app.entity.CourseLocation;
import com.course.app.entity.Student;
import com.course.app.entity.StudentCourseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseLocationRepository extends JpaRepository<StudentCourseLocation, Long> {
    
    List<StudentCourseLocation> findByStudent(Student student);
    
    List<StudentCourseLocation> findByCourseLocation(CourseLocation courseLocation);
    
    Optional<StudentCourseLocation> findByStudentAndCourseLocation(Student student, CourseLocation courseLocation);
    
    boolean existsByStudentAndCourseLocation(Student student, CourseLocation courseLocation);
}
