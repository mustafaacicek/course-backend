package com.course.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.course.app.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    boolean existsByNationalId(String nationalId);
    
    Optional<Student> findByNationalId(String nationalId);
    
    Optional<Student> findByUserId(Long userId);
}
