package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.ListeningHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link ListeningHistory} entities.
 * Provides custom queries and methods for retrieving user-specific
 * listening data such as top artists, top genres, and history records.
 *
 * Extends {@link JpaRepository} to provide standard CRUD operations.
 */
@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {

    /**
     * Retrieves the most frequently listened-to artists for a given user.
     *
     * @param username the username to query listening history for.
     * @param pageable pageable object to limit the number of results (e.g., top 5).
     * @return a list of artist names sorted by listen count (descending).
     */
    @Query("SELECT h.artist FROM ListeningHistory h WHERE h.username = :user GROUP BY h.artist ORDER BY COUNT(h.artist) DESC")
    List<String> findTopArtistsByUser(@Param("user") String username, Pageable pageable);

    /**
     * Retrieves the most frequently listened-to genres for a given user.
     *
     * @param username the username to query listening history for.
     * @param pageable pageable object to limit the number of results (e.g., top 5).
     * @return a list of genre names sorted by listen count (descending).
     */
    @Query("SELECT h.genre FROM ListeningHistory h WHERE h.username = :user GROUP BY h.genre ORDER BY COUNT(h.genre) DESC")
    List<String> findTopGenresByUser(@Param("user") String username, Pageable pageable);

    /**
     * Finds a single listening history record by artist name.
     *
     * @param artist the name of the artist.
     * @return the first matching {@link ListeningHistory} record, or null if none found.
     */
    ListeningHistory findByArtist(String artist);

    /**
     * Retrieves all listening history records associated with a specific user.
     *
     * @param username the username to search for.
     * @return a list of {@link ListeningHistory} records for the given user.
     */
    List<ListeningHistory> findByUsername(String username);
}
