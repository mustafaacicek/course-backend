package com.course.app.service;

import com.course.app.dto.AdminDashboardDTO;
import com.course.app.dto.UserSummaryDTO;
import com.course.app.entity.CourseLocation;
import com.course.app.entity.User;
import com.course.app.repository.CourseLocationRepository;
import com.course.app.repository.LessonNoteRepository;
import com.course.app.repository.LessonRepository;
import com.course.app.repository.StudentCourseLocationRepository;
import com.course.app.repository.StudentRepository;
import com.course.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseLocationRepository courseLocationRepository;
    private final StudentRepository studentRepository;
    private final StudentCourseLocationRepository studentCourseLocationRepository;
    private final LessonRepository lessonRepository;
    private final LessonNoteRepository lessonNoteRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            CourseLocationRepository courseLocationRepository,
            StudentRepository studentRepository,
            StudentCourseLocationRepository studentCourseLocationRepository,
            LessonRepository lessonRepository,
            LessonNoteRepository lessonNoteRepository) {
        this.userRepository = userRepository;
        this.courseLocationRepository = courseLocationRepository;
        this.studentRepository = studentRepository;
        this.studentCourseLocationRepository = studentCourseLocationRepository;
        this.lessonRepository = lessonRepository;
        this.lessonNoteRepository = lessonNoteRepository;
    }

    /**
     * Get dashboard data for the current admin user
     * @param adminId Admin user ID
     * @return Dashboard data
     */
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardData(Long adminId) {
        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Create dashboard DTO
        AdminDashboardDTO dashboardDTO = new AdminDashboardDTO();
        
        // Set admin info
        UserSummaryDTO adminDTO = new UserSummaryDTO();
        adminDTO.setId(admin.getId());
        adminDTO.setUsername(admin.getUsername());
        adminDTO.setRole(admin.getRole());
        adminDTO.setFirstName(admin.getFirstName());
        adminDTO.setLastName(admin.getLastName());
        adminDTO.setPhone(admin.getPhone());
        dashboardDTO.setAdmin(adminDTO);

        // Get admin locations
        List<CourseLocation> adminLocations = courseLocationRepository.findByAdminsContaining(admin);
        
        // Log admin locations for debugging
        System.out.println("Admin locations found: " + adminLocations.size() + " for admin ID: " + adminId);
        
        // Get total student count in system for reference (using studentRepository)
        long totalStudentsInSystem = studentRepository.count();
        System.out.println("Total students in system: " + totalStudentsInSystem);
        
        // Calculate counts
        int totalStudents = 0;
        int totalLessons = 0;
        int totalNotes = 0;
        
        // Process each location
        List<AdminDashboardDTO.CourseLocationSummaryDTO> locationDTOs = new ArrayList<>();
        
        for (CourseLocation location : adminLocations) {
            AdminDashboardDTO.CourseLocationSummaryDTO locationDTO = new AdminDashboardDTO.CourseLocationSummaryDTO();
            locationDTO.setId(location.getId());
            locationDTO.setName(location.getName());
            locationDTO.setAddress(location.getAddress());
            locationDTO.setPhone(location.getPhone());
            
            // Count students in this location
            int studentCount = studentCourseLocationRepository.countByLocationId(location.getId());
            locationDTO.setStudentCount(studentCount);
            totalStudents += studentCount;
            
            // Add to list
            locationDTOs.add(locationDTO);
            
            // Count lessons and notes for this location
            totalLessons += lessonRepository.countByLocationId(location.getId());
            totalNotes += lessonNoteRepository.countByLocationId(location.getId());
        }
        
        // Set counts
        dashboardDTO.setCourseCount(adminLocations.size());
        dashboardDTO.setStudentCount(totalStudents);
        dashboardDTO.setLessonCount(totalLessons);
        dashboardDTO.setNoteCount(totalNotes);
        dashboardDTO.setLocations(locationDTOs);
        
        // Add recent activities (mock data for now)
        dashboardDTO.setRecentActivities(getRecentActivities());
        
        return dashboardDTO;
    }
    
    /**
     * Generate mock recent activities
     * In a real application, this would come from an activity log table
     */
    private List<AdminDashboardDTO.ActivityDTO> getRecentActivities() {
        List<AdminDashboardDTO.ActivityDTO> activities = new ArrayList<>();
        // Use direct formatting in the activity objects
        
        // Add some mock activities
        AdminDashboardDTO.ActivityDTO activity1 = new AdminDashboardDTO.ActivityDTO();
        activity1.setType("add");
        activity1.setText("Yeni öğrenci eklendi: Ahmet Yılmaz");
        activity1.setTimestamp(LocalDateTime.now().minusHours(2));
        activity1.setFormattedTime("Bugün, " + activity1.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        activities.add(activity1);
        
        AdminDashboardDTO.ActivityDTO activity2 = new AdminDashboardDTO.ActivityDTO();
        activity2.setType("edit");
        activity2.setText("Ders notu güncellendi: Matematik 101");
        activity2.setTimestamp(LocalDateTime.now().minusHours(5));
        activity2.setFormattedTime("Bugün, " + activity2.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        activities.add(activity2);
        
        AdminDashboardDTO.ActivityDTO activity3 = new AdminDashboardDTO.ActivityDTO();
        activity3.setType("create");
        activity3.setText("Yeni kurs oluşturuldu: İngilizce Konuşma");
        activity3.setTimestamp(LocalDateTime.now().minusDays(1));
        activity3.setFormattedTime("Dün, " + activity3.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        activities.add(activity3);
        
        return activities;
    }
}
