package com.musicstreaming.application.service;

import com.musicstreaming.application.model.User;
import com.musicstreaming.application.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService() {
    }

    public void registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword(password));
        userRepository.save(user);
    }

    public User findCurrentUser() {
        String userEmail = (String) VaadinSession.getCurrent().getAttribute("user");

        if (userEmail != null) {
            return userRepository.findByEmail(userEmail);
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
