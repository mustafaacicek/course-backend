package com.course.app.service;

import com.course.app.dto.CourseLocationCreateRequest;
import com.course.app.dto.CourseLocationResponse;
import com.course.app.dto.CourseLocationUpdateRequest;
import com.course.app.dto.UserSummaryResponse;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.User;
import com.course.app.exception.ResourceAlreadyExistsException;
import com.course.app.exception.ResourceNotFoundException;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseLocationService {

    private final CourseLocationRepository courseLocationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CourseLocationResponse> getAllLocations() {
        return courseLocationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseLocationResponse getLocationById(Long id) {
        CourseLocation location = courseLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course location not found with id: " + id));
        return mapToResponse(location);
    }

    @Transactional
    public CourseLocationResponse createLocation(CourseLocationCreateRequest request) {
        // Check if location with the same name already exists
        if (courseLocationRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Course location with name " + request.getName() + " already exists");
        }

        CourseLocation location = new CourseLocation();
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setPhone(request.getPhone());
        
        // Set admins if provided
        if (request.getAdminIds() != null && !request.getAdminIds().isEmpty()) {
            List<User> admins = userRepository.findAllById(request.getAdminIds());
            location.setAdmins(admins);
        }

        CourseLocation savedLocation = courseLocationRepository.save(location);
        return mapToResponse(savedLocation);
    }

    @Transactional
    public CourseLocationResponse updateLocation(Long id, CourseLocationUpdateRequest request) {
        CourseLocation location = courseLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course location not found with id: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            // Check if another location with the same name exists
            courseLocationRepository.findByName(request.getName())
                    .ifPresent(existingLocation -> {
                        if (!existingLocation.getId().equals(id)) {
                            throw new ResourceAlreadyExistsException("Course location with name " + request.getName() + " already exists");
                        }
                    });
            location.setName(request.getName());
        }

        if (request.getAddress() != null) {
            location.setAddress(request.getAddress());
        }

        if (request.getPhone() != null) {
            location.setPhone(request.getPhone());
        }

        // Update admins if provided
        if (request.getAdminIds() != null) {
            List<User> admins = userRepository.findAllById(request.getAdminIds());
            location.setAdmins(admins);
        }

        CourseLocation updatedLocation = courseLocationRepository.save(location);
        return mapToResponse(updatedLocation);
    }

    @Transactional
    public void deleteLocation(Long id) {
        if (!courseLocationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course location not found with id: " + id);
        }
        courseLocationRepository.deleteById(id);
    }

    @Transactional
    public CourseLocationResponse assignAdminsToLocation(Long locationId, List<Long> adminIds) {
        CourseLocation location = courseLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Course location not found with id: " + locationId));

        List<User> admins = userRepository.findAllById(adminIds);
        
        // Check if all admin IDs were found
        if (admins.size() != adminIds.size()) {
            throw new ResourceNotFoundException("One or more admin users not found");
        }
        
        location.setAdmins(admins);
        CourseLocation updatedLocation = courseLocationRepository.save(location);
        return mapToResponse(updatedLocation);
    }

    @Transactional(readOnly = true)
    public List<CourseLocationResponse> getLocationsByAdminId(Long adminId) {
        return courseLocationRepository.findAllByAdminId(adminId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CourseLocationResponse> getLocationsForCurrentAdmin() {
        // Get the current authenticated user
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        List<CourseLocation> locations;
        
        // If user is SUPERADMIN, return all locations
        if (currentUser.getRole() == com.course.app.entity.Role.SUPERADMIN) {
            System.out.println("DEBUG: User is SUPERADMIN, returning all locations");
            locations = courseLocationRepository.findAll();
        } else {
            // Otherwise, get locations where the user is an admin
            System.out.println("DEBUG: User is not SUPERADMIN, returning only assigned locations");
            locations = courseLocationRepository.findByAdminsContaining(currentUser);
        }
        
        return locations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CourseLocationResponse mapToResponse(CourseLocation location) {
        List<UserSummaryResponse> adminResponses = location.getAdmins().stream()
                .map(admin -> new UserSummaryResponse(
                        admin.getId(),
                        admin.getUsername(),
                        admin.getRole(),
                        admin.getFirstName(),
                        admin.getLastName()
                ))
                .collect(Collectors.toList());

        return new CourseLocationResponse(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getPhone(),
                location.getCreatedAt(),
                location.getUpdatedAt(),
                adminResponses
        );
    }
}
