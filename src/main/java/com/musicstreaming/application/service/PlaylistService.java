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

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepo;
    @Autowired private PlaylistSongRepository songRepo;
    @Autowired private SecurityService securityService;
    @Autowired private UserService userService;

    public void createPlaylist(String name) {
        User user = userService.getUserByUsername(securityService.getAuthenticatedUser().getUsername());

        Playlist p = new Playlist();
        p.setName(name);
        p.setUser(user);
        playlistRepo.save(p);
    }

    public void deletePlaylist(Long id) {
        playlistRepo.deleteById(id);
    }

    public void addSongToPlaylist(Long playlistId, PlaylistSong song) {
        Playlist playlist = playlistRepo.findById(playlistId).orElseThrow();

        List<PlaylistSong> songs = songRepo.findByPlaylistId(playlist.getId());

        song.setPlaylist(playlist);
        song.setTrackOrder(songs.size());

        songs.add(song);

        playlist.setSongs(songs);
        playlistRepo.save(playlist);
    }

    public void removeSong(Long songId) {
        songRepo.deleteById(songId);
    }

    public void deleteSongByTitleAndArtist(String title, String artist) {
        songRepo.deleteByTitleAndArtist(title, artist);
    }

    public void reorderSong(Long playlistId, Long songId, int newPosition) {
        List<PlaylistSong> songs = songRepo.findByPlaylistIdOrderByTrackOrder(playlistId);
        PlaylistSong movedSong = songs.stream().filter(s -> s.getId().equals(songId)).findFirst().orElseThrow();
        songs.remove(movedSong);
        songs.add(newPosition, movedSong);
        for (int i = 0; i < songs.size(); i++) {
            songs.get(i).setTrackOrder(i);
        }
        songRepo.saveAll(songs);
    }

    public List<Playlist> getAllPlaylistsByUser(Long userId) {
        return playlistRepo.findAllWithSongs(userId);
    }

    public List<PlaylistSong> getSongsByPlaylistId(Long playlistId) {
        return songRepo.findByPlaylistId(playlistId);
    }

    public void updatePlaylist(Playlist playlist) {
        playlistRepo.save(playlist);
    }
}

