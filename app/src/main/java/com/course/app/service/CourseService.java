package com.course.app.service;

import com.course.app.dto.CourseCreateRequest;
import com.course.app.dto.CourseDTO;
import com.course.app.dto.CourseUpdateRequest;
import com.course.app.entity.Course;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.User;
import com.course.app.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.CourseRepository;
import com.course.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseLocationRepository courseLocationRepository;
    private final UserRepository userRepository;

    /**
     * Get all courses
     */
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get courses for the current admin user
     */
    public List<CourseDTO> getCoursesForCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        
        // Get all course locations where the admin is assigned
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(currentUser);
        
        // Get all courses for these locations
        return courseRepository.findByCourseLocationIn(adminLocations).stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get course by ID
     */
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs bulunamadı: " + id));
        return CourseDTO.fromEntity(course);
    }
    
    /**
     * Get course by ID for admin user (only courses in admin's locations)
     */
    public CourseDTO getCourseByIdForAdmin(Long id) {
        // Get current admin user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        
        // Get admin's locations
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(currentUser);
        
        // Get course by ID
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs bulunamadı: " + id));
        
        // Check if course is in admin's locations
        boolean hasAccess = false;
        
        // Check primary location
        if (course.getCourseLocation() != null) {
            hasAccess = adminLocations.stream()
                    .anyMatch(loc -> loc.getId().equals(course.getCourseLocation().getId()));
        }
        
        // Check all locations
        if (!hasAccess) {
            for (CourseLocation courseLocation : course.getCourseLocations()) {
                if (adminLocations.stream().anyMatch(loc -> loc.getId().equals(courseLocation.getId()))) {
                    hasAccess = true;
                    break;
                }
            }
        }
        
        if (!hasAccess) {
            throw new AccessDeniedException("Bu kursa erişim yetkiniz yok: " + id);
        }
        
        return CourseDTO.fromEntity(course);
    }

    /**
     * Create a new course
     */
    @Transactional
    public CourseDTO createCourse(CourseCreateRequest request) {
        // Get the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setCreatedBy(currentUser);
        
        // Check if we're using the multi-location approach (superadmin) or single location (admin)
        if (request.getCourseLocationIds() != null && !request.getCourseLocationIds().isEmpty()) {
            // Superadmin flow - multiple locations
            List<CourseLocation> locations = courseLocationRepository.findAllById(request.getCourseLocationIds());
            
            if (locations.size() != request.getCourseLocationIds().size()) {
                throw new ResourceNotFoundException("Bir veya daha fazla kurs lokasyonu bulunamadı");
            }
            
            course.setCourseLocations(locations);
            
            // For backward compatibility, set the first location as the primary one
            if (!locations.isEmpty()) {
                course.setCourseLocation(locations.get(0));
            }
        } else if (request.getCourseLocationId() != null) {
            // Admin flow - single location
            CourseLocation courseLocation = courseLocationRepository.findById(request.getCourseLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + request.getCourseLocationId()));
            
            course.setCourseLocation(courseLocation);
            
            // Also add to the locations list for consistency
            course.getCourseLocations().add(courseLocation);
        } else {
            throw new IllegalArgumentException("Kurs için en az bir lokasyon belirtilmelidir");
        }
        
        Course savedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(savedCourse);
    }

    /**
     * Update an existing course for admin user (only courses in admin's locations)
     */
    @Transactional
    public CourseDTO updateCourseForAdmin(Long id, CourseUpdateRequest request) {
        // First check if admin has access to this course
        getCourseByIdForAdmin(id); // This will throw AccessDeniedException if admin doesn't have access
        
        // If we get here, admin has access, so proceed with update
        return updateCourse(id, request);
    }
    
    /**
     * Update an existing course
     */
    @Transactional
    public CourseDTO updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs bulunamadı: " + id));
        
        // Update fields if provided
        if (request.getName() != null) {
            course.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        
        if (request.getStartDate() != null) {
            course.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            course.setEndDate(request.getEndDate());
        }
        
        // Check if we're using the multi-location approach (superadmin) or single location (admin)
        if (request.getCourseLocationIds() != null) {
            // Superadmin flow - multiple locations
            if (request.getCourseLocationIds().isEmpty()) {
                // Clear all locations
                course.getCourseLocations().clear();
                course.setCourseLocation(null);
            } else {
                List<CourseLocation> locations = courseLocationRepository.findAllById(request.getCourseLocationIds());
                
                if (locations.size() != request.getCourseLocationIds().size()) {
                    throw new ResourceNotFoundException("Bir veya daha fazla kurs lokasyonu bulunamadı");
                }
                
                // Replace all locations
                course.getCourseLocations().clear();
                course.getCourseLocations().addAll(locations);
                
                // For backward compatibility, set the first location as the primary one
                if (!locations.isEmpty()) {
                    course.setCourseLocation(locations.get(0));
                } else {
                    course.setCourseLocation(null);
                }
            }
        } else if (request.getCourseLocationId() != null) {
            // Admin flow - single location
            CourseLocation courseLocation = courseLocationRepository.findById(request.getCourseLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + request.getCourseLocationId()));
            
            course.setCourseLocation(courseLocation);
            
            // Update the locations list for consistency
            // First check if this location is already in the list
            boolean locationExists = course.getCourseLocations().stream()
                    .anyMatch(loc -> loc.getId().equals(courseLocation.getId()));
            
            if (!locationExists) {
                course.getCourseLocations().add(courseLocation);
            }
        }
        
        Course updatedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(updatedCourse);
    }

    /**
     * Delete a course for admin user (only courses in admin's locations)
     */
    @Transactional
    public void deleteCourseForAdmin(Long id) {
        // First check if admin has access to this course
        getCourseByIdForAdmin(id); // This will throw AccessDeniedException if admin doesn't have access
        
        // If we get here, admin has access, so proceed with delete
        deleteCourse(id);
    }
    
    /**
     * Delete a course
     */
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kurs bulunamadı: " + id);
        }
        courseRepository.deleteById(id);
    }
    
    /**
     * Remove a course from a specific location
     */
    @Transactional
    public CourseDTO removeCourseFromLocation(Long courseId, Long locationId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs bulunamadı: " + courseId));
        
        // Verify the location exists
        courseLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + locationId));
        
        // Check if the course is assigned to this location
        boolean removed = course.getCourseLocations().removeIf(loc -> loc.getId().equals(locationId));
        
        if (!removed) {
            throw new IllegalStateException("Kurs bu lokasyona atanmamış");
        }
        
        // If this was the primary location, update it
        if (course.getCourseLocation() != null && course.getCourseLocation().getId().equals(locationId)) {
            // Set the first available location as primary, or null if none left
            if (!course.getCourseLocations().isEmpty()) {
                course.setCourseLocation(course.getCourseLocations().get(0));
            } else {
                course.setCourseLocation(null);
            }
        }
        
        Course updatedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(updatedCourse);
    }
    
    /**
     * Add a course to a specific location
     */
    @Transactional
    public CourseDTO addCourseToLocation(Long courseId, Long locationId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs bulunamadı: " + courseId));
        
        CourseLocation location = courseLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + locationId));
        
        // Check if the course is already assigned to this location
        boolean alreadyAssigned = course.getCourseLocations().stream()
                .anyMatch(loc -> loc.getId().equals(locationId));
        
        if (alreadyAssigned) {
            throw new IllegalStateException("Kurs zaten bu lokasyona atanmış");
        }
        
        // Add the location
        course.getCourseLocations().add(location);
        
        // If there's no primary location, set this as primary
        if (course.getCourseLocation() == null) {
            course.setCourseLocation(location);
        }
        
        Course updatedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(updatedCourse);
    }
    
    /**
     * Get all courses for a specific location
     */
    public List<CourseDTO> getCoursesByLocationId(Long locationId) {
        CourseLocation location = courseLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs lokasyonu bulunamadı: " + locationId));
        
        return courseRepository.findByCourseLocationsContaining(location).stream()
                .map(CourseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
