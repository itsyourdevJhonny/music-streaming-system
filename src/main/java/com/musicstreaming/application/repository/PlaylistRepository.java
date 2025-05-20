package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for accessing and managing {@link Playlist} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations.
 * Includes a custom query to efficiently retrieve playlists along with their associated songs.
 */
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /**
     * Retrieves all playlists belonging to a specific user along with their songs in a single query.
     *
     * This uses a {@code LEFT JOIN FETCH} to eagerly load the associated songs, avoiding the N+1 select problem.
     *
     * @param userId the ID of the user whose playlists are to be retrieved.
     * @return a list of playlists that belong to the specified user, each with its songs populated.
     */
    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.user.id = :userId")
    List<Playlist> findAllWithSongs(Long userId);
}
