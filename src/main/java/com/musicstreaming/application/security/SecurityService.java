package com.musicstreaming.application.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Security service to handle authentication-related operations such as
 * retrieving the currently authenticated user and performing logout.
 *
 * This service acts as a wrapper around Vaadin's {@link AuthenticationContext}
 * and simplifies access to security-related actions within the application.
 */
@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    /**
     * Constructs a new {@code SecurityService} with the given {@link AuthenticationContext}.
     *
     * @param authenticationContext the Vaadin authentication context used for security operations.
     */
    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return the {@link UserDetails} of the authenticated user.
     * @throws java.util.NoSuchElementException if no user is currently authenticated.
     */
    public UserDetails getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).get();
    }

    /**
     * Logs out the current user and clears their session.
     */
    public void logout() {
        authenticationContext.logout();
    }
}
