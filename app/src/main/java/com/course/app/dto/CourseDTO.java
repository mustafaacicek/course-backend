package com.course.app.dto;

import com.course.app.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private List<CourseLocationDTO> courseLocations;
    private CourseLocationDTO courseLocation; // For backward compatibility
    private UserSummaryDTO createdBy;
    
    // Static method to convert entity to DTO
    public static CourseDTO fromEntity(Course course) {
        if (course == null) return null;
        
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        // Set related entities
        // Handle multiple course locations
        if (course.getCourseLocations() != null && !course.getCourseLocations().isEmpty()) {
            List<CourseLocationDTO> locationDTOs = course.getCourseLocations().stream()
                    .map(CourseLocationDTO::fromEntity)
                    .toList();
            dto.setCourseLocations(locationDTOs);
        } else {
            dto.setCourseLocations(new ArrayList<>());
        }
        
        // For backward compatibility
        if (course.getCourseLocation() != null) {
            dto.setCourseLocation(CourseLocationDTO.fromEntity(course.getCourseLocation()));
        }
        
        if (course.getCreatedBy() != null) {
            dto.setCreatedBy(UserSummaryDTO.fromEntity(course.getCreatedBy()));
        }
        
        return dto;
    }
}
