package com.musicstreaming.application.repository;

import com.musicstreaming.application.model.ListeningHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    @Query("SELECT h.artist FROM ListeningHistory h WHERE h.username = :user GROUP BY h.artist ORDER BY COUNT(h.artist) DESC")
    List<String> findTopArtistsByUser(@Param("user") String username, Pageable pageable);

    @Query("SELECT h.genre FROM ListeningHistory h WHERE h.username = :user GROUP BY h.genre ORDER BY COUNT(h.genre) DESC")
    List<String> findTopGenresByUser(@Param("user") String username, Pageable pageable);

    ListeningHistory findByArtist(String artist);

    List<ListeningHistory> findByUsername(String username);
}
