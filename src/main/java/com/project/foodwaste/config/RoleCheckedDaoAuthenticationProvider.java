package com.project.foodwaste.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleCheckedDaoAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(RoleCheckedDaoAuthenticationProvider.class);

    private final DaoAuthenticationProvider delegate;

    public RoleCheckedDaoAuthenticationProvider(DaoAuthenticationProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.debug("Authenticating user: {}", authentication.getName());
        Authentication auth = delegate.authenticate(authentication);
        logger.debug("User authenticated: {} with authorities: {}", auth.getName(), auth.getAuthorities());
        
        Object details = authentication.getDetails();

        if (details instanceof RoleWebAuthenticationDetails) {
            RoleWebAuthenticationDetails roleDetails = (RoleWebAuthenticationDetails) details;
            String selectedRole = roleDetails.getRole();
            logger.debug("Selected role from form: {}", selectedRole);
            
            if (selectedRole != null && !selectedRole.isBlank()) {
                String expectedAuthority = "ROLE_" + selectedRole;
                boolean authorized = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(expectedAuthority::equals);

                logger.debug("Checking authority: {} | Match found: {}", expectedAuthority, authorized);

                if (!authorized) {
                    logger.warn("Role mismatch: User role {} does not match selected role {}", 
                            auth.getAuthorities(), selectedRole);
                    throw new BadCredentialsException("Invalid role selection");
                }
            }
        }

        logger.info("Authentication successful for user: {}", auth.getName());
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}
