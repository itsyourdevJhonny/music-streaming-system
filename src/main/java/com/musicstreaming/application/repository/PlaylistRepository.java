package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.user.id = :userId")
    List<Playlist> findAllWithSongs(Long userId);
}