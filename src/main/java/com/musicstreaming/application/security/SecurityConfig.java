package com.musicstreaming.application.security;

import com.musicstreaming.application.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration class for the music streaming application.
 * Integrates Spring Security with Vaadin and defines access rules,
 * login view, and authentication settings.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    /**
     * Configures HTTP security rules for the application.
     * Allows public access to specific static resources (like images),
     * and sets up the login view using Vaadin.
     *
     * @param http the {@link HttpSecurity} object used to define web-based security.
     * @throws Exception if an error occurs during configuration.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png")
                ).permitAll()  // Allow public access to image files
        );

        // Call Vaadin's default security configuration
        super.configure(http);

        // Define the custom login view class used for authentication
        setLoginView(http, LoginView.class);
    }

    /**
     * Configures and exposes an {@link AuthenticationManager} bean.
     * This manager is used to process authentication requests.
     *
     * @param httpSecurity the current {@link HttpSecurity} context.
     * @return an {@link AuthenticationManager} instance.
     * @throws Exception if building the manager fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder authBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        return authBuilder.build();
    }

    /**
     * Defines a {@link PasswordEncoder} bean to be used for hashing passwords.
     * Uses BCrypt, which is a strong and secure hashing algorithm.
     *
     * @return a BCrypt-based {@link PasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
