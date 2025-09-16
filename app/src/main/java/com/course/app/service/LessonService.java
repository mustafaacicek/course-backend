package com.course.app.service;

import com.course.app.dto.LessonCreateRequest;
import com.course.app.dto.LessonDTO;
import com.course.app.dto.LessonUpdateRequest;
import com.course.app.entity.Course;
import com.course.app.entity.Lesson;
import com.course.app.entity.User;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.exception.UnauthorizedException;
import com.course.app.repository.CourseRepository;
import com.course.app.repository.LessonRepository;
import com.course.app.repository.UserRepository;
import com.course.app.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Autowired
    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository, UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all lessons
     */
    public List<LessonDTO> getAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        return LessonDTO.fromEntities(lessons);
    }

    /**
     * Get lessons by course ID
     */
    public List<LessonDTO> getLessonsByCourseId(Long courseId) {
        // Verify course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByDateAsc(courseId);
        return LessonDTO.fromEntities(lessons);
    }

    /**
     * Get lesson by ID
     */
    public LessonDTO getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
        return LessonDTO.fromEntity(lesson);
    }

    /**
     * Create a new lesson
     */
    @Transactional
    public LessonDTO createLesson(LessonCreateRequest request) {
        // Find the course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        // Get current user
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("User must be authenticated to create a lesson");
        }
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        // Create new lesson
        Lesson lesson = new Lesson();
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setDate(request.getDate());
        lesson.setDefaultScore(request.getDefaultScore());
        lesson.setCourse(course);
        lesson.setCreatedBy(currentUser); // Set the creator

        // Save and return
        Lesson savedLesson = lessonRepository.save(lesson);
        return LessonDTO.fromEntity(savedLesson);
    }

    /**
     * Update an existing lesson
     */
    @Transactional
    public LessonDTO updateLesson(Long id, LessonUpdateRequest request) {
        // Find the lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            lesson.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            lesson.setDescription(request.getDescription());
        }
        
        if (request.getDate() != null) {
            lesson.setDate(request.getDate());
        }
        
        if (request.getDefaultScore() != null) {
            lesson.setDefaultScore(request.getDefaultScore());
        }
        
        // Update course if provided
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));
            lesson.setCourse(course);
        }

        // Save and return
        Lesson updatedLesson = lessonRepository.save(lesson);
        return LessonDTO.fromEntity(updatedLesson);
    }

    /**
     * Delete a lesson
     * Only the creator of the lesson or a superadmin can delete it
     */
    @Transactional
    public void deleteLesson(Long id) {
        // Find the lesson
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
        
        // Get current user
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("User must be authenticated to delete a lesson");
        }
        
        // Check if user is superadmin
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        
        // Check if user is the creator of the lesson
        boolean isCreator = lesson.getCreatedBy() != null && 
                            lesson.getCreatedBy().getId().equals(currentUserId);
        
        // Only allow deletion if user is superadmin or the creator
        if (!isSuperAdmin && !isCreator) {
            throw new UnauthorizedException("Only the creator of the lesson or a superadmin can delete it");
        }
        
        lessonRepository.deleteById(id);
    }
    
    /**
     * Count lessons by course ID
     */
    public long countLessonsByCourseId(Long courseId) {
        // Verify course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                
        return lessonRepository.countByCourseId(courseId);
    }
    
    /**
     * Bulk delete lessons (superadmin only)
     * @return number of lessons successfully deleted
     */
    @Transactional
    public int bulkDeleteLessons(List<Long> lessonIds) {
        int deletedCount = 0;
        
        for (Long id : lessonIds) {
            try {
                if (lessonRepository.existsById(id)) {
                    lessonRepository.deleteById(id);
                    deletedCount++;
                }
            } catch (Exception e) {
                // Log the error but continue with other deletions
                System.err.println("Error deleting lesson with id: " + id + ". Error: " + e.getMessage());
            }
        }
        
        return deletedCount;
    }
    
    /**
     * Bulk move lessons to another course (superadmin only)
     * @return number of lessons successfully moved
     */
    @Transactional
    public int bulkMoveLessonsToCourse(List<Long> lessonIds, Long targetCourseId) {
        // Verify target course exists
        Course targetCourse = courseRepository.findById(targetCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("Target course not found with id: " + targetCourseId));
        
        int movedCount = 0;
        
        for (Long id : lessonIds) {
            try {
                Lesson lesson = lessonRepository.findById(id)
                        .orElse(null);
                        
                if (lesson != null) {
                    lesson.setCourse(targetCourse);
                    lessonRepository.save(lesson);
                    movedCount++;
                }
            } catch (Exception e) {
                // Log the error but continue with other moves
                System.err.println("Error moving lesson with id: " + id + ". Error: " + e.getMessage());
            }
        }
        
        return movedCount;
    }
}
