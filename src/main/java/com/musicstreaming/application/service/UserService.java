package com.musicstreaming.application.service;

import com.musicstreaming.application.model.User;
import com.musicstreaming.application.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for user-related operations such as registration,
 * password encryption, and retrieval of user details.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService() {
        // Default constructor
    }

    /**
     * Registers a new user with the specified username and raw password.
     * The password will be encrypted before saving to the database.
     *
     * @param username the username of the new user
     * @param password the raw password to be encrypted and stored
     */
    public void registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword(password));
        userRepository.save(user);
    }

    /**
     * Retrieves the currently logged-in user based on the email stored in VaadinSession.
     *
     * @return the {@link User} object of the currently authenticated user,
     * or null if no user is logged in
     */
    public User findCurrentUser() {
        String userEmail = (String) VaadinSession.getCurrent().getAttribute("user");

        if (userEmail != null) {
            return userRepository.findByEmail(userEmail);
        }
        return null;
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the {@link User} if found, or null otherwise
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Encrypts a raw password using the configured password encoder.
     *
     * @param password the raw password to encrypt
     * @return the encrypted password string
     */
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}