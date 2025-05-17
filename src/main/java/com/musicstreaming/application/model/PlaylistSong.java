package com.musicstreaming.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlaylistSong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String spotifyUrl;
    private String coverImage;
    private int trackOrder;

    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;
}

