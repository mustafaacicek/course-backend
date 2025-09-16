package com.course.app.repository;

import com.course.app.entity.Attendance;
import com.course.app.entity.Course;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByCourseAndAttendanceDate(Course course, LocalDate attendanceDate);
    
    List<Attendance> findByCourseLocationAndAttendanceDate(CourseLocation courseLocation, LocalDate attendanceDate);
    
    List<Attendance> findByStudentAndAttendanceDateBetween(Student student, LocalDate startDate, LocalDate endDate);
    
    Optional<Attendance> findByStudentAndCourseAndAttendanceDate(Student student, Course course, LocalDate attendanceDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.courseLocation.id IN :locationIds AND a.attendanceDate = :date")
    List<Attendance> findByLocationIdsAndDate(@Param("locationIds") List<Long> locationIds, @Param("date") LocalDate date);
    
    @Query("SELECT DISTINCT a.attendanceDate FROM Attendance a WHERE a.courseLocation.id IN :locationIds ORDER BY a.attendanceDate DESC")
    List<LocalDate> findDistinctAttendanceDatesByLocationIds(@Param("locationIds") List<Long> locationIds);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.isPresent = true")
    Long countPresentDaysByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.isPresent = false")
    Long countAbsentDaysByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentId(@Param("studentId") Long studentId);
}
