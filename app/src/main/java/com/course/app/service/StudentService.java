package com.course.app.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.course.app.dto.CourseLocationDTO;
import com.course.app.dto.StudentCreateRequest;
import com.course.app.dto.StudentDTO;
import com.course.app.dto.StudentUpdateRequest;
import com.course.app.dto.UserSummaryDTO;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.LessonNote;
import com.course.app.entity.Student;
import com.course.app.entity.StudentCourseLocation;
import com.course.app.entity.User;
import com.course.app.entity.Role;
import com.course.app.exception.ResourceAlreadyExistsException;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.LessonNoteHistoryRepository;
import com.course.app.repository.LessonNoteRepository;
import com.course.app.repository.StudentCourseLocationRepository;
import com.course.app.repository.StudentRepository;
import com.course.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CourseLocationRepository courseLocationRepository;
    private final StudentCourseLocationRepository studentCourseLocationRepository;
    private final LessonNoteRepository lessonNoteRepository;
    private final LessonNoteHistoryRepository lessonNoteHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + id));
        return convertToDTO(student);
    }
    
    public StudentDTO getStudentByNationalId(String nationalId) {
        Student student = studentRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("TC Kimlik No ile öğrenci bulunamadı: " + nationalId));
        return convertToDTO(student);
    }
    
    @Transactional
    public StudentDTO createStudent(StudentCreateRequest request) {
        // Check if nationalId already exists
        if (studentRepository.existsByNationalId(request.getNationalId())) {
            throw new ResourceAlreadyExistsException("Bu TC Kimlik No ile kayıtlı öğrenci zaten mevcut: " + request.getNationalId());
        }
        
        User savedUser;
        
        // Create user for student if username and password are provided
        if (request.getUsername() != null && !request.getUsername().isEmpty() && 
            request.getPassword() != null && !request.getPassword().isEmpty()) {
            
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ResourceAlreadyExistsException("Bu kullanıcı adı zaten kullanılıyor: " + request.getUsername());
            }
            
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.STUDENT);
            savedUser = userRepository.save(user);
        } else {
            // Generate a username and password if not provided
            String generatedUsername = "student_" + request.getNationalId();
            String generatedPassword = request.getNationalId(); // Using nationalId as default password
            
            // Ensure the generated username is unique
            int counter = 1;
            while (userRepository.existsByUsername(generatedUsername)) {
                generatedUsername = "student_" + request.getNationalId() + "_" + counter;
                counter++;
            }
            
            User user = new User();
            user.setUsername(generatedUsername);
            user.setPassword(passwordEncoder.encode(generatedPassword));
            user.setRole(Role.STUDENT);
            savedUser = userRepository.save(user);
        }
        
        // Create student
        Student student = new Student();
        student.setNationalId(request.getNationalId());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setMotherName(request.getMotherName());
        student.setFatherName(request.getFatherName());
        student.setAddress(request.getAddress());
        student.setPhone(request.getPhone());
        student.setBirthDate(request.getBirthDate());
        student.setUser(savedUser);
        
        Student savedStudent = studentRepository.save(student);
        
        // If locationId is provided, assign student to that specific location
        if (request.getLocationId() != null) {
            CourseLocation location = courseLocationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lokasyon bulunamadı: " + request.getLocationId()));
            
            // Assign student to the specified location
            StudentCourseLocation assignment = new StudentCourseLocation();
            assignment.setStudent(savedStudent);
            assignment.setCourseLocation(location);
            studentCourseLocationRepository.save(assignment);
        }
        // If adminId is provided but no locationId, assign student to admin's course locations
        else if (request.getAdminId() != null) {
            User admin = userRepository.findById(request.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin bulunamadı: " + request.getAdminId()));
            
            // Check if the user is actually an admin
            if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.SUPERADMIN) {
                throw new IllegalArgumentException("Seçilen kullanıcı bir yönetici değil: " + request.getAdminId());
            }
            
            // Get all course locations where the admin is assigned
            List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
            
            if (adminLocations.isEmpty()) {
                // No locations found, just return the student without assignment
                return convertToDTO(savedStudent);
            }
            
            // Assign student to all admin's locations
            for (CourseLocation location : adminLocations) {
                StudentCourseLocation assignment = new StudentCourseLocation();
                assignment.setStudent(savedStudent);
                assignment.setCourseLocation(location);
                studentCourseLocationRepository.save(assignment);
            }
        }
        
        return convertToDTO(savedStudent);
    }
    
    @Transactional
    public StudentDTO updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + id));
        
        // Check if nationalId is being changed and if it already exists
        if (request.getNationalId() != null && !request.getNationalId().equals(student.getNationalId())) {
            if (studentRepository.existsByNationalId(request.getNationalId())) {
                throw new ResourceAlreadyExistsException("Bu TC Kimlik No ile kayıtlı öğrenci zaten mevcut: " + request.getNationalId());
            }
            student.setNationalId(request.getNationalId());
        }
        
        // Update student fields if provided
        if (request.getFirstName() != null) {
            student.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            student.setLastName(request.getLastName());
        }
        
        if (request.getMotherName() != null) {
            student.setMotherName(request.getMotherName());
        }
        
        if (request.getFatherName() != null) {
            student.setFatherName(request.getFatherName());
        }
        
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }
        
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        
        if (request.getBirthDate() != null) {
            student.setBirthDate(request.getBirthDate());
        }
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            User user = student.getUser();
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }
        
        // Handle admin assignment if provided
        if (request.getAdminId() != null) {
            // First, clear existing course location assignments
            if (student.getCourseLocations() != null && !student.getCourseLocations().isEmpty()) {
                studentCourseLocationRepository.deleteAll(student.getCourseLocations());
                student.getCourseLocations().clear();
            }
            
            // Get the admin
            User admin = userRepository.findById(request.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin bulunamadı: " + request.getAdminId()));
            
            // Check if the user is actually an admin
            if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.SUPERADMIN) {
                throw new IllegalArgumentException("Seçilen kullanıcı bir yönetici değil: " + request.getAdminId());
            }
            
            // Get all course locations where the admin is assigned
            List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
            
            // Assign student to all admin's locations
            for (CourseLocation location : adminLocations) {
                StudentCourseLocation assignment = new StudentCourseLocation();
                assignment.setStudent(student);
                assignment.setCourseLocation(location);
                studentCourseLocationRepository.save(assignment);
            }
        }
        
        Student updatedStudent = studentRepository.save(student);
        return convertToDTO(updatedStudent);
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + id));
        
        // Öğrencinin kullanıcı bilgisini al
        User user = student.getUser();
        
        // Öğrencinin ders notlarını bul
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentId(id);
        
        // Her bir ders notu için
        for (LessonNote note : lessonNotes) {
            // Önce ders notu geçmişlerini sil
            lessonNoteHistoryRepository.deleteByLessonNoteId(note.getId());
        }
        
        // Ders notlarını sil
        lessonNoteRepository.deleteByStudentId(id);
        
        // Öğrencinin kurs lokasyonlarını temizle
        if (student.getCourseLocations() != null && !student.getCourseLocations().isEmpty()) {
            studentCourseLocationRepository.deleteAll(student.getCourseLocations());
        }
        
        // Öğrenciyi sil
        studentRepository.delete(student);
        
        // İlişkili kullanıcıyı sil
        if (user != null) {
            userRepository.delete(user);
        }
    }
    
    /**
     * Calculate and update the total score for a student based on passed lessons
     * @param studentId The student ID
     */
    @Transactional
    public void updateStudentTotalScore(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        // Get all lesson notes for the student
        List<LessonNote> lessonNotes = lessonNoteRepository.findByStudentId(studentId);
        
        // Calculate total score from passed lessons with defaultScore or score
        int totalScore = 0;
        for (LessonNote note : lessonNotes) {
            if (Boolean.TRUE.equals(note.getPassed()) && note.getLesson() != null) {
                if (note.getLesson().getDefaultScore() != null) {
                    totalScore += note.getLesson().getDefaultScore();
                } else if (note.getScore() != null) {
                    totalScore += note.getScore();
                }
            }
        }
        
        // Update student's total score
        student.setTotalScore(totalScore);
        studentRepository.save(student);
    }
    
    /**
     * Get the total score for a student
     * @param studentId The student ID
     * @return The total score
     */
    public Integer getStudentTotalScore(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        return student.getTotalScore();
    }
    
    /**
     * Update teacher comment for a student
     * @param studentId The student ID
     * @param teacherComment The teacher comment
     * @return The updated student DTO
     */
    @Transactional
    public StudentDTO updateTeacherComment(Long studentId, String teacherComment) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        student.setTeacherComment(teacherComment);
        Student updatedStudent = studentRepository.save(student);
        
        return convertToDTO(updatedStudent);
    }
    
    /**
     * Get the admin ID associated with a student
     * @param studentId The student ID
     * @return The admin ID, or null if no admin is assigned
     */
    public Long getStudentAdminId(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        // Get the student's course locations
        if (student.getCourseLocations() == null || student.getCourseLocations().isEmpty()) {
            return null;
        }
        
        // Get the first location's admin (assuming all locations have the same admin)
        CourseLocation location = student.getCourseLocations().get(0).getCourseLocation();
        if (location.getAdmins() == null || location.getAdmins().isEmpty()) {
            return null;
        }
        
        // Return the first admin's ID
        return location.getAdmins().get(0).getId();
    }
    
    /**
     * Get the admin details associated with a student
     * @param studentId The student ID
     * @return The admin details as UserSummaryDTO, or null if no admin is assigned
     */
    public UserSummaryDTO getStudentAdmin(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Öğrenci bulunamadı: " + studentId));
        
        // Get the student's course locations
        if (student.getCourseLocations() == null || student.getCourseLocations().isEmpty()) {
            return null;
        }
        
        // Get the first location's admin (assuming all locations have the same admin)
        CourseLocation location = student.getCourseLocations().get(0).getCourseLocation();
        if (location.getAdmins() == null || location.getAdmins().isEmpty()) {
            return null;
        }
        
        // Get the first admin
        User admin = location.getAdmins().get(0);
        
        // Convert to DTO
        UserSummaryDTO adminDTO = new UserSummaryDTO();
        adminDTO.setId(admin.getId());
        adminDTO.setUsername(admin.getUsername());
        adminDTO.setRole(admin.getRole());
        adminDTO.setFirstName(admin.getFirstName());
        adminDTO.setLastName(admin.getLastName());
        adminDTO.setPhone(admin.getPhone());
        
        return adminDTO;
    }
    
    /**
     * Admin'in kendi lokasyonlarındaki öğrencileri getir
     * @param adminId Admin ID'si
     * @return Öğrenci DTO listesi
     */
    public List<StudentDTO> getStudentsByAdminId(Long adminId) {
        System.out.println("DEBUG StudentService: getStudentsByAdminId called with adminId = " + adminId);
        
        // Admin kullanıcısını kontrol et
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null) {
            System.out.println("DEBUG StudentService: Admin user not found with ID = " + adminId);
            return Collections.emptyList();
        }
        
        System.out.println("DEBUG StudentService: Found admin user = " + admin.getUsername() + ", role = " + admin.getRole());
        
        // Admin'in lokasyonlarını kontrol et
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        System.out.println("DEBUG StudentService: Admin has " + adminLocations.size() + " locations");
        
        for (CourseLocation location : adminLocations) {
            System.out.println("DEBUG StudentService: Admin location = " + location.getName() + " (ID: " + location.getId() + ")");
        }
        
        if (adminLocations.isEmpty()) {
            System.out.println("DEBUG StudentService: Admin has no locations, returning empty list");
            return Collections.emptyList();
        }
        
        // Repository metodunu kullanarak admin ID'sine göre öğrencileri bul
        List<Student> students = studentRepository.findByLocationAdminId(adminId);
        System.out.println("DEBUG StudentService: Found " + students.size() + " students for admin ID = " + adminId);
        
        for (Student student : students) {
            System.out.println("DEBUG StudentService: Student = " + student.getFirstName() + " " + student.getLastName() + " (ID: " + student.getId() + ")");
        }
        
        // DTO'lara dönüştür
        List<StudentDTO> studentDTOs = students.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        System.out.println("DEBUG StudentService: Returning " + studentDTOs.size() + " student DTOs");
        return studentDTOs;
    }
    
    /**
     * Kullanıcı ID'sine göre kullanıcıyı getir
     * @param userId Kullanıcı ID'si
     * @return Kullanıcı nesnesi veya null
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    /**
     * Admin'in lokasyonlarını getir
     * @param adminId Admin ID'si
     * @return Admin'in lokasyonları
     */
    public List<CourseLocation> getAdminLocations(Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null) {
            return Collections.emptyList();
        }
        return courseLocationRepository.findByAdminsContaining(admin);
    }
    
    /**
     * Belirli bir lokasyondaki öğrencileri getir
     * @param locationId Lokasyon ID'si
     * @return Öğrenci DTO listesi
     */
    public List<StudentDTO> getStudentsByLocationId(Long locationId) {
        // Lokasyonu kontrol et
        CourseLocation location = courseLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Lokasyon bulunamadı: " + locationId));
        
        // Bu lokasyondaki öğrencileri bul
        List<StudentCourseLocation> studentLocations = studentCourseLocationRepository.findByCourseLocationId(locationId);
        
        // Öğrenci listesini oluştur
        List<Student> students = studentLocations.stream()
                .map(StudentCourseLocation::getStudent)
                .collect(Collectors.toList());
        
        // DTO'lara dönüştür
        return students.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Admin'in belirli bir lokasyona erişim yetkisi olup olmadığını kontrol et
     * @param adminId Admin ID'si
     * @param locationId Lokasyon ID'si
     * @return Erişim yetkisi varsa true, yoksa false
     */
    public boolean adminHasAccessToLocation(Long adminId, Long locationId) {
        // Admin kullanıcısını kontrol et
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null) {
            return false;
        }
        
        // Lokasyonu kontrol et
        CourseLocation location = courseLocationRepository.findById(locationId).orElse(null);
        if (location == null) {
            return false;
        }
        
        // Admin'in bu lokasyona erişim yetkisi var mı kontrol et
        return location.getAdmins() != null && location.getAdmins().contains(admin);
    }
    
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setNationalId(student.getNationalId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setMotherName(student.getMotherName());
        dto.setFatherName(student.getFatherName());
        dto.setAddress(student.getAddress());
        dto.setPhone(student.getPhone());
        dto.setBirthDate(student.getBirthDate());
        dto.setTotalScore(student.getTotalScore());
        dto.setTeacherComment(student.getTeacherComment());
        
        if (student.getUser() != null) {
            dto.setUserId(student.getUser().getId());
            dto.setUsername(student.getUser().getUsername());
        }
        
        // Add course locations information
        if (student.getCourseLocations() != null && !student.getCourseLocations().isEmpty()) {
            List<CourseLocationDTO> locationDTOs = student.getCourseLocations().stream()
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
            
            dto.setCourseLocations(locationDTOs);
        }
        
        return dto;
    }
}
