package com.course.app.service;

import com.course.app.dto.LessonCreateRequestWithMultipleCourses;
import com.course.app.dto.LessonDTO;
import com.course.app.entity.Course;
import com.course.app.entity.Lesson;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.CourseRepository;
import com.course.app.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperadminLessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public SuperadminLessonService(
            LessonRepository lessonRepository,
            CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Create a lesson and assign it to multiple courses
     */
    @Transactional
    public List<LessonDTO> createLessonForMultipleCourses(LessonCreateRequestWithMultipleCourses request) {
        // Validate that all courses exist
        List<Course> courses = courseRepository.findAllById(request.getCourseIds());
        
        if (courses.size() != request.getCourseIds().size()) {
            List<Long> foundIds = courses.stream().map(Course::getId).collect(Collectors.toList());
            List<Long> missingIds = request.getCourseIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            
            throw new ResourceNotFoundException("Courses not found with ids: " + missingIds);
        }
        
        List<Lesson> createdLessons = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lessonDate = LocalDate.parse(request.getDate(), formatter);
        
        // Create a lesson for each course
        for (Course course : courses) {
            Lesson lesson = new Lesson();
            lesson.setName(request.getName());
            lesson.setDescription(request.getDescription());
            lesson.setDate(lessonDate);
            lesson.setDefaultScore(request.getDefaultScore());
            lesson.setCourse(course);
            
            createdLessons.add(lessonRepository.save(lesson));
        }
        
        // Convert to DTOs and return
        return LessonDTO.fromEntities(createdLessons);
    }
    
    /**
     * Get all lessons for superadmin
     */
    public List<LessonDTO> getAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        return LessonDTO.fromEntities(lessons);
    }
}
