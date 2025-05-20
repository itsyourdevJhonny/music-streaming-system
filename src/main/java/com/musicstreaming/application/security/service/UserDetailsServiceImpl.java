package com.musicstreaming.application.security.service;

import com.musicstreaming.application.model.User;
import com.musicstreaming.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserDetailsService} to load user-specific data
 * during authentication. This service retrieves {@link User} entities
 * from the database using {@link UserRepository} and converts them
 * into {@link UserDetails} used by Spring Security.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Locates the user based on the provided username.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated {@link UserDetails} instance (never {@code null}).
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserDetailsImpl.build(user);
    }
}