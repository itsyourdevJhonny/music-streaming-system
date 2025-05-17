package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylistIdOrderByTrackOrder(Long playlistId);
    List<PlaylistSong> findByPlaylistId(Long playlistId);

    void deleteByTitleAndArtist(String title, String artist);
    List<PlaylistSong> findByPlaylistName(String name);
}

