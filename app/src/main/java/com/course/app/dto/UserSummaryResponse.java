package com.course.app.dto;

import com.course.app.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    
    private Long id;
    private String username;
    private Role role;
    private String firstName;
    private String lastName;
}
