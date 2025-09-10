package com.course.app.dto;

import com.course.app.entity.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter arasında olmalıdır")
    private String username;
    
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;
    
    @NotNull(message = "Kullanıcı rolü belirtilmelidir")
    private Role role;
    
    private String firstName;
    
    private String lastName;
    
    @Size(max = 20, message = "Telefon numarası en fazla 20 karakter olabilir")
    private String phone;
}
