package com.musicstreaming.application.views.playlist;

import com.musicstreaming.application.model.Playlist;
import com.musicstreaming.application.model.PlaylistSong;
import com.musicstreaming.application.model.User;
import com.musicstreaming.application.security.SecurityService;
import com.musicstreaming.application.service.PlaylistService;
import com.musicstreaming.application.service.UserService;
import com.musicstreaming.application.views.HomeView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * PlaylistView class responsible for displaying and managing user playlists.
 *
 * <p>This view allows the user to create new playlists, edit or delete existing ones,
 * and view songs within a selected playlist. It extends {@link AppLayout} for
 * providing a layout with navigation capabilities.</p>
 *
 * <p>Access to this view is permitted to all authenticated users via {@link PermitAll}.</p>
 *
 * @author
 */
@PermitAll
@Route("playlists")
@PageTitle("Playlist | MSS")
public class PlaylistView extends AppLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final PlaylistService playlistService;

    private final TextField playlistField = new TextField();
    private final VerticalLayout parentLayout = new VerticalLayout();
    private final Div cardContainer = new Div();
    private final User user;

    /**
     * Constructs the PlaylistView and initializes the user interface.
     *
     * @param securityService the service providing security and authentication information
     * @param userService     the service for user-related operations
     * @param service         the service for playlist-related operations
     */
    public PlaylistView(SecurityService securityService, UserService userService, PlaylistService service) {
        this.securityService = securityService;
        this.userService = userService;
        this.playlistService = service;

        // Get currently authenticated user
        String username = securityService.getAuthenticatedUser().getUsername();
        user = userService.getUserByUsername(username);

        // Configure the playlist input field with icon and placeholder
        playlistField.setSuffixComponent(new Icon("vaadin", "music"));
        playlistField.setPlaceholder("Enter new playlist name");

        // Add listener for pressing Enter key on playlistField to create new playlist
        playlistField.addKeyPressListener(Key.ENTER, e -> {
            String name = playlistField.getValue();

            if (name.isEmpty() || name.matches("\\s*")) {
                Notification.show("Playlist name cannot be blank.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                // Create playlist via service
                playlistService.createPlaylist(name);

                // UI feedback and update layout with new playlist entry
                Icon moreIcon = new Icon("vaadin", "ellipsis-dots-h");
                Div nameDiv = new Div(new Span(name), new Span("0 song/s"));
                HorizontalLayout playlistLayout = new HorizontalLayout(new Icon("vaadin", "music"), nameDiv, moreIcon);
                playlistLayout.addClassName("playlist-layout");
                parentLayout.add(playlistLayout);

                playlistField.clear();

                Notification.show("Playlist [" + name + "] created.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        // Show no playlists message or load playlists if they exist
        if (playlistService.getAllPlaylistsByUser(user.getId()).isEmpty()) {
            Div noPlaylistDiv = new Div(getSvgIcon("empty-music"), new Span("No Playlist Yet."));
            noPlaylistDiv.addClassName("no-playlist-div");
            setContent(noPlaylistDiv);
        } else {
            createContent();
        }

        createHeader();
    }

    /**
     * Creates and loads the main content of the playlist view,
     * including the list of playlists and their interaction logic.
     */
    private void createContent() {
        parentLayout.addClassName("playlist-parent-layout");

        Div mainLayout = new Div();
        mainLayout.addClassName("playlist-main-layout");

        // Fetch playlists for the current user
        List<Playlist> playlists = playlistService.getAllPlaylistsByUser(user.getId());

        for (Playlist playlist : playlists) {
            String name = playlist.getName();
            int songsSize = playlist.getSongs().size();

            Icon moreIcon = new Icon("vaadin", "ellipsis-dots-h");

            Span songSizeSpan = new Span();
            songSizeSpan.setText(songsSize + " song/s");

            Span nameSpan = new Span(name);

            Div nameDiv = new Div(nameSpan, songSizeSpan);

            HorizontalLayout playlistLayout = new HorizontalLayout(new Icon("vaadin", "music"), nameDiv, moreIcon);
            playlistLayout.addClassName("playlist-layout");

            // On clicking the playlist, display its songs
            playlistLayout.addClickListener(e -> displaySongs(playlist, songSizeSpan));

            // More icon opens dialog with edit/delete options
            moreIcon.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.addClassName("playlist-dialog");
                dialog.getHeader().add(new Div(new Span("Choose an action"), new Span(playlist.getName())));
                dialog.open();

                Button deleteButton = new Button("Delete", new Icon("vaadin", "edit"));
                deleteButton.addClickListener(event -> {
                    playlistService.deletePlaylist(playlist.getId());
                    parentLayout.remove(playlistLayout);
                    cardContainer.removeAll();
                    dialog.close();
                });

                Button editButton = new Button("Edit", new Icon("vaadin", "trash"));
                editButton.addClickListener(event -> {
                    dialog.close();

                    Dialog editDialog = new Dialog();
                    editDialog.addClassName("playlist-edit-dialog");
                    editDialog.getHeader().add(new Span("Edit Playlist"));
                    editDialog.open();

                    TextField editField = new TextField("Enter new Playlist name");
                    editField.setPlaceholder("Current name: " + playlist.getName());

                    Button doneButton = new Button("Done", new Icon("vaadin", "check"), event1 -> {
                        String value = editField.getValue();
                        if (value.isEmpty() || value.matches("\\s*")) {
                            Notification.show("Playlist name cannot be blank.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                        } else {
                            editDialog.close();
                            nameSpan.setText(value);
                            playlist.setName(value);
                            playlistService.updatePlaylist(playlist);

                            Notification.show("Playlist name changed successfully.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        }
                    });

                    editDialog.add(editField, doneButton);
                });

                dialog.add(deleteButton, editButton);
            });

            parentLayout.add(playlistLayout);
        }

        mainLayout.add(parentLayout, cardContainer);
        setContent(mainLayout);
    }

    /**
     * Displays the songs inside a selected playlist.
     *
     * @param playlist     the playlist whose songs are to be displayed
     * @param songSizeSpan the UI element showing the count of songs in the playlist, updated dynamically
     */
    private void displaySongs(Playlist playlist, Span songSizeSpan) {
        cardContainer.removeAll();

        Span headerText = new Span(new Icon("vaadin", "music"), new Span(playlist.getName()));
        headerText.addClassName("playlist-songs-header-text");
        cardContainer.add(headerText);

        List<PlaylistSong> songs = playlistService.getSongsByPlaylistId(playlist.getId());

        if (!songs.isEmpty()) {
            songs.forEach(playlistSong -> {
                String name = playlistSong.getTitle();
                String artist = playlistSong.getArtist();
                String imageUrl = playlistSong.getCoverImage();
                String albumId = playlistSong.getSpotifyUrl();

                cardContainer.add(createCard(songSizeSpan, playlistSong.getPlaylist().getId(), name, "by " + artist, imageUrl, albumId, "album"));
            });
        } else {
            Span noSongText = new Span("No song/s");
            noSongText.addClassName("playlist-no-song-text");
            cardContainer.add(noSongText);
        }
    }

    /**
     * Creates the header bar with navigation and controls such as "Create Playlist" and "Back" buttons.
     */
    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassName("playlist-header-title");

        Button createButton = new Button("Create Playlist", new Icon("vaadin", "plus"));
        Icon closeIcon = new Icon("vaadin", "close");
        Div playlistFieldDiv = new Div(playlistField, closeIcon);

        // Close button hides playlist input and shows "Create Playlist" button again
        closeIcon.addClickListener(e -> {
            headerLayout.remove(playlistFieldDiv);
            headerLayout.addComponentAtIndex(1, createButton);
        });

        // Create button shows the playlist input field for new playlist name
        createButton.addClickListener(e -> {
            headerLayout.remove(createButton);
            headerLayout.addComponentAtIndex(1, playlistFieldDiv);
        });

        Button backButton = new Button("Back", new Icon("vaadin", "arrow-backward"), e -> UI.getCurrent().navigate(HomeView.class));

        headerLayout.add(backButton, createButton, new Span("Playlist"), getSvgIcon("logo"));

        addToNavbar(headerLayout);
    }

    /**
     * Creates a card component representing a song with album art, title, artist, and embedded Spotify player.
     *
     * @param songSizeSpan the span displaying the number of songs (may be updated on song delete)
     * @param playlistId   the ID of the playlist containing the song
     * @param title        the song title
     * @param subtitle     the song artist or subtitle text
     * @param imageUrl     the URL of the song's cover image
     * @param spotifyId    the Spotify album/track ID to embed the player
     * @param type         the Spotify embed type (e.g. "album", "track")
     * @return the UI component representing the song card
     */
    private Div createCard(Span songSizeSpan, Long playlistId, String title, String subtitle, String imageUrl, String spotifyId, String type) {
        Image image = new Image(imageUrl, "cover");
        image.setWidth("150px");
        image.setHeight("150px");

        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle().set("font-weight", "bold");

        Div subtitleDiv = new Div();
        subtitleDiv.setText(subtitle);

        String iframeHtml = String.format(
                "<iframe style='border-radius:12px' src='https://open.spotify.com/embed/%s/%s?utm_source=generator' " +
                        "width='100%%' height='80' frameborder='0' allow='autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture' loading='lazy'></iframe>",
                type, spotifyId
        );

        Div detailsDiv = new Div(image, titleDiv, subtitleDiv);

        Div card = new Div(detailsDiv, new Html(iframeHtml));
        card.addClassName("library-card");
        card.getStyle()
                .set("padding", "10px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("width", "300px")
                .set("margin", "10px");

        // Add delete button to remove the song card from the UI
        card.add(createDeleteButton(songSizeSpan, playlistId, card, title, subtitle));

        return card;
    }

    /**
     * Creates a delete button for removing a song card from the UI.
     * (Note: Currently only removes from UI, no backend delete implemented.)
     *
     * @param songSizeSpan the span showing the number of songs, potentially for update
     * @param playlistId   the playlist id the song belongs to
     * @param card         the card UI component to be removed
     * @param title        the song title (for potential confirmation)
     * @param artist       the artist (for potential confirmation)
     * @return the delete button component
     */
    private Button createDeleteButton(Span songSizeSpan, Long playlistId, Div card, String title, String artist) {
        Button deleteButton = new Button("Delete", new Icon("vaadin", "trash"), e -> {
            // Currently removes card visually. To fully delete, integrate playlistService.deleteSong(...) here.
            cardContainer.remove(card);
        });
        deleteButton.addClassName("playlist-delete-button");
        return deleteButton;
    }

    /**
     * Utility method to load SVG icons from resources.
     *
     * @param iconName the base name of the SVG file (without extension)
     * @return an SvgIcon instance for the specified icon
     */
    private SvgIcon getSvgIcon(String iconName) {
        return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + iconName + ".svg")));
    }
}