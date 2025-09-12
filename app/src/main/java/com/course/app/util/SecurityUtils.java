package com.course.app.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Get the current user ID from the security context
     * @return The current user ID or null if not authenticated
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("DEBUG getCurrentUserId: Authentication = " + authentication);
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            
            Object principal = authentication.getPrincipal();
            
            // Debug logging to understand the principal type
            System.out.println("DEBUG getCurrentUserId: Principal class = " + (principal != null ? principal.getClass().getName() : "null"));
            System.out.println("DEBUG getCurrentUserId: Principal toString = " + (principal != null ? principal.toString() : "null"));
            System.out.println("DEBUG getCurrentUserId: Authentication name = " + authentication.getName());
            
            if (principal instanceof UserDetailsImpl) {
                Long userId = ((UserDetailsImpl) principal).getId();
                System.out.println("DEBUG getCurrentUserId: Found UserDetailsImpl with ID = " + userId);
                return userId;
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // For testing or when using Spring's default User implementation
                // Try to find the user by username
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                System.out.println("DEBUG getCurrentUserId: Found username from User = " + username);
                Long userId = findUserIdByUsername(username);
                System.out.println("DEBUG getCurrentUserId: Resolved user ID = " + userId);
                return userId;
            } else if (principal instanceof String && !principal.equals("anonymousUser")) {
                // For cases where the principal is a username string
                System.out.println("DEBUG getCurrentUserId: Found username from String = " + principal);
                Long userId = findUserIdByUsername((String) principal);
                System.out.println("DEBUG getCurrentUserId: Resolved user ID = " + userId);
                return userId;
            } else {
                // Try to extract username from toString() if available
                String principalString = principal.toString();
                if (principalString.contains("username=")) {
                    String username = principalString.substring(
                        principalString.indexOf("username=") + 9, 
                        principalString.indexOf(",", principalString.indexOf("username="))
                    );
                    System.out.println("DEBUG getCurrentUserId: Extracted username from toString = " + username);
                    Long userId = findUserIdByUsername(username);
                    System.out.println("DEBUG getCurrentUserId: Resolved user ID = " + userId);
                    return userId;
                }
                
                // Try authentication.getName() as fallback
                String authName = authentication.getName();
                if (authName != null && !authName.equals("anonymousUser")) {
                    System.out.println("DEBUG getCurrentUserId: Trying authentication.getName() = " + authName);
                    Long userId = findUserIdByUsername(authName);
                    System.out.println("DEBUG getCurrentUserId: Resolved user ID from auth name = " + userId);
                    return userId;
                }
            }
        }
        
        System.out.println("DEBUG getCurrentUserId: Could not determine user ID, returning null");
        return null;
    }
    
    /**
     * Helper method to find a user ID by username
     */
    private static Long findUserIdByUsername(String username) {
        System.out.println("DEBUG findUserIdByUsername: Looking for username = " + username);
        try {
            // Get application context to access repositories
            org.springframework.context.ApplicationContext context = 
                org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(
                    ((org.springframework.web.context.request.ServletRequestAttributes) 
                     org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                    .getRequest().getServletContext());
            
            System.out.println("DEBUG findUserIdByUsername: ApplicationContext = " + context);
            
            if (context != null) {
                com.course.app.repository.UserRepository userRepository = 
                    context.getBean(com.course.app.repository.UserRepository.class);
                
                System.out.println("DEBUG findUserIdByUsername: UserRepository = " + userRepository);
                
                java.util.Optional<com.course.app.entity.User> userOpt = userRepository.findByUsername(username);
                System.out.println("DEBUG findUserIdByUsername: User found = " + userOpt.isPresent());
                
                if (userOpt.isPresent()) {
                    com.course.app.entity.User user = userOpt.get();
                    System.out.println("DEBUG findUserIdByUsername: User details = ID:" + user.getId() + ", Username:" + user.getUsername() + ", Role:" + user.getRole());
                    return user.getId();
                } else {
                    System.out.println("DEBUG findUserIdByUsername: No user found with username = " + username);
                    return null;
                }
            } else {
                System.out.println("DEBUG findUserIdByUsername: ApplicationContext is null");
            }
        } catch (Exception e) {
            System.err.println("DEBUG findUserIdByUsername: Error finding user by username: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG findUserIdByUsername: Returning null for username = " + username);
        return null;
    }
    
    /**
     * Check if the current user has the given role
     * @param role The role to check
     * @return True if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }
    
    /**
     * Check if the current user is an admin
     * @return True if the user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if the current user is a superadmin
     * @return True if the user is a superadmin, false otherwise
     */
    public static boolean isSuperAdmin() {
        return hasRole("SUPERADMIN");
    }
    
    /**
     * Get the current user's role from the security context
     * @return The current user's role or null if not authenticated
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("DEBUG getCurrentUserRole: Authentication = " + authentication);
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            
            System.out.println("DEBUG getCurrentUserRole: Authorities = " + authentication.getAuthorities());
            
            // Get the first authority (role) from the authentication
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse(null);
            
            System.out.println("DEBUG getCurrentUserRole: Found role = " + role);
            return role;
        }
        System.out.println("DEBUG getCurrentUserRole: No authentication or not authenticated");
        return null;
    }
}
