package com.course.app.repository;

import com.course.app.entity.CourseLocation;
import com.course.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseLocationRepository extends JpaRepository<CourseLocation, Long> {
    
    Optional<CourseLocation> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT cl FROM CourseLocation cl JOIN cl.admins a WHERE a.id = :adminId")
    List<CourseLocation> findAllByAdminId(@Param("adminId") Long adminId);
    
    List<CourseLocation> findByAdminsContaining(User admin);
}
