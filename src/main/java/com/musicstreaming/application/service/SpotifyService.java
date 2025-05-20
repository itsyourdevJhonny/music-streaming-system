package com.musicstreaming.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service class to interact with the Spotify Web API.
 * Handles authentication, search, and retrieval of various Spotify resources.
 */
@Slf4j
@Service
public class SpotifyService {
    private String accessToken;
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Initializes the service and refreshes the Spotify API access token.
     */
    public SpotifyService() {
        refreshAccessToken();
    }

    /**
     * Retrieves a new Spotify access token using the Client Credentials Flow.
     * Uses client ID and client secret to authenticate.
     * Stores the access token for subsequent API calls.
     */
    private void refreshAccessToken() {
        String clientId = "4a0dcee452f04485ab08b03803d186b6";
        String clientSecret = "b26627e7e9f74ea0898cd74f3bda3c68";
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .addHeader("Authorization", "Basic " + encodedAuth)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ObjectMapper mapper = new ObjectMapper();
            assert response.body() != null;
            JsonNode node = mapper.readTree(response.body().string());
            accessToken = node.get("access_token").asText();
        } catch (IOException e) {
            log.error("Error refreshing Spotify access token:", e);
        }
    }

    /**
     * Performs a search query on Spotify for the specified type (track, artist, album, etc.).
     *
     * @param query the search query string
     * @param type  the type of Spotify resource to search for (e.g., "track", "artist")
     * @return a JsonNode containing the search results, or null if an error occurs
     */
    public JsonNode search(String query, String type) {
        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=" + type + "&limit=10";
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Retrieves a list of Spotify categories.
     *
     * @return a JsonNode containing categories data
     */
    public JsonNode getCategories() {
        String url = "https://api.spotify.com/v1/browse/categories?limit=50";
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Retrieves playlists for a given Spotify category ID.
     *
     * @param categoryId the Spotify category ID
     * @return a JsonNode containing playlists of the category
     */
    public JsonNode getPlaylistsByCategory(String categoryId) {
        String url = "https://api.spotify.com/v1/browse/categories/" + categoryId + "/playlists";
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Retrieves new album releases from Spotify with pagination support.
     *
     * @param offset the offset for pagination (number of items to skip)
     * @return a JsonNode containing new release data
     */
    public JsonNode getNewReleases(int offset) {
        String url = "https://api.spotify.com/v1/browse/new-releases?limit=30&offset=" + offset;
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Retrieves detailed information about an artist by Spotify artist ID.
     *
     * @param artistId the Spotify artist ID
     * @return a JsonNode containing artist information
     */
    public JsonNode getArtist(String artistId) {
        String url = "https://api.spotify.com/v1/artists/" + artistId;
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Retrieves related artists to a given artist ID.
     *
     * @param artistId the Spotify artist ID
     * @return a JsonNode containing related artists data
     */
    public JsonNode getRelatedArtists(String artistId) {
        String url = "https://api.spotify.com/v1/artists/" + artistId + "/related-artists";
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Searches for an artist by name and returns their Spotify artist ID.
     *
     * @param artistName the name of the artist to search
     * @return the Spotify artist ID if found, or null otherwise
     */
    public String searchArtistIdByName(String artistName) {
        String encodedName = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encodedName + "&type=artist&limit=1";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ObjectMapper mapper = new ObjectMapper();
            assert response.body() != null;
            JsonNode result = mapper.readTree(response.body().string());
            JsonNode items = result.path("artists").path("items");

            if (items.isArray() && !items.isEmpty()) {
                return items.get(0).path("id").asText();
            }
        } catch (IOException e) {
            log.error("Error searching artist ID by name:", e);
        }

        return null;
    }

    /**
     * Searches for a track by title and artist name.
     *
     * @param title  the track title
     * @param artist the artist name
     * @return a JsonNode containing track information
     */
    public JsonNode getTrackByTitleAndArtist(String title, String artist) {
        String query = String.format("track:%s artist:%s", title, artist);
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encoded + "&type=track&limit=1";

        return getJsonAuthorizationMapper(url);
    }

    /**
     * Retrieves Spotify's featured playlists.
     *
     * @return a JsonNode containing featured playlists data
     */
    public JsonNode getFeaturedPlaylists() {
        String url = "https://api.spotify.com/v1/browse/featured-playlists?limit=10";
        return getJsonAuthorizationMapper(url);
    }

    /**
     * Helper method to perform a GET request with Bearer token authorization
     * and parse the response JSON into a JsonNode.
     *
     * @param url the endpoint URL to fetch
     * @return a JsonNode of the response body, or null if an error occurs
     */
    public JsonNode getJsonAuthorizationMapper(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ObjectMapper mapper = new ObjectMapper();
            assert response.body() != null;
            return mapper.readTree(response.body().string());
        } catch (IOException e) {
            log.error("Error fetching JSON from Spotify API:", e);
            return null;
        }
    }
}