package com.musicstreaming.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a record of a user's listening history in the music streaming application.
 * This entity captures the details of each listening event such as the song title,
 * artist, genre, and the time the song was played.
 *
 * Mapped to a database table using JPA annotations.
 * Lombok's @Getter and @Setter annotations automatically generate
 * getter and setter methods for all fields.
 */
@Getter
@Setter
@Entity
public class ListeningHistory {

    /**
     * The unique identifier for each listening history record.
     * It's auto-generated using the IDENTITY strategy, which relies on
     * the database to generate the value upon insert.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The username of the user who listened to the song.
     */
    private String username;

    /**
     * The title of the song that was played.
     */
    private String songTitle;

    /**
     * The name of the artist of the song.
     */
    private String artist;

    /**
     * The genre of the song.
     */
    private String genre;

    /**
     * The date and time when the song was played.
     */
    private LocalDateTime timestamp;
}
