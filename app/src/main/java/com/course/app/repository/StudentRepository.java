package com.course.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.course.app.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    boolean existsByNationalId(String nationalId);
    
    Optional<Student> findByNationalId(String nationalId);
    
    Optional<Student> findByUserId(Long userId);
    
    /**
     * Admin'in kendi lokasyonlarındaki öğrencileri bul
     * @param adminId Admin kullanıcı ID'si
     * @return Öğrenci listesi
     */
    @Query(value = "SELECT DISTINCT s.* FROM students s " +
           "JOIN student_course_locations scl ON s.id = scl.student_id " +
           "JOIN course_locations cl ON scl.course_location_id = cl.id " +
           "JOIN course_location_admins cla ON cl.id = cla.location_id " +
           "WHERE cla.user_id = :adminId", nativeQuery = true)
    List<Student> findByLocationAdminId(@Param("adminId") Long adminId);
}
