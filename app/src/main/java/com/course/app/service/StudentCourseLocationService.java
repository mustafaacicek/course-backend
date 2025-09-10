package com.course.app.service;

import com.course.app.entity.CourseLocation;
import com.course.app.entity.Student;
import com.course.app.entity.StudentCourseLocation;
import com.course.app.entity.User;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.StudentCourseLocationRepository;
import com.course.app.repository.StudentRepository;
import com.course.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseLocationService {

    private final StudentCourseLocationRepository studentCourseLocationRepository;
    private final StudentRepository studentRepository;
    private final CourseLocationRepository courseLocationRepository;
    private final UserRepository userRepository;

    /**
     * Assign a student to a course location
     */
    @Transactional
    public StudentCourseLocation assignStudentToCourseLocation(Long studentId, Long courseLocationId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        CourseLocation courseLocation = courseLocationRepository.findById(courseLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + courseLocationId));
        
        // Check if assignment already exists
        if (studentCourseLocationRepository.existsByStudentAndCourseLocation(student, courseLocation)) {
            // Already assigned, return the existing assignment
            return studentCourseLocationRepository.findByStudentAndCourseLocation(student, courseLocation)
                    .orElseThrow(() -> new RuntimeException("Unexpected error: Assignment should exist"));
        }
        
        // Create new assignment
        StudentCourseLocation assignment = new StudentCourseLocation();
        assignment.setStudent(student);
        assignment.setCourseLocation(courseLocation);
        
        return studentCourseLocationRepository.save(assignment);
    }
    
    /**
     * Assign a student to an admin's course locations
     */
    @Transactional
    public List<StudentCourseLocation> assignStudentToAdminLocations(Long studentId, Long adminId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin bulunamadı: " + adminId));
        
        // Get all course locations where the admin is assigned
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        
        if (adminLocations.isEmpty()) {
            throw new ResourceNotFoundException("Bu admin hiçbir kurs lokasyonuna atanmamış: " + adminId);
        }
        
        // Assign student to all admin's locations
        return adminLocations.stream()
                .map(location -> {
                    // Check if assignment already exists
                    if (studentCourseLocationRepository.existsByStudentAndCourseLocation(student, location)) {
                        return studentCourseLocationRepository.findByStudentAndCourseLocation(student, location)
                                .orElseThrow(() -> new RuntimeException("Unexpected error: Assignment should exist"));
                    }
                    
                    // Create new assignment
                    StudentCourseLocation assignment = new StudentCourseLocation();
                    assignment.setStudent(student);
                    assignment.setCourseLocation(location);
                    return studentCourseLocationRepository.save(assignment);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get all course locations for a student
     */
    public List<CourseLocation> getStudentCourseLocations(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        return studentCourseLocationRepository.findByStudent(student).stream()
                .map(StudentCourseLocation::getCourseLocation)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all students for a course location
     */
    public List<Student> getCourseLocationStudents(Long courseLocationId) {
        CourseLocation courseLocation = courseLocationRepository.findById(courseLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + courseLocationId));
        
        return studentCourseLocationRepository.findByCourseLocation(courseLocation).stream()
                .map(StudentCourseLocation::getStudent)
                .collect(Collectors.toList());
    }
    
    /**
     * Remove a student from a course location
     */
    @Transactional
    public void removeStudentFromCourseLocation(Long studentId, Long courseLocationId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        CourseLocation courseLocation = courseLocationRepository.findById(courseLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + courseLocationId));
        
        StudentCourseLocation assignment = studentCourseLocationRepository.findByStudentAndCourseLocation(student, courseLocation)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bu kurs lokasyonuna atanmamış"));
        
        studentCourseLocationRepository.delete(assignment);
    }
}
