package com.course.app.repository;

import com.course.app.entity.Course;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Legacy methods for backward compatibility
    List<Course> findByCourseLocation(CourseLocation courseLocation);
    List<Course> findByCreatedBy(User createdBy);
    List<Course> findByCourseLocationIn(List<CourseLocation> courseLocations);
    
    // New methods for multi-location support
    List<Course> findByCourseLocationsContaining(CourseLocation location);
}
