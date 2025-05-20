package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Provides methods for querying users by username or email.
 *
 * Extends {@link JpaRepository} to support CRUD operations and pagination.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for.
     * @return an {@link Optional} containing the user if found, or empty if not.
     */
    Optional<User> findByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address to search for.
     * @return the {@link User} entity if found, otherwise {@code null}.
     */
    User findByEmail(String email);
}
