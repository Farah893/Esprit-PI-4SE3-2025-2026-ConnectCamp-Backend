package tn.esprit.projetintegre.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.projetintegre.enums.Role;

public class SecurityUtil {

    /**
     * Checks if the currently authenticated user has the given role.
     */
    public static boolean hasRole(Role requiredRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("ROLE_" + requiredRole.name()));
    }

    public static boolean hasAnyRole(Role... roles) {
        for (Role role : roles) {
            if (hasRole(role)) return true;
        }
        return false;
    }

    /**
     * Retrieves the email (username) of the currently authenticated user.
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }
        return principal.toString();
    }
}
