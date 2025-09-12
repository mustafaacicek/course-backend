package com.course.app.service;

import com.course.app.dto.LessonNoteCreateRequest;
import com.course.app.dto.LessonNoteDTO;
import com.course.app.dto.LessonNoteHistoryDTO;
import com.course.app.dto.LessonNoteUpdateRequest;
import com.course.app.entity.Lesson;
import com.course.app.entity.LessonNote;
import com.course.app.entity.LessonNoteHistory;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.Role;
import com.course.app.entity.Student;
import com.course.app.entity.User;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.LessonNoteHistoryRepository;
import com.course.app.repository.LessonNoteRepository;
import com.course.app.repository.LessonRepository;
import com.course.app.repository.StudentRepository;
import com.course.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonNoteService {

    private final LessonNoteRepository lessonNoteRepository;
    private final LessonNoteHistoryRepository lessonNoteHistoryRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CourseLocationRepository courseLocationRepository;
    private final StudentService studentService;

    @Autowired
    public LessonNoteService(
            LessonNoteRepository lessonNoteRepository,
            LessonNoteHistoryRepository lessonNoteHistoryRepository,
            LessonRepository lessonRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            CourseLocationRepository courseLocationRepository,
            StudentService studentService) {
        this.lessonNoteRepository = lessonNoteRepository;
        this.lessonNoteHistoryRepository = lessonNoteHistoryRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.courseLocationRepository = courseLocationRepository;
        this.studentService = studentService;
    }

    /**
     * Get all lesson notes
     */
    public List<LessonNoteDTO> getAllLessonNotes() {
        List<LessonNote> lessonNotes = lessonNoteRepository.findAll();
        return LessonNoteDTO.fromEntities(lessonNotes);
    }
    
    /**
     * Get lesson notes for admin user (only notes of students in admin's locations)
     */
    public List<LessonNoteDTO> getLessonNotesForAdmin(Long adminId) {
        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + adminId));
        
        // Get admin's locations
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        
        if (adminLocations.isEmpty()) {
            // Admin has no locations, return empty list
            return List.of();
        }
        
        // Get all students in admin's locations (using a Set to avoid duplicates)
        java.util.Set<Long> studentIds = new java.util.HashSet<>();
        
        for (CourseLocation location : adminLocations) {
            // Get students in this location
            location.getStudents().forEach(scl -> studentIds.add(scl.getStudent().getId()));
        }
        
        if (studentIds.isEmpty()) {
            // No students in admin's locations, return empty list
            return List.of();
        }
        
        // Get lesson notes for these students
        List<LessonNote> lessonNotes = new ArrayList<>();
        
        for (Long studentId : studentIds) {
            List<LessonNote> studentNotes = lessonNoteRepository.findByStudentId(studentId);
            lessonNotes.addAll(studentNotes);
        }
        
        return LessonNoteDTO.fromEntities(lessonNotes);
    }

    /**
     * Get lesson notes by lesson ID
     */
    public List<LessonNoteDTO> getLessonNotesByLessonId(Long lessonId) {
        // Verify lesson exists
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
                
        List<LessonNote> lessonNotes = lessonNoteRepository.findByLessonId(lessonId);
        return LessonNoteDTO.fromEntities(lessonNotes);
    }
    
    /**
     * Get lesson notes by lesson ID for admin user (only notes of students in admin's locations)
     */
    public List<LessonNoteDTO> getLessonNotesByLessonIdForAdmin(Long lessonId, Long adminId) {
        // Verify lesson exists
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        
        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + adminId));
        
        // Get admin's locations
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        
        if (adminLocations.isEmpty()) {
            // Admin has no locations, return empty list
            return List.of();
        }
        
        // Get all students in admin's locations (using a Set to avoid duplicates)
        java.util.Set<Long> studentIds = new java.util.HashSet<>();
        
        for (CourseLocation location : adminLocations) {
            // Get students in this location
            location.getStudents().forEach(scl -> studentIds.add(scl.getStudent().getId()));
        }
        
        if (studentIds.isEmpty()) {
            // No students in admin's locations, return empty list
            return List.of();
        }
        
        // Get all lesson notes for the specified lesson
        List<LessonNote> allLessonNotes = lessonNoteRepository.findByLessonId(lessonId);
        
        // Filter notes to only include those for students in admin's locations
        List<LessonNote> filteredNotes = allLessonNotes.stream()
                .filter(note -> studentIds.contains(note.getStudent().getId()))
                .collect(Collectors.toList());
        
        return LessonNoteDTO.fromEntities(filteredNotes);
    }

    /**
     * Get lesson notes by student ID
     */
    public List<LessonNoteDTO> getLessonNotesByStudentId(Long studentId) {
        // Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentId(studentId);
        return LessonNoteDTO.fromEntities(lessonNotes);
    }
    
    /**
     * Get lesson notes by student ID for admin user (only if student is in admin's locations)
     */
    public List<LessonNoteDTO> getLessonNotesByStudentIdForAdmin(Long studentId, Long adminId) {
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        
        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + adminId));
        
        // Get admin's locations
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        
        if (adminLocations.isEmpty()) {
            // Admin has no locations, return empty list
            return List.of();
        }
        
        // Check if student is in any of admin's locations
        boolean studentInAdminLocation = false;
        
        for (CourseLocation location : adminLocations) {
            for (var studentLocation : location.getStudents()) {
                if (studentLocation.getStudent().getId().equals(studentId)) {
                    studentInAdminLocation = true;
                    break;
                }
            }
            if (studentInAdminLocation) break;
        }
        
        if (!studentInAdminLocation) {
            // Student is not in admin's locations, return empty list
            return List.of();
        }
        
        // Student is in admin's locations, return all notes for this student
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentId(studentId);
        return LessonNoteDTO.fromEntities(lessonNotes);
    }

    /**
     * Get lesson note by ID
     */
    public LessonNoteDTO getLessonNoteById(Long id) {
        LessonNote lessonNote = lessonNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson note not found with id: " + id));
        return LessonNoteDTO.fromEntity(lessonNote);
    }

    /**
     * Create a new lesson note
     */
    @Transactional
    public LessonNoteDTO createLessonNote(LessonNoteCreateRequest request) {
        // Find the lesson
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + request.getLessonId()));

        // Find the student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        // Check if a note already exists for this student and lesson
        LessonNote existingNote = lessonNoteRepository.findByStudentIdAndLessonId(
                request.getStudentId(), request.getLessonId());
        
        if (existingNote != null) {
            throw new IllegalStateException("A note already exists for this student and lesson");
        }

        // Create new lesson note
        LessonNote lessonNote = new LessonNote();
        
        // Eğer öğrenci geçtiyse ve dersin defaultScore değeri varsa, o değeri kullan
        if (Boolean.TRUE.equals(request.getPassed())) {
            if (lesson.getDefaultScore() != null) {
                lessonNote.setScore(lesson.getDefaultScore());
            } else if (request.getScore() != null) {
                lessonNote.setScore(request.getScore());
            } else {
                lessonNote.setScore(0); // Varsayılan değer olarak 0 atanır
            }
        } else {
            lessonNote.setScore(request.getScore());
        }
        
        lessonNote.setPassed(request.getPassed());
        lessonNote.setRemark(request.getRemark());
        lessonNote.setLesson(lesson);
        lessonNote.setStudent(student);

        // Save lesson note
        LessonNote savedLessonNote = lessonNoteRepository.save(lessonNote);
        
        // Ensure score is not null if passed
        if (Boolean.TRUE.equals(request.getPassed()) && savedLessonNote.getScore() == null) {
            if (lesson.getDefaultScore() != null) {
                savedLessonNote.setScore(lesson.getDefaultScore());
            } else {
                savedLessonNote.setScore(0); // Default to 0 if no score available
            }
            savedLessonNote = lessonNoteRepository.save(savedLessonNote);
        }
        
        // Update student's total score if passed
        if (Boolean.TRUE.equals(request.getPassed())) {
            studentService.updateStudentTotalScore(student.getId());
        }
        
        return LessonNoteDTO.fromEntity(savedLessonNote);
    }

    /**
     * Update an existing lesson note
     */
    @Transactional
    public LessonNoteDTO updateLessonNote(Long id, LessonNoteUpdateRequest request, Long currentUserId) {
        // Find the lesson note
        LessonNote lessonNote = lessonNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson note not found with id: " + id));

        // Find the current user by ID
        User currentUser = null;
        
        // Log the current user ID for debugging
        System.out.println("Current user ID from security context: " + currentUserId);
        
        if (currentUserId != null) {
            try {
                currentUser = userRepository.findById(currentUserId)
                        .orElse(null);
                
                if (currentUser != null) {
                    System.out.println("Found user: " + currentUser.getUsername() + ", role: " + currentUser.getRole());
                } else {
                    System.out.println("User not found with ID: " + currentUserId);
                }
            } catch (Exception e) {
                System.err.println("Error finding user with ID " + currentUserId + ": " + e.getMessage());
            }
        } else {
            // If no user ID is provided, try to find an admin user
            try {
                // Find users with ADMIN role
                List<User> adminUsers = userRepository.findByRole(Role.ADMIN);
                if (!adminUsers.isEmpty()) {
                    currentUser = adminUsers.get(0);
                    System.out.println("Using admin user as fallback: " + currentUser.getUsername());
                } else {
                    System.out.println("No admin users found, using first available user");
                    currentUser = userRepository.findAll().stream().findFirst().orElse(null);
                }
            } catch (Exception e) {
                System.err.println("Error finding fallback user: " + e.getMessage());
            }
        }
        
        // Create history record before updating
        LessonNoteHistory history = new LessonNoteHistory();
        history.setOldScore(lessonNote.getScore());
        history.setOldPassed(lessonNote.getPassed());
        history.setOldRemark(lessonNote.getRemark());
        history.setChangeDate(LocalDateTime.now());
        history.setLessonNote(lessonNote);
        
        // Set the modified by user - this is critical for tracking who made the change
        if (currentUser != null) {
            history.setModifiedBy(currentUser);
            System.out.println("Setting history modified by user: " + currentUser.getUsername());
        } else {
            System.out.println("WARNING: No user found to associate with this change!");
        }
        
        // Save history
        LessonNoteHistory savedHistory = lessonNoteHistoryRepository.save(history);
        System.out.println("Saved history record with ID: " + savedHistory.getId() + 
                          ", modifiedBy: " + (savedHistory.getModifiedBy() != null ? 
                                            savedHistory.getModifiedBy().getUsername() : "null"));

        // Update fields if provided
        if (request.getPassed() != null) {
            lessonNote.setPassed(request.getPassed());
            
            // Eğer öğrenci geçtiyse ve dersin defaultScore değeri varsa, o değeri kullan
            if (Boolean.TRUE.equals(request.getPassed())) {
                if (lessonNote.getLesson().getDefaultScore() != null) {
                    lessonNote.setScore(lessonNote.getLesson().getDefaultScore());
                } else if (request.getScore() != null) {
                    lessonNote.setScore(request.getScore());
                } else {
                    lessonNote.setScore(0); // Varsayılan değer olarak 0 atanır
                }
            } else if (request.getScore() != null) {
                lessonNote.setScore(request.getScore());
            }
        } else if (request.getScore() != null) {
            lessonNote.setScore(request.getScore());
        }
        
        if (request.getRemark() != null) {
            lessonNote.setRemark(request.getRemark());
        }

        // Save lesson note
        LessonNote updatedLessonNote = lessonNoteRepository.save(lessonNote);
        
        // Update student's total score if passed status changed
        if (request.getPassed() != null) {
            // Ensure score is not null before saving
            if (Boolean.TRUE.equals(request.getPassed()) && updatedLessonNote.getScore() == null) {
                if (updatedLessonNote.getLesson().getDefaultScore() != null) {
                    updatedLessonNote.setScore(updatedLessonNote.getLesson().getDefaultScore());
                } else {
                    updatedLessonNote.setScore(0); // Default to 0 if no score available
                }
                lessonNoteRepository.save(updatedLessonNote);
            }
            
            studentService.updateStudentTotalScore(lessonNote.getStudent().getId());
        }
        
        return LessonNoteDTO.fromEntity(updatedLessonNote);
    }

    /**
     * Delete a lesson note
     */
    @Transactional
    public void deleteLessonNote(Long id) {
        // Check if lesson note exists
        if (!lessonNoteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson note not found with id: " + id);
        }
        
        // Delete all history records first
        List<LessonNoteHistory> histories = lessonNoteHistoryRepository.findByLessonNoteId(id);
        lessonNoteHistoryRepository.deleteAll(histories);
        
        // Then delete the lesson note
        lessonNoteRepository.deleteById(id);
    }
    
    /**
     * Get history for a lesson note
     */
    public List<LessonNoteHistoryDTO> getLessonNoteHistory(Long lessonNoteId) {
        // Verify lesson note exists
        lessonNoteRepository.findById(lessonNoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson note not found with id: " + lessonNoteId));
                
        List<LessonNoteHistory> histories = lessonNoteHistoryRepository.findByLessonNoteIdOrderByChangeDateDesc(lessonNoteId);
        return LessonNoteHistoryDTO.fromEntities(histories);
    }
    
    /**
     * Get lesson notes by course ID
     */
    public List<LessonNoteDTO> getLessonNotesByCourseId(Long courseId) {
        List<LessonNote> lessonNotes = lessonNoteRepository.findByLessonCourseId(courseId);
        return LessonNoteDTO.fromEntities(lessonNotes);
    }
    
    /**
     * Get lesson notes by course ID for admin user (only notes of students in admin's locations)
     */
    public List<LessonNoteDTO> getLessonNotesByCourseIdForAdmin(Long courseId, Long adminId) {
        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + adminId));
        
        // Get admin's locations
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        
        if (adminLocations.isEmpty()) {
            // Admin has no locations, return empty list
            return List.of();
        }
        
        // Get all students in admin's locations (using a Set to avoid duplicates)
        java.util.Set<Long> studentIds = new java.util.HashSet<>();
        
        for (CourseLocation location : adminLocations) {
            // Get students in this location
            location.getStudents().forEach(scl -> studentIds.add(scl.getStudent().getId()));
        }
        
        if (studentIds.isEmpty()) {
            // No students in admin's locations, return empty list
            return List.of();
        }
        
        // Get all lesson notes for the specified course
        List<LessonNote> allLessonNotes = lessonNoteRepository.findByLessonCourseId(courseId);
        
        // Filter notes to only include those for students in admin's locations
        List<LessonNote> filteredNotes = allLessonNotes.stream()
                .filter(note -> studentIds.contains(note.getStudent().getId()))
                .collect(Collectors.toList());
        
        return LessonNoteDTO.fromEntities(filteredNotes);
    }
    
    /**
     * Get passed lesson notes by student ID
     */
    public List<LessonNoteDTO> getPassedLessonNotesByStudentId(Long studentId) {
        // Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentIdAndPassedTrue(studentId);
        return LessonNoteDTO.fromEntities(lessonNotes);
    }
    
    /**
     * Get failed lesson notes by student ID
     */
    public List<LessonNoteDTO> getFailedLessonNotesByStudentId(Long studentId) {
        // Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentIdAndPassedFalse(studentId);
        return LessonNoteDTO.fromEntities(lessonNotes);
    }
}
