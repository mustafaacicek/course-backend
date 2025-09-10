package com.course.app.dto;

import com.course.app.entity.Role;
import com.course.app.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    
    private Long id;
    private String username;
    private Role role;
    private String firstName;
    private String lastName;
    private String phone;
    
    // Static method to convert entity to DTO
    public static UserSummaryDTO fromEntity(User user) {
        if (user == null) return null;
        
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        
        return dto;
    }
}
