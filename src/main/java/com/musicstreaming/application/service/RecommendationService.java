package com.musicstreaming.application.service;

import com.musicstreaming.application.model.ListeningHistory;
import com.musicstreaming.application.repository.ListeningHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Service for handling music recommendation-related operations.
 * It provides functionality to fetch user listening history,
 * top artists, top genres, and to add new listening history entries.
 */
@Service
public class RecommendationService {

    @Autowired
    ListeningHistoryRepository historyRepo;

    /**
     * Retrieves the complete listening history for a given user.
     *
     * @param username the username whose listening history is to be fetched
     * @return a list of {@link ListeningHistory} records for the user
     */
    public List<ListeningHistory> getSongsByUsername(String username) {
        return historyRepo.findByUsername(username);
    }

    /**
     * Retrieves the top artists listened to by the specified user,
     * limited by the given number of results.
     *
     * @param username the username to query top artists for
     * @param limit    maximum number of top artists to return
     * @return a list of artist names sorted by listening frequency in descending order
     */
    public List<String> getTopArtists(String username, int limit) {
        return historyRepo.findTopArtistsByUser(username, PageRequest.of(0, limit));
    }

    /**
     * Retrieves the top music genres listened to by the specified user,
     * limited by the given number of results.
     *
     * @param username the username to query top genres for
     * @param limit    maximum number of top genres to return
     * @return a list of genre names sorted by listening frequency in descending order
     */
    public List<String> getTopGenres(String username, int limit) {
        return historyRepo.findTopGenresByUser(username, PageRequest.of(0, limit));
    }

    /**
     * Adds a new listening history entry for a user and artist with a specific song title,
     * only if there is no existing entry for the artist.
     *
     * @param artist   the artist of the song listened to
     * @param username the username of the listener
     * @param title    the title of the song listened to
     */
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