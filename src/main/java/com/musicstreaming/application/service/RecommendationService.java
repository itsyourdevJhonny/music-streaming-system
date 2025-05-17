package com.musicstreaming.application.service;

import com.musicstreaming.application.model.ListeningHistory;
import com.musicstreaming.application.repository.ListeningHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    ListeningHistoryRepository historyRepo;

    public List<ListeningHistory> getSongsByUsername(String username) {
        return historyRepo.findByUsername(username);
    }

    public List<String> getTopArtists(String username, int limit) {
        return historyRepo.findTopArtistsByUser(username, PageRequest.of(0, limit));
    }

    public List<String> getTopGenres(String username, int limit) {
        return historyRepo.findTopGenresByUser(username, PageRequest.of(0, limit));
    }

    public void add(String artist, String username, String title) {
        ListeningHistory existingHistory = historyRepo.findByArtist(artist);

        if (existingHistory == null) {
            ListeningHistory history = new ListeningHistory();
            history.setArtist(artist);
            history.setUsername(username);
            history.setSongTitle(title);
            history.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Manila")));

            historyRepo.save(history);
        }
    }
}

