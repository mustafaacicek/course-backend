package com.course.app.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {
    
    @NotBlank(message = "TC Kimlik No boş olamaz")
    @Size(min = 11, max = 11, message = "TC Kimlik No 11 haneli olmalıdır")
    @Pattern(regexp = "^[0-9]{11}$", message = "TC Kimlik No sadece rakamlardan oluşmalıdır")
    private String nationalId;
    
    @NotBlank(message = "Ad boş olamaz")
    @Size(min = 2, max = 50, message = "Ad 2-50 karakter arasında olmalıdır")
    private String firstName;
    
    @NotBlank(message = "Soyad boş olamaz")
    @Size(min = 2, max = 50, message = "Soyad 2-50 karakter arasında olmalıdır")
    private String lastName;
    
    @Size(max = 50, message = "Anne adı en fazla 50 karakter olabilir")
    private String motherName;
    
    @Size(max = 50, message = "Baba adı en fazla 50 karakter olabilir")
    private String fatherName;
    
    @Size(max = 255, message = "Adres en fazla 255 karakter olabilir")
    private String address;
    
    @Pattern(regexp = "^[0-9]{10,11}$|^$", message = "Telefon numarası geçerli değil")
    private String phone;
    
    @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır")
    private LocalDate birthDate;
    
    // Username and password are now optional
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter arasında olmalıdır")
    private String username;
    
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;
    
    private Long adminId; // Admin ID to automatically assign student to admin's course location
    
    private Long locationId; // Course location ID for student assignment
}
