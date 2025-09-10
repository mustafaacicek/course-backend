package com.course.app.dto;

import com.course.app.entity.CourseLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseLocationDTO {
    
    private Long id;
    private String name;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserSummaryDTO> admins;
    
    // Static method to convert entity to DTO
    public static CourseLocationDTO fromEntity(CourseLocation location) {
        if (location == null) return null;
        
        CourseLocationDTO dto = new CourseLocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        dto.setPhone(location.getPhone());
        dto.setCreatedAt(location.getCreatedAt());
        dto.setUpdatedAt(location.getUpdatedAt());
        
        // Convert admins to DTOs if available
        if (location.getAdmins() != null) {
            List<UserSummaryDTO> adminDTOs = location.getAdmins().stream()
                    .map(UserSummaryDTO::fromEntity)
                    .collect(Collectors.toList());
            dto.setAdmins(adminDTOs);
        }
        
        return dto;
    }
}
