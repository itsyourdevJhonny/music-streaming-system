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

@Slf4j
@Service
public class SpotifyService {
    private String accessToken;
    private final OkHttpClient client = new OkHttpClient();

    public SpotifyService() {
        refreshAccessToken();
    }

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
            log.error("Error:", e);
        }
    }

    public JsonNode search(String query, String type) {
        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=" + type + "&limit=10";
        return getJsonAuthorizationMapper(url);
    }

    public JsonNode getCategories() {
        String url = "https://api.spotify.com/v1/browse/categories?limit=50";
        return getJsonAuthorizationMapper(url);
    }

    public JsonNode getPlaylistsByCategory(String categoryId) {
        String url = "https://api.spotify.com/v1/browse/categories/" + categoryId + "/playlists";
        return getJsonAuthorizationMapper(url);
    }

    public JsonNode getNewReleases(int offset) {
        String url = "https://api.spotify.com/v1/browse/new-releases?limit=30&offset=" + offset;
        return getJsonAuthorizationMapper(url);
    }

    public JsonNode getArtist(String artistId) {
        String url = "https://api.spotify.com/v1/artists/" + artistId;
        return getJsonAuthorizationMapper(url);
    }

    public JsonNode getRelatedArtists(String artistId) {
        String url = "https://api.spotify.com/v1/artists/" + artistId + "/related-artists";
        return getJsonAuthorizationMapper(url);
    }

    public String searchArtistIdByName(String artistName) {
        String encodedName = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + artistName + "&type=artist&limit=1";

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
            log.error("Error:", e);
        }

        return null;
    }

    public JsonNode getTrackByTitleAndArtist(String title, String artist) {
        String query = String.format("track:%s artist:%s", title, artist);
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encoded + "&type=track&limit=1";

        return getJsonAuthorizationMapper(url);
    }

    public JsonNode getFeaturedPlaylists() {
        String url = "https://api.spotify.com/v1/browse/featured-playlists?limit=10";
        return getJsonAuthorizationMapper(url);
    }

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
            log.error("Error:", e);
            return null;
        }
    }
}
