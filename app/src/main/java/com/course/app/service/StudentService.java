package com.course.app.service;

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
import com.course.app.entity.Student;
import com.course.app.entity.StudentCourseLocation;
import com.course.app.entity.User;
import com.course.app.entity.Role;
import com.course.app.exception.ResourceAlreadyExistsException;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.CourseLocationRepository;
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
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Bu kullanıcı adı zaten kullanılıyor: " + request.getUsername());
        }
        
        // Create user for student
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        User savedUser = userRepository.save(user);
        
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
        
        // If adminId is provided, assign student to admin's course locations
        if (request.getAdminId() != null) {
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
        
        // Delete associated user
        userRepository.delete(student.getUser());
        
        // Student will be deleted by cascade
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
