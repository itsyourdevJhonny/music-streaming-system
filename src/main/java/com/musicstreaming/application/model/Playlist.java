package com.musicstreaming.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("trackOrder ASC")
    private List<PlaylistSong> songs = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

