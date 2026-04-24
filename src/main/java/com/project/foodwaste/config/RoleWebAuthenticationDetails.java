package com.project.foodwaste.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class RoleWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String role;

    public RoleWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.role = request.getParameter("role");
    }

    public String getRole() {
        return role;
    }
}
