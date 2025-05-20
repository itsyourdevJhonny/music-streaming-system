package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link PlaylistSong} entities.
 * Provides methods for retrieving, ordering, and deleting songs within playlists.
 *
 * Extends {@link JpaRepository} to provide built-in CRUD functionality.
 */
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    /**
     * Retrieves all songs from a specific playlist, ordered by their track order.
     *
     * @param playlistId the ID of the playlist.
     * @return a list of {@link PlaylistSong} ordered by the {@code trackOrder} field.
     */
    List<PlaylistSong> findByPlaylistIdOrderByTrackOrder(Long playlistId);

    /**
     * Retrieves all songs from a specific playlist without enforcing any order.
     *
     * @param playlistId the ID of the playlist.
     * @return a list of {@link PlaylistSong} in their default database order.
     */
    List<PlaylistSong> findByPlaylistId(Long playlistId);

    /**
     * Deletes a song entry from any playlist using the song's title and artist.
     *
     * Use with caution as this may affect multiple playlists if songs with the same
     * title and artist exist in more than one playlist.
     *
     * @param title  the title of the song to be deleted.
     * @param artist the artist of the song to be deleted.
     */
    void deleteByTitleAndArtist(String title, String artist);

    /**
     * Finds songs by the name of the playlist they belong to.
     *
     * Assumes a derived property or query from the relationship between PlaylistSong and Playlist.
     * May require a custom query or join in practice depending on your JPA mapping.
     *
     * @param name the name of the playlist.
     * @return a list of {@link PlaylistSong} that belong to the specified playlist.
     */
    List<PlaylistSong> findByPlaylistName(String name);  // May not work unless 'playlist.name' is mapped or queried explicitly
}
