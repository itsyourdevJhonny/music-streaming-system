package com.musicstreaming.application.service;

import com.musicstreaming.application.model.Playlist;
import com.musicstreaming.application.model.PlaylistSong;
import com.musicstreaming.application.model.User;
import com.musicstreaming.application.repository.PlaylistRepository;
import com.musicstreaming.application.repository.PlaylistSongRepository;
import com.musicstreaming.application.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for managing playlists and their songs.
 * Handles creation, deletion, updating of playlists, and operations
 * on songs within playlists such as adding, removing, and reordering.
 */
@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepo;

    @Autowired
    private PlaylistSongRepository songRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    /**
     * Creates a new playlist with the specified name for the currently authenticated user.
     *
     * @param name the name of the new playlist
     */
    public void createPlaylist(String name) {
        User user = userService.getUserByUsername(securityService.getAuthenticatedUser().getUsername());

        Playlist p = new Playlist();
        p.setName(name);
        p.setUser(user);
        playlistRepo.save(p);
    }

    /**
     * Deletes a playlist by its unique ID.
     *
     * @param id the ID of the playlist to delete
     */
    public void deletePlaylist(Long id) {
        playlistRepo.deleteById(id);
    }

    /**
     * Adds a new song to the specified playlist. The song's order is set
     * based on the current number of songs in the playlist.
     *
     * @param playlistId the ID of the playlist to add the song to
     * @param song       the song entity to add
     * @throws java.util.NoSuchElementException if the playlist is not found
     */
    public void addSongToPlaylist(Long playlistId, PlaylistSong song) {
        Playlist playlist = playlistRepo.findById(playlistId).orElseThrow();

        List<PlaylistSong> songs = songRepo.findByPlaylistId(playlist.getId());

        song.setPlaylist(playlist);
        song.setTrackOrder(songs.size());

        songs.add(song);

        playlist.setSongs(songs);
        playlistRepo.save(playlist);
    }

    /**
     * Removes a song from the repository by its unique ID.
     *
     * @param songId the ID of the song to remove
     */
    public void removeSong(Long songId) {
        songRepo.deleteById(songId);
    }

    /**
     * Deletes songs from playlists based on their title and artist.
     *
     * @param title  the title of the song to delete
     * @param artist the artist of the song to delete
     */
    public void deleteSongByTitleAndArtist(String title, String artist) {
        songRepo.deleteByTitleAndArtist(title, artist);
    }

    /**
     * Reorders a song within a playlist by moving it to a new position.
     * Adjusts the order of other songs accordingly.
     *
     * @param playlistId  the ID of the playlist containing the song
     * @param songId      the ID of the song to reorder
     * @param newPosition the new zero-based index position for the song
     * @throws java.util.NoSuchElementException if the song is not found in the playlist
     */
    public void reorderSong(Long playlistId, Long songId, int newPosition) {
        List<PlaylistSong> songs = songRepo.findByPlaylistIdOrderByTrackOrder(playlistId);
        PlaylistSong movedSong = songs.stream()
                .filter(s -> s.getId().equals(songId))
                .findFirst()
                .orElseThrow();
        songs.remove(movedSong);
        songs.add(newPosition, movedSong);
        for (int i = 0; i < songs.size(); i++) {
            songs.get(i).setTrackOrder(i);
        }
        songRepo.saveAll(songs);
    }

    /**
     * Retrieves all playlists belonging to a specific user, including their songs.
     *
     * @param userId the ID of the user
     * @return a list of playlists owned by the user, each with their songs loaded
     */
    public List<Playlist> getAllPlaylistsByUser(Long userId) {
        return playlistRepo.findAllWithSongs(userId);
    }

    /**
     * Retrieves all songs for a given playlist by playlist ID.
     *
     * @param playlistId the ID of the playlist
     * @return a list of songs in the specified playlist
     */
    public List<PlaylistSong> getSongsByPlaylistId(Long playlistId) {
        return songRepo.findByPlaylistId(playlistId);
    }

    /**
     * Updates an existing playlist's data.
     *
     * @param playlist the playlist entity with updated data to save
     */
    public void updatePlaylist(Playlist playlist) {
        playlistRepo.save(playlist);
    }
}
