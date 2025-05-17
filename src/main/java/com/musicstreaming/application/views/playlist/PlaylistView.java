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

    public PlaylistView(SecurityService securityService, UserService userService, PlaylistService service) {
        this.securityService = securityService;
        this.userService = userService;
        this.playlistService = service;

        String username = securityService.getAuthenticatedUser().getUsername();
        user = userService.getUserByUsername(username);

        playlistField.setSuffixComponent(new Icon("vaadin", "music"));
        playlistField.setPlaceholder("Enter new playlist name");
        playlistField.addKeyPressListener(Key.ENTER, e -> {
            String name = playlistField.getValue();

            if (name.isEmpty() || name.matches("\\s*")) {
                Notification.show("Playlist name cannot be blank.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                playlistService.createPlaylist(name);

                Icon moreIcon = new Icon("vaadin", "ellipsis-dots-h");

                Div nameDiv = new Div(new Span(name), new Span("0 song/s"));

                HorizontalLayout playlistLayout = new HorizontalLayout(new Icon("vaadin", "music"), nameDiv, moreIcon);
                playlistLayout.addClassName("playlist-layout");

                parentLayout.add(playlistLayout);

                playlistField.clear();

                Notification.show("Playlist [" + name + "] created.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        if (playlistService.getAllPlaylistsByUser(user.getId()).isEmpty()) {
            Div noPlaylistDiv = new Div(getSvgIcon("empty-music"), new Span("No Playlist Yet."));
            noPlaylistDiv.addClassName("no-playlist-div");
            setContent(noPlaylistDiv);
        } else {
            createContent();
        }

        createHeader();
    }

    private void createContent() {
        parentLayout.addClassName("playlist-parent-layout");

        Div mainLayout = new Div();
        mainLayout.addClassName("playlist-main-layout");

        List<Playlist> playlists = playlistService.getAllPlaylistsByUser(user.getId());

        for (Playlist playlist : playlists) {
            String name = playlist.getName();
            int songsSize = playlist.getSongs().size();

            Icon moreIcon = new Icon("vaadin", "ellipsis-dots-h");

            Span songSizeSpan = new Span();
            songSizeSpan.setText(String.valueOf(songsSize) + " song/s");

            Span nameSpan = new Span(name);

            Div nameDiv = new Div(nameSpan, songSizeSpan);

            HorizontalLayout playlistLayout = new HorizontalLayout(new Icon("vaadin", "music"), nameDiv, moreIcon);
            playlistLayout.addClassName("playlist-layout");
            playlistLayout.addClickListener(e -> displaySongs(playlist, songSizeSpan));

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
                            nameSpan.setText(editField.getValue());
                            playlist.setName(editField.getValue());
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

    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassName("playlist-header-title");

        Button createButton = new Button("Create Playlist", new Icon("vaadin", "plus"));

        Icon closeIcon = new Icon("vaadin", "close");

        Div playlistFieldDiv = new Div(playlistField, closeIcon);

        closeIcon.addClickListener(e -> {
            headerLayout.remove(playlistFieldDiv);
            headerLayout.addComponentAtIndex(1, createButton);
        });

        createButton.addClickListener(e -> {
            headerLayout.remove(createButton);
            headerLayout.addComponentAtIndex(1, playlistFieldDiv);
        });

        Button backButton = new Button("Back", new Icon("vaadin", "arrow-backward"), e -> UI.getCurrent().navigate(HomeView.class));

        headerLayout.add(backButton, createButton, new Span("Playlist"), getSvgIcon("logo"));

        addToNavbar(headerLayout);
    }

    private Div createCard(Span songSizeSpan, Long playlistId, String title, String subtitle, String imageUrl, String spotifyId, String type) {
        Image image = new Image(imageUrl, "cover");
        image.setWidth("150px");
        image.setHeight("150px");

        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle().set("font-weight", "bold");

        Div subtitleDiv = new Div();
        subtitleDiv.setText(subtitle);
        subtitleDiv.getStyle().set("font-size", "small").set("color", "#555");

        String iframeHtml = String.format(
                "<iframe style='border-radius:12px' src='https://open.spotify.com/embed/%s/%s?utm_source=generator' " +
                        "width='100%%' height='80' frameborder='0' allow='autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture' loading='lazy'></iframe>",
                type, spotifyId
        );

        Div detailsDiv = new Div(image, titleDiv, subtitleDiv);

        Div card = new Div(detailsDiv, new Html(iframeHtml));
        card.addClassName("library-card");
        card.getStyle().set("padding", "10px").set("border", "1px solid #ccc").set("border-radius", "8px").set("width", "300px").set("margin", "10px");
        card.add(createDeleteButton(songSizeSpan, playlistId, card, title, subtitle));

        return card;
    }

    private Button createDeleteButton(Span songSizeSpan, Long playlistId, Div card, String title, String artist) {
        Button deleteButton = new Button("Delete", new Icon("vaadin", "trash"), e -> {
            cardContainer.remove(card);
        });
        deleteButton.addClassName("playlist-delete-button");
        return deleteButton;
    }

    private SvgIcon getSvgIcon(String iconName) {
        return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + iconName + ".svg")));
    }
}

