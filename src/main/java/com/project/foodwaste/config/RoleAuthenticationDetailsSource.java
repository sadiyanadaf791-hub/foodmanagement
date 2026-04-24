package com.project.foodwaste.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;

public class RoleAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, RoleWebAuthenticationDetails> {

    @Override
    public RoleWebAuthenticationDetails buildDetails(HttpServletRequest request) {
        return new RoleWebAuthenticationDetails(request);
    }
}
