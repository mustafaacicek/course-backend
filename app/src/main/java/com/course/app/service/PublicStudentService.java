package com.course.app.service;

import com.course.app.dto.*;
import com.course.app.entity.*;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.LessonNoteRepository;
import com.course.app.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicStudentService {

    private final StudentRepository studentRepository;
    private final LessonNoteRepository lessonNoteRepository;
    private final AttendanceService attendanceService;

    /**
     * Get student performance details by national ID
     * This method is used for the public API for parents
     */
    @Transactional(readOnly = true)
    public StudentPerformanceDTO getStudentPerformanceByNationalId(String nationalId) {
        // Find the student by national ID
        Student student = studentRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + nationalId));
        
        // Create the DTO
        StudentPerformanceDTO performanceDTO = new StudentPerformanceDTO();
        
        // Set basic student information
        performanceDTO.setNationalId(student.getNationalId());
        performanceDTO.setFirstName(student.getFirstName());
        performanceDTO.setLastName(student.getLastName());
        performanceDTO.setMotherName(student.getMotherName());
        performanceDTO.setFatherName(student.getFatherName());
        performanceDTO.setBirthDate(student.getBirthDate());
        
        // Get course locations with their admins
        List<CourseLocationDTO> locationDTOs = new ArrayList<>();
        if (student.getCourseLocations() != null && !student.getCourseLocations().isEmpty()) {
            locationDTOs = student.getCourseLocations().stream()
                    .map(scl -> {
                        CourseLocation location = scl.getCourseLocation();
                        CourseLocationDTO locationDTO = new CourseLocationDTO();
                        locationDTO.setId(location.getId());
                        locationDTO.setName(location.getName());
                        locationDTO.setAddress(location.getAddress());
                        locationDTO.setPhone(location.getPhone());
                        locationDTO.setCreatedAt(location.getCreatedAt());
                        locationDTO.setUpdatedAt(location.getUpdatedAt());
                        
                        // Convert admins to DTOs
                        if (location.getAdmins() != null) {
                            List<UserSummaryDTO> adminDTOs = location.getAdmins().stream()
                                    .map(admin -> {
                                        UserSummaryDTO adminDTO = new UserSummaryDTO();
                                        adminDTO.setId(admin.getId());
                                        adminDTO.setUsername(admin.getUsername());
                                        adminDTO.setRole(admin.getRole());
                                        adminDTO.setFirstName(admin.getFirstName());
                                        adminDTO.setLastName(admin.getLastName());
                                        adminDTO.setPhone(admin.getPhone());
                                        return adminDTO;
                                    })
                                    .collect(Collectors.toList());
                            locationDTO.setAdmins(adminDTOs);
                        }
                        
                        return locationDTO;
                    })
                    .collect(Collectors.toList());
        }
        performanceDTO.setCourseLocations(locationDTOs);
        
        // Get courses the student is enrolled in
        Set<Course> studentCourses = new HashSet<>();
        
        // Get courses from the student's course locations
        if (student.getCourseLocations() != null) {
            for (StudentCourseLocation scl : student.getCourseLocations()) {
                CourseLocation location = scl.getCourseLocation();
                
                // Add courses from the many-to-many relationship
                if (location.getCourses() != null) {
                    studentCourses.addAll(location.getCourses());
                }
                
                // Add courses from the legacy one-to-many relationship
                if (location.getLegacyCourses() != null) {
                    studentCourses.addAll(location.getLegacyCourses());
                }
            }
        }
        
        // Convert courses to DTOs
        List<CourseDTO> courseDTOs = studentCourses.stream()
                .map(course -> {
                    CourseDTO courseDTO = new CourseDTO();
                    courseDTO.setId(course.getId());
                    courseDTO.setName(course.getName());
                    courseDTO.setDescription(course.getDescription());
                    courseDTO.setStartDate(course.getStartDate());
                    courseDTO.setEndDate(course.getEndDate());
                    
                    // Get lessons for this course
                    if (course.getLessons() != null) {
                        List<LessonDTO> lessonDTOs = course.getLessons().stream()
                                .map(lesson -> {
                                    LessonDTO lessonDTO = new LessonDTO();
                                    lessonDTO.setId(lesson.getId());
                                    lessonDTO.setName(lesson.getName());
                                    lessonDTO.setDescription(lesson.getDescription());
                                    lessonDTO.setDate(lesson.getDate());
                                    lessonDTO.setDefaultScore(lesson.getDefaultScore());
                                    return lessonDTO;
                                })
                                .collect(Collectors.toList());
                        courseDTO.setLessons(lessonDTOs);
                    }
                    
                    return courseDTO;
                })
                .collect(Collectors.toList());
        performanceDTO.setCourses(courseDTOs);
        
        // Get lesson notes for the student
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentId(student.getId());
        List<LessonNoteDTO> lessonNoteDTOs = lessonNotes.stream()
                .map(note -> {
                    LessonNoteDTO noteDTO = new LessonNoteDTO();
                    noteDTO.setId(note.getId());
                    noteDTO.setScore(note.getScore());
                    noteDTO.setPassed(note.getPassed());
                    noteDTO.setRemark(note.getRemark());
                    noteDTO.setCreatedAt(note.getCreatedAt());
                    noteDTO.setUpdatedAt(note.getUpdatedAt());
                    
                    // Set lesson information
                    if (note.getLesson() != null) {
                        LessonDTO lessonDTO = new LessonDTO();
                        lessonDTO.setId(note.getLesson().getId());
                        lessonDTO.setName(note.getLesson().getName());
                        lessonDTO.setDescription(note.getLesson().getDescription());
                        lessonDTO.setDate(note.getLesson().getDate());
                        lessonDTO.setDefaultScore(note.getLesson().getDefaultScore());
                        
                        // Set course information
                        if (note.getLesson().getCourse() != null) {
                            CourseDTO courseDTO = new CourseDTO();
                            courseDTO.setId(note.getLesson().getCourse().getId());
                            courseDTO.setName(note.getLesson().getCourse().getName());
                            lessonDTO.setCourse(courseDTO);
                        }
                        
                        noteDTO.setLesson(lessonDTO);
                    }
                    
                    return noteDTO;
                })
                .collect(Collectors.toList());
        performanceDTO.setLessonNotes(lessonNoteDTOs);
        
        // Calculate summary statistics
        performanceDTO.setTotalCourses(courseDTOs.size());
        
        int totalLessons = lessonNoteDTOs.size();
        performanceDTO.setTotalLessons(totalLessons);
        
        long passedLessons = lessonNoteDTOs.stream()
                .filter(note -> Boolean.TRUE.equals(note.getPassed()))
                .count();
        performanceDTO.setPassedLessons((int) passedLessons);
        
        long failedLessons = lessonNoteDTOs.stream()
                .filter(note -> Boolean.FALSE.equals(note.getPassed()))
                .count();
        performanceDTO.setFailedLessons((int) failedLessons);
        
        // Calculate average score
        double averageScore = lessonNoteDTOs.stream()
                .filter(note -> note.getScore() != null)
                .mapToInt(LessonNoteDTO::getScore)
                .average()
                .orElse(0.0);
        performanceDTO.setAverageScore(Math.round(averageScore * 100.0) / 100.0); // Round to 2 decimal places
        
        // Set total score from student entity
        performanceDTO.setTotalScore(student.getTotalScore());
        
        // Calculate attendance rate (assuming all lessons should be attended)
        double attendanceRate = totalLessons > 0 ? 
                (double) (passedLessons + failedLessons) / totalLessons * 100 : 0;
        performanceDTO.setAttendanceRate(Math.round(attendanceRate * 10.0) / 10.0); // Round to 1 decimal place
        
        // Determine performance level based on average score
        String performanceLevel;
        if (averageScore >= 90) {
            performanceLevel = "Mükemmel";
        } else if (averageScore >= 75) {
            performanceLevel = "Çok İyi";
        } else if (averageScore >= 60) {
            performanceLevel = "İyi";
        } else if (averageScore >= 50) {
            performanceLevel = "Orta";
        } else {
            performanceLevel = "Geliştirilmeli";
        }
        performanceDTO.setPerformanceLevel(performanceLevel);
        
        // Set teacher comment from student entity if available, otherwise set a generic one based on performance level
        if (student.getTeacherComment() != null && !student.getTeacherComment().isEmpty()) {
            performanceDTO.setTeacherComment(student.getTeacherComment());
        } else {
            // Generate a generic teacher comment based on performance level
            String teacherComment;
            if (averageScore >= 90) {
                teacherComment = "Öğrencimiz derslerde çok başarılı ve aktif katılım gösteriyor. Tebrikler!";
            } else if (averageScore >= 75) {
                teacherComment = "Öğrencimiz derslerde başarılı ve düzenli çalışıyor. Gayretlerini sürdürmesini dileriz.";
            } else if (averageScore >= 60) {
                teacherComment = "Öğrencimiz derslerde iyi bir performans gösteriyor, ancak daha fazla çalışma ile potansiyelini artırabilir.";
            } else if (averageScore >= 50) {
                teacherComment = "Öğrencimizin derslere daha fazla odaklanması ve düzenli çalışması gerekmektedir.";
            } else {
                teacherComment = "Öğrencimizin derslere katılımı ve çalışma düzeni geliştirilmelidir. Veli görüşmesi tavsiye edilir.";
            }
            performanceDTO.setTeacherComment(teacherComment);
        }
        
        // Get attendance data for the student
        try {
            Map<String, Object> attendanceData = attendanceService.getStudentAttendanceDetails(student.getId());
            performanceDTO.setAttendanceData(attendanceData);
        } catch (Exception e) {
            // If there's an error getting attendance data, just log it and continue
            System.err.println("Error getting attendance data for student " + student.getId() + ": " + e.getMessage());
        }
        
        return performanceDTO;
    }
}
