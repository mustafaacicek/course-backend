package com.course.app.dto;

import com.course.app.entity.LessonNote;
import com.course.app.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentLessonNoteDTO {
    private Long id;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String motherName;
    private String fatherName;
    private String address;
    private String phone;
    private LocalDate birthDate;
    private Long userId;
    private String username;
    private LessonNoteDTO lessonNote; // Current lesson note for the specified lesson, if exists

    public static StudentLessonNoteDTO fromEntity(Student student, Long lessonId) {
        StudentLessonNoteDTO dto = new StudentLessonNoteDTO();
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
        
        // Find the lesson note for the specified lesson
        if (student.getLessonNotes() != null && !student.getLessonNotes().isEmpty()) {
            LessonNote note = student.getLessonNotes().stream()
                .filter(ln -> ln.getLesson() != null && ln.getLesson().getId().equals(lessonId))
                .findFirst()
                .orElse(null);
            
            if (note != null) {
                dto.setLessonNote(LessonNoteDTO.fromEntity(note));
            }
        }
        
        return dto;
    }
    
    public static List<StudentLessonNoteDTO> fromEntities(List<Student> students, Long lessonId) {
        return students.stream()
            .map(student -> fromEntity(student, lessonId))
            .collect(Collectors.toList());
    }
}
