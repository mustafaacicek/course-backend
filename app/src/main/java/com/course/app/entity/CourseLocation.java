package com.course.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String address;
    
    private String phone;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @ManyToMany
    @JoinTable(
        name = "course_location_admins",
        joinColumns = @JoinColumn(name = "location_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> admins = new ArrayList<>();
    
    // Relations
    @ManyToMany(mappedBy = "courseLocations")
    private List<Course> courses = new ArrayList<>();
    
    // For backward compatibility during transition
    @OneToMany(mappedBy = "courseLocation")
    private List<Course> legacyCourses;
    
    @OneToMany(mappedBy = "courseLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCourseLocation> students = new ArrayList<>();
}
