package com.musicstreaming.application.views;

import com.fasterxml.jackson.databind.JsonNode;
import com.musicstreaming.application.model.ListeningHistory;
import com.musicstreaming.application.security.SecurityService;
import com.musicstreaming.application.service.RecommendationService;
import com.musicstreaming.application.service.SpotifyService;
import com.musicstreaming.application.service.UserService;
import com.musicstreaming.application.views.library.MusicLibraryView;
import com.musicstreaming.application.views.playlist.PlaylistView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * Main application view shown at the root route ("/").
 * Displays music streaming homepage including recently played tracks,
 * recommendations, featured playlists, and footer with contact info.
 */
@Route("")
@PageTitle("Home | MSS")
@PermitAll
public class HomeView extends AppLayout {

    private final SpotifyService spotifyService;
    private final RecommendationService recommendationService;
    private final SecurityService securityService;
    private final UserService userService;

    /**
     * Creates the HomeView with required service dependencies.
     *
     * @param spotifyService       Spotify API integration service
     * @param recommendationService Service to get personalized recommendations
     * @param securityService       Security service to get authenticated user info
     * @param userService           Service to manage user data
     */
    public HomeView(SpotifyService spotifyService, RecommendationService recommendationService, SecurityService securityService, UserService userService) {
        this.spotifyService = spotifyService;
        this.recommendationService = recommendationService;
        this.securityService = securityService;
        this.userService = userService;
        addClassName("home-main-layout-2");

        createContent();
        addToNavbar(createHeader());
    }

    /**
     * Creates and sets the main content layout including title,
     * recently played, recommendations, featured playlists, and footer.
     */
    private void createContent() {
        Div mainLayout = new Div();
        mainLayout.addClassName("home-main-layout");

        Span title = new Span("Music Streaming System");

        Div footerDiv = new Div(new Span(description()), createFooter());

        mainLayout.add(title,
                new Span(getSvgIcon("logo"), footerDiv),
                createRecentlyPlayed(),
                createRecommendation(),
                createFeaturedPlaylists(),
                createCardFooter()
        );

        setContent(mainLayout);
    }

    /**
     * Creates footer layout for cards with copyright notice and logo.
     *
     * @return HorizontalLayout containing footer info
     */
    private HorizontalLayout createCardFooter() {
        HorizontalLayout footerLayout = new HorizontalLayout(new Span("All rights reserved @ 2025"), getSvgIcon("logo"));
        footerLayout.addClassName("home-card-footer");
        return footerLayout;
    }

    /**
     * Creates the "Recently Played" section cards for the authenticated user.
     *
     * @return Div container with recently played track cards
     */
    private Div createRecentlyPlayed() {
        String username = securityService.getAuthenticatedUser().getUsername();

        Div cardContainer = new Div(new Span("Recently played"));
        cardContainer.addClassName("home-card-container");
        cardContainer.getStyle().set("display", "flex").set("flex-wrap", "wrap");

        List<ListeningHistory> histories = recommendationService.getSongsByUsername(username);

        for (ListeningHistory history : histories) {
            String artist = history.getArtist().replace("by ", "");

            try {
                JsonNode trackResult = spotifyService.getTrackByTitleAndArtist(history.getSongTitle(), artist);

                if (trackResult != null) {
                    JsonNode track = trackResult.path("tracks").path("items").get(0);
                    if (track != null) {
                        String name = track.path("name").asText();
                        String artistName = track.path("artists").get(0).path("name").asText();
                        String imageUrl = track.path("album").path("images").get(0).path("url").asText();
                        String spotifyUrl = track.path("external_urls").path("spotify").asText();

                        cardContainer.add(createCard(name, "By " + artistName, imageUrl, spotifyUrl));
                    }
                } else {
                    cardContainer.add(new Span("No recently played yet."));
                    return cardContainer;
                }
            } catch (Exception e) {
                cardContainer.add(new Span("No recently played yet."));
                return cardContainer;
            }
        }

        return cardContainer;
    }

    /**
     * Creates the "Recommendation" section with personalized and new releases.
     *
     * @return Div container with recommended track cards
     */
    private Div createRecommendation() {
        String username = securityService.getAuthenticatedUser().getUsername();

        Div cardContainer = new Div(new Span("Recommendation"));
        cardContainer.addClassName("home-card-container");
        cardContainer.getStyle().set("display", "flex").set("flex-wrap", "wrap");

        List<ListeningHistory> histories = recommendationService.getSongsByUsername(username);

        for (ListeningHistory history : histories) {
            String artist = history.getArtist().replace("by ", "");

            try {
                JsonNode trackResult = spotifyService.getTrackByTitleAndArtist(history.getSongTitle(), artist);

                JsonNode track = trackResult.path("tracks").path("items").get(0);
                if (track != null) {
                    String name = track.path("name").asText();
                    String artistName = track.path("artists").get(0).path("name").asText();
                    String imageUrl = track.path("album").path("images").get(0).path("url").asText();
                    String spotifyUrl = track.path("external_urls").path("spotify").asText();

                    cardContainer.add(createCard(name, "By " + artistName, imageUrl, spotifyUrl));
                }
            } catch (Exception e) {
                cardContainer.add(new Span("No recently played yet."));
                return cardContainer;
            }
        }

        try {
            JsonNode albums = spotifyService.getNewReleases(1);
            if (albums != null) {
                for (JsonNode item : albums.path("albums").path("items")) {
                    String name = item.path("name").asText();
                    String artistName = item.path("artists").get(0).path("name").asText();
                    String imageUrl = item.path("images").get(0).path("url").asText();
                    String spotifyUrl = item.path("external_urls").path("spotify").asText();
                    cardContainer.add(createCard(name, "By " + artistName, imageUrl, spotifyUrl));
                }
            }
        } catch (Exception e) {
            cardContainer.add(new Span("Sorry, something went wrong."));
        }

        return cardContainer;
    }

    /**
     * Creates the "Featured Playlists" section with playlist cards.
     *
     * @return Div container with featured playlist cards
     */
    private Div createFeaturedPlaylists() {
        Div cardContainer = new Div(new Span("Featured Playlists"));
        cardContainer.addClassName("home-card-container");
        cardContainer.getStyle().set("display", "flex").set("flex-wrap", "wrap");
        JsonNode result = spotifyService.getFeaturedPlaylists();

        if (result != null && result.has("playlists")) {
            for (JsonNode playlist : result.path("playlists").path("items")) {
                String name = playlist.path("name").asText();
                String imageUrl = playlist.path("images").get(0).path("url").asText();
                String spotifyUrl = playlist.path("external_urls").path("spotify").asText();
                String description = playlist.path("description").asText();

                cardContainer.add(createCard(name, description, imageUrl, spotifyUrl));
            }
        } else {
            cardContainer.add(new Span("No playlists found."));
        }

        return cardContainer;
    }

    /**
     * Creates an individual card with image, title, subtitle, and open button.
     *
     * @param title       Card title text
     * @param subtitle    Card subtitle text
     * @param imageUrl    URL of the card image
     * @param spotifyUrl  Spotify URL to open when clicking the button
     * @return Div representing the card component
     */
    private Div createCard(String title, String subtitle, String imageUrl, String spotifyUrl) {
        Image image = new Image(imageUrl, "cover");
        image.setWidth("150px");
        image.setHeight("150px");
        image.getStyle().set("border-radius", "8px");

        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle().set("font-weight", "bold");

        Div subtitleDiv = new Div();
        subtitleDiv.setText(subtitle);
        subtitleDiv.getStyle().set("font-size", "small").set("color", "#666");

        Button openBtn = new Button("Open in Spotify", new Icon("vaadin", "eye"), e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs("window.open($0, '_blank')", spotifyUrl));
        });

        Div card = new Div(image, titleDiv, subtitleDiv, openBtn);
        card.getStyle()
                .set("padding", "10px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "12px")
                .set("width", "200px")
                .set("text-align", "center")
                .set("margin", "10px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.1)");

        return card;
    }

    /**
     * Creates the footer layout with contact info and social media icons.
     *
     * @return Div containing footer with social links
     */
    private Div createFooter() {
        Span contactText = new Span("Contact Us:");

        Anchor facebookLink = new Anchor("https://facebook.com", getSvgIcon("facebook"));
        facebookLink.setTarget("_blank");

        Anchor tiktokLink = new Anchor("https://tiktok.com", getSvgIcon("tiktok"));
        tiktokLink.setTarget("_blank");

        Anchor instagramLink = new Anchor("https://instagram.com", getSvgIcon("instagram"));
        instagramLink.setTarget("_blank");

        Anchor phoneNumberLink = new Anchor("https://facebook.com", getSvgIcon("phone-number"));
        phoneNumberLink.setTarget("_blank");

        HorizontalLayout iconsLayout = new HorizontalLayout(facebookLink, tiktokLink, instagramLink, phoneNumberLink);

        Div footerLayout = new Div(contactText, iconsLayout);
        footerLayout.addClassName("home-footer");

        return footerLayout;
    }

    /**
     * Returns a description text for the music streaming system.
     *
     * @return Description string
     */
    private String description() {
        return """
                The music streaming system is to provide users with an easy-to-use platform where they can listen to a vast array of music content.\s
                These systems aim to make the music listening experience accessible and seamless, allowing users to play songs on-demand,\s
                create personalized playlists, and discover new artists and genres.\s
                One of the defining features of music streaming services is the ability to offer users a music library that far exceeds the capacity of physical media storage.\s
                For instance, platforms like Spotify, Apple Music, and YouTube Music give users access to millions of tracks from a wide range of genres, all with just a few clicks or taps.
               \s""";
    }

    /**
     * Creates the header layout with navigation buttons and user info.
     *
     * @return HorizontalLayout containing header components
     */
    private HorizontalLayout createHeader() {
        Span headerText = new Span("Music Streaming System");
        Button libraryButton = new Button("Library", getSvgIcon("library"), e -> UI.getCurrent().navigate(MusicLibraryView.class));
        Button playlistButton = new Button("Playlist", getSvgIcon("playlist"), e -> UI.getCurrent().navigate(PlaylistView.class));

        Span userName = new Span(securityService.getAuthenticatedUser().getUsername());
        Icon userIcon = new Icon("vaadin", "user");
        Button signoutButton = new Button("Sign out", new Icon("vaadin", "sign-out"), e -> securityService.logout());

        Div userDiv = new Div(userName, userIcon, signoutButton);
        userDiv.addClassName("home-user-div");

        HorizontalLayout headerLayout = new HorizontalLayout(headerText, libraryButton, playlistButton, userDiv);
        headerLayout.addClassName("home-header-layout");

        return headerLayout;
    }

    /**
     * Loads an SVG icon by its name from resources.
     *
     * @param iconName The SVG icon file name without extension
     * @return SvgIcon loaded from resource
     */
    private SvgIcon getSvgIcon(String iconName) {
        return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + iconName + ".svg")));
    }
}