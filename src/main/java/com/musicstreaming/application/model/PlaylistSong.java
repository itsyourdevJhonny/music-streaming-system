package com.musicstreaming.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a song within a playlist in the music streaming application.
 * Each PlaylistSong contains metadata about the song and a reference to the playlist it belongs to.
 *
 * This entity is used to manage individual song entries in playlists,
 * including the order in which songs appear.
 *
 * Lombok annotations (@Getter and @Setter) auto-generate standard accessor methods.
 */
@Entity
@Getter
@Setter
public class PlaylistSong {

    /**
     * The unique identifier for the playlist song entry.
     * This value is generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the song.
     */
    private String title;

    /**
     * The name of the artist who performed the song.
     */
    private String artist;

    /**
     * The URL to the song on Spotify.
     * This allows integration or linking to the actual streaming platform.
     */
    private String spotifyUrl;

    /**
     * A URL pointing to the cover image for the song.
     */
    private String coverImage;

    /**
     * The order in which this song appears in the playlist.
     * This field is used to sort songs within the playlist.
     */
    private int trackOrder;

    /**
     * The playlist to which this song belongs.
     *
     * This is a many-to-one relationship since multiple PlaylistSong entries
     * can reference the same playlist.
     *
     * - @JoinColumn(name = "playlist_id"): defines the foreign key column linking to the Playlist table.
     */
    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;
}
