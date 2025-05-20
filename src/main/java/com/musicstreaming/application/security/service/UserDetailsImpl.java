package com.musicstreaming.application.security.service;

import com.musicstreaming.application.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of {@link UserDetails} to provide user information
 * to Spring Security during authentication and authorization processes.
 *
 * Wraps the {@link User} entity into a UserDetails object used by Spring Security.
 */
@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    /**
     * The unique identifier of the user.
     */
    Long id;

    /**
     * The user's email address.
     */
    String email;

    /**
     * The user's username.
     */
    String username;

    /**
     * The user's hashed password.
     */
    String password;

    /**
     * The authorities granted to the user (e.g., roles).
     */
    Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a new {@code UserDetailsImpl} with given user data.
     *
     * @param id          the user's unique ID
     * @param email       the user's email address
     * @param username    the user's username
     * @param password    the user's password hash
     * @param authorities the user's granted authorities
     */
    public UserDetailsImpl(Long id, String email, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Creates a {@code UserDetailsImpl} instance from a {@link User} entity.
     * Converts the user's role to a {@link SimpleGrantedAuthority}.
     *
     * @param user the user entity to convert.
     * @return a {@code UserDetailsImpl} instance representing the user.
     */
    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     * Always returns {@code true} here meaning account is not expired.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * Always returns {@code true} here meaning account is not locked.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * Always returns {@code true} here meaning credentials are valid.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * Always returns {@code true} here meaning user is enabled.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
