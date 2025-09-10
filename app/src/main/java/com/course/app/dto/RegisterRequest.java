package com.course.app.dto;

import com.course.app.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @NotBlank(message = "Password cannot be blank")
    private String password;
    
    @NotNull(message = "Role cannot be null")
    private Role role;
    
    private String firstName;
    
    private String lastName;
}
