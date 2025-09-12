package com.course.app.controller;

import com.course.app.dto.AdminDashboardDTO;
import com.course.app.service.AdminDashboardService;
import com.course.app.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * Get dashboard data for the current admin user
     * @return Dashboard data
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERADMIN')")
    public ResponseEntity<AdminDashboardDTO> getDashboardData() {
        // Get current user ID from security context
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get dashboard data
        AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData(currentUserId);
        return ResponseEntity.ok(dashboardData);
    }
}
