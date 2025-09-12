package com.course.app.service;

import com.course.app.dto.LessonNoteBatchUpdateRequest;
import com.course.app.dto.LessonNoteDTO;
import com.course.app.dto.StudentLessonNoteDTO;
import com.course.app.entity.Lesson;
import com.course.app.entity.LessonNote;
import com.course.app.entity.LessonNoteHistory;
import com.course.app.entity.Student;
import com.course.app.entity.User;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.LessonNoteHistoryRepository;
import com.course.app.repository.LessonNoteRepository;
import com.course.app.repository.LessonRepository;
import com.course.app.repository.StudentLessonNoteRepository;
import com.course.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentLessonNoteService {

    private final StudentLessonNoteRepository studentLessonNoteRepository;
    private final LessonRepository lessonRepository;
    private final LessonNoteRepository lessonNoteRepository;
    private final LessonNoteHistoryRepository lessonNoteHistoryRepository;
    private final UserRepository userRepository;
    private final StudentService studentService;

    @Autowired
    public StudentLessonNoteService(
            StudentLessonNoteRepository studentLessonNoteRepository,
            LessonRepository lessonRepository,
            LessonNoteRepository lessonNoteRepository,
            LessonNoteHistoryRepository lessonNoteHistoryRepository,
            UserRepository userRepository,
            StudentService studentService) {
        this.studentLessonNoteRepository = studentLessonNoteRepository;
        this.lessonRepository = lessonRepository;
        this.lessonNoteRepository = lessonNoteRepository;
        this.lessonNoteHistoryRepository = lessonNoteHistoryRepository;
        this.userRepository = userRepository;
        this.studentService = studentService;
    }

    /**
     * Get students by course ID with their lesson notes for a specific lesson
     */
    public List<StudentLessonNoteDTO> getStudentsWithLessonNotes(Long courseId, Long lessonId) {
        // Verify lesson exists
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        
        List<Student> students = studentLessonNoteRepository.findByCourseId(courseId);
        return StudentLessonNoteDTO.fromEntities(students, lessonId);
    }

    /**
     * Update a single student's lesson note
     */
    @Transactional
    public LessonNoteDTO updateStudentLessonNote(Long studentId, Long lessonId, LessonNoteBatchUpdateRequest.LessonNoteUpdateItem noteData, Long currentUserId) {
        // Validate input parameters
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID cannot be null");
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("Current user ID cannot be null");
        }
        
        // Find student
        Student student = studentLessonNoteRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        // Find lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        
        // Find current user
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        
        // Find existing note or create new one
        LessonNote lessonNote = lessonNoteRepository.findByStudentIdAndLessonId(studentId, lessonId);
        
        if (lessonNote == null) {
            // Create new note
            lessonNote = new LessonNote();
            lessonNote.setStudent(student);
            lessonNote.setLesson(lesson);
        } else {
            // Create history record before updating
            LessonNoteHistory history = new LessonNoteHistory();
            history.setOldScore(lessonNote.getScore());
            history.setOldPassed(lessonNote.getPassed());
            history.setOldRemark(lessonNote.getRemark());
            history.setChangeDate(LocalDateTime.now());
            history.setLessonNote(lessonNote);
            history.setModifiedBy(currentUser);
            
            // Save history
            lessonNoteHistoryRepository.save(history);
        }
        
        // Update note fields
        if (Boolean.TRUE.equals(noteData.getPassed()) && lesson.getDefaultScore() != null) {
            // Eğer öğrenci geçtiyse ve dersin defaultScore değeri varsa, o değeri kullan
            lessonNote.setScore(lesson.getDefaultScore());
        } else {
            lessonNote.setScore(noteData.getScore());
        }
        
        lessonNote.setPassed(noteData.getPassed());
        lessonNote.setRemark(noteData.getRemark());
        
        // Save note
        LessonNote savedNote = lessonNoteRepository.save(lessonNote);
        
        // Update student's total score if passed
        if (Boolean.TRUE.equals(noteData.getPassed())) {
            studentService.updateStudentTotalScore(studentId);
        }
        
        return LessonNoteDTO.fromEntity(savedNote);
    }

    /**
     * Batch update lesson notes
     */
    @Transactional
    public List<LessonNoteDTO> batchUpdateLessonNotes(LessonNoteBatchUpdateRequest request, Long currentUserId) {
        List<LessonNoteDTO> updatedNotes = new ArrayList<>();
        
        // Validate input parameters
        if (request == null || request.getNotes() == null) {
            throw new IllegalArgumentException("Request or notes cannot be null");
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("Current user ID cannot be null");
        }
        
        // Verify user exists
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        for (LessonNoteBatchUpdateRequest.LessonNoteUpdateItem item : request.getNotes()) {
            // Validate each item's IDs
            if (item.getStudentId() == null || item.getLessonId() == null) {
                continue; // Skip items with null IDs
            }
            
            try {
                LessonNoteDTO updatedNote = updateStudentLessonNote(
                    item.getStudentId(), 
                    item.getLessonId(), 
                    item,
                    currentUserId
                );
                updatedNotes.add(updatedNote);
            } catch (Exception e) {
                // Log the error but continue processing other items
                System.err.println("Error updating note for student " + item.getStudentId() + ": " + e.getMessage());
            }
        }
        
        return updatedNotes;
    }
}
