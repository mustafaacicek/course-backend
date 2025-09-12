package com.course.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardDTO {
    
    private UserSummaryDTO admin;
    private int courseCount;
    private int studentCount;
    private int lessonCount;
    private int noteCount;
    private List<CourseLocationSummaryDTO> locations;
    private List<ActivityDTO> recentActivities;
    
    // Nested DTO for location summary with student count
    public static class CourseLocationSummaryDTO {
        private Long id;
        private String name;
        private String address;
        private String phone;
        private int studentCount;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public int getStudentCount() {
            return studentCount;
        }
        
        public void setStudentCount(int studentCount) {
            this.studentCount = studentCount;
        }
    }
    
    // Nested DTO for recent activities
    public static class ActivityDTO {
        private String type; // add, edit, create
        private String text;
        private LocalDateTime timestamp;
        private String formattedTime;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getFormattedTime() {
            return formattedTime;
        }
        
        public void setFormattedTime(String formattedTime) {
            this.formattedTime = formattedTime;
        }
    }
    
    // Getters and setters
    public UserSummaryDTO getAdmin() {
        return admin;
    }
    
    public void setAdmin(UserSummaryDTO admin) {
        this.admin = admin;
    }
    
    public int getCourseCount() {
        return courseCount;
    }
    
    public void setCourseCount(int courseCount) {
        this.courseCount = courseCount;
    }
    
    public int getStudentCount() {
        return studentCount;
    }
    
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
    
    public int getLessonCount() {
        return lessonCount;
    }
    
    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }
    
    public int getNoteCount() {
        return noteCount;
    }
    
    public void setNoteCount(int noteCount) {
        this.noteCount = noteCount;
    }
    
    public List<CourseLocationSummaryDTO> getLocations() {
        return locations;
    }
    
    public void setLocations(List<CourseLocationSummaryDTO> locations) {
        this.locations = locations;
    }
    
    public List<ActivityDTO> getRecentActivities() {
        return recentActivities;
    }
    
    public void setRecentActivities(List<ActivityDTO> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
