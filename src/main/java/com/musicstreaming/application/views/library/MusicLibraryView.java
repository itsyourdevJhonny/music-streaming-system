package com.musicstreaming.application.views.library;

import com.fasterxml.jackson.databind.JsonNode;
import com.musicstreaming.application.model.Playlist;
import com.musicstreaming.application.model.PlaylistSong;
import com.musicstreaming.application.security.SecurityService;
import com.musicstreaming.application.service.PlaylistService;
import com.musicstreaming.application.service.RecommendationService;
import com.musicstreaming.application.service.SpotifyService;
import com.musicstreaming.application.service.UserService;
import com.musicstreaming.application.views.HomeView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.*;

/**
 * View class for the music library page.
 * Allows users to browse and filter music content such as tracks, albums, and artists.
 * Supports search, genre filtering, popularity thresholds, and release date ranges.
 */
@Route("library")
@PageTitle("Library | MSS")
@PermitAll
public class MusicLibraryView extends AppLayout {

    private final RecommendationService recommendationService;
    private final SecurityService securityService;
    private final UserService userService;
    private final PlaylistService playlistService;
    private final SpotifyService spotifyService = new SpotifyService();

    private final ComboBox<String> genreComboBox = new ComboBox<>("Genre");
    private final ComboBox<String> typeComboBox = new ComboBox<>("Type");
    private final TextField searchField = new TextField("Search");
    private final IntegerField popularityField = new IntegerField("Minimum Popularity (0–100)");
    private final DatePicker fromDate = new DatePicker("Released After");
    private final DatePicker toDate = new DatePicker("Released Before");
    private final Grid<String> resultGrid = new Grid<>();
    private final Map<String, String> genreMap = new HashMap<>();
    private final Div cardContainer = new Div();
    private int offset = 0;

    /**
     * Constructor that initializes the Music Library UI.
     * Sets up filters, event listeners, layout, and loads initial content.
     *
     * @param recommendationService service for music recommendations
     * @param securityService       service to handle security and authentication
     * @param userService           service for user data operations
     * @param playlistService       service for playlist management
     */
    public MusicLibraryView(RecommendationService recommendationService,
                            SecurityService securityService,
                            UserService userService,
                            PlaylistService playlistService) {
        this.recommendationService = recommendationService;
        this.securityService = securityService;
        this.userService = userService;
        this.playlistService = playlistService;

        addClassName("library-app-layout");
        cardContainer.addClassName("library-card-container");

        // Set up filter controls
        typeComboBox.setItems("track", "album", "artist");
        typeComboBox.setValue("album");

        popularityField.setMin(0);
        popularityField.setMax(100);

        genreComboBox.setPlaceholder("Select Genre");

        resultGrid.addColumn(item -> item).setHeader("Results");
        cardContainer.getStyle().set("display", "flex").set("flex-wrap", "wrap");

        // Load available genres from Spotify
        loadGenres();

        // Add filter event
        genreComboBox.addValueChangeListener(e -> filterByGenre());

        // Load initial music content
        getNewReleases();

        // Header setup
        Span headerTitle = new Span("Music Library");
        Button backButton = new Button("Back", new Icon("vaadin", "arrow-backward"),
                e -> UI.getCurrent().navigate(HomeView.class));
        HorizontalLayout headerTitleLayout = new HorizontalLayout(backButton, headerTitle, getSvgIcon("logo"));
        headerTitleLayout.addClassName("library-header-title");

        // Filter/search layout
        HorizontalLayout headerLayout = new HorizontalLayout(
                searchField, typeComboBox, popularityField, fromDate, toDate, genreComboBox);
        headerLayout.addClassName("library-header-layout");

        // Add event handlers
        setEventHandlers();

        // Add header and main content to view
        addToNavbar(headerTitleLayout, headerLayout);
        setContent(createContent());
    }

    /**
     * Sets event handlers for the UI components related to filtering and searching.
     * Adds listeners to update the content dynamically as the user changes filter values.
     */
    private void setEventHandlers() {
        searchField.addValueChangeListener(e -> filterBySearch());
        searchField.setSuffixComponent(new Icon("vaadin", "search"));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        popularityField.addValueChangeListener(e -> filterByPopularity());
        popularityField.setValueChangeMode(ValueChangeMode.EAGER);

        typeComboBox.addValueChangeListener(e -> filterBySearch());
        fromDate.addValueChangeListener(e -> filterByDate());
        toDate.addValueChangeListener(e -> filterByDate());
        genreComboBox.addValueChangeListener(e -> filterByGenre());
    }

    /**
     * Creates the main content layout of the music library view.
     * Includes sections for new releases, popular artists, and a "More" button.
     *
     * @return Div container holding the main content components.
     */
    private Div createContent() {
        Span newReleaseText = new Span("New Releases");
        newReleaseText.addClassName("library-new-release-text");

        Span popularArtistText = new Span("Popular Artists");
        popularArtistText.addClassName("library-new-release-text");

        Button moreButton = new Button("More", new Icon("vaadin", "forward"), e -> getNewReleases());

        Div contentLayout = new Div();
        contentLayout.addClassName("library-content-layout");
        contentLayout.add(newReleaseText, cardContainer, popularArtistText, loadRandomArtists(), moreButton);

        return contentLayout;
    }

    /**
     * Fetches new album releases from the Spotify API and populates the card container with album cards.
     * Handles exceptions by showing an error message in the UI.
     */
    private void getNewReleases() {
        cardContainer.removeAll();

        offset++;

        try {
            JsonNode albums = spotifyService.getNewReleases(offset);
            if (albums != null) {
                for (JsonNode item : albums.path("albums").path("items")) {
                    String name = item.path("name").asText();
                    String artist = item.path("artists").get(0).path("name").asText();
                    String imageUrl = item.path("images").get(0).path("url").asText();
                    String albumUrl = item.path("external_urls").path("spotify").asText();
                    String albumId = extractSpotifyId(albumUrl); // extract ID from URL
                    cardContainer.add(createCard(name, "by " + artist, imageUrl, albumId, "album"));
                }
            }
        } catch (Exception e) {
            cardContainer.add(new Span("Sorry, something went wrong."));
        }
    }

    /**
     * Loads a random selection of popular artists and returns a container with their artist cards.
     * Artist data is retrieved from the Spotify API.
     *
     * @return Div container with cards for random popular artists.
     */
    private Div loadRandomArtists() {
        Div container = new Div();
        container.addClassName("library-popular-container");

        List<String> artistIds = new ArrayList<>(List.of(
                "06HL4z0CvFAxyc27GXpf02", // Taylor Swift
                "1Xyo4u8uXC1ZmMpatF05PJ", // The Weekend
                "3Nrfpe0tUJi4K4DXYWgMUX", // BTS
                "66CXWjxzNUsdJxJ2JdwvnR", // Ariana Grande
                "1uNFoZAHBGtllmzznpCI3s", // Justin Bieber
                "3TVXtAsR1Inumwj472S9r4", // Drake
                "7dGJo4pcD2V6oG8kP0tJRR", // Eminem
                "246dkjvS1zLTtiykXe5h60"  // Post Malone
        ));

        Collections.shuffle(artistIds); // Randomize the list
        List<String> random5 = artistIds.subList(0, 5); // Pick 5

        SpotifyService spotifyService = new SpotifyService();

        for (String artistId : random5) {
            try {
                JsonNode artist = spotifyService.getArtist(artistId);
                if (artist != null) {
                    String name = artist.path("name").asText();
                    String imageUrl = artist.path("images").get(0).path("url").asText();
                    String spotifyUrl = artist.path("external_urls").path("spotify").asText();
                    container.add(createArtistCard(name, "Popular Artist", imageUrl, spotifyUrl));
                }
            } catch (Exception e) {
                cardContainer.add(new Span("Sorry, something went wrong."));
            }
        }

        return container;
    }

    /**
     * Loads music genres from Spotify categories API and populates the genreComboBox.
     * Fills the genreMap with genre names and their corresponding IDs.
     * Shows an error message in the UI if the API call fails.
     */
    private void loadGenres() {
        try {
            JsonNode categories = spotifyService.getCategories();
            if (categories != null) {
                for (JsonNode item : categories.path("categories").path("items")) {
                    genreMap.put(item.path("name").asText(), item.path("id").asText());
                }
                genreComboBox.setItems(genreMap.keySet());
            }
        } catch (Exception e) {
            cardContainer.add(new Span("Sorry, something went wrong."));
        }
    }

    /**
     * Filters music library results based on the search query and selected type (track, album, artist).
     * Clears previous results and updates the UI with matching items from Spotify search API.
     */
    private void filterBySearch() {
        cardContainer.removeAll();

        String query = searchField.getValue();
        String type = typeComboBox.getValue();

        if (query == null || query.isEmpty()) {
            resultGrid.setItems(Collections.emptyList());
            return;
        }

        JsonNode searchResults = spotifyService.search(query, type);

        if (searchResults != null) {
            JsonNode items = searchResults.path(type + "s").path("items");
            for (JsonNode item : items) {
                String name = item.path("name").asText();
                String artist = item.path("artists").get(0).path("name").asText();
                String imageUrl = item.path("images").get(0).path("url").asText();
                String albumUrl = item.path("external_urls").path("spotify").asText();
                String albumId = extractSpotifyId(albumUrl); // extract ID from URL
                cardContainer.add(createCard(name, "by " + artist, imageUrl, albumId, "album"));
            }
        }
    }

    /**
     * Filters music library results based on the minimum popularity threshold.
     * Retrieves search results from Spotify and only displays items with popularity
     * greater than or equal to the specified minimum.
     */
    private void filterByPopularity() {
        String query = searchField.getValue();
        String type = typeComboBox.getValue();
        int minPopularity = popularityField.getValue();

        if (query == null || query.isEmpty()) return;

        JsonNode searchResults = spotifyService.search(query, type);

        if (searchResults != null) {
            JsonNode items = searchResults.path(type + "s").path("items");
            for (JsonNode item : items) {
                int popularity = item.has("popularity") ? item.get("popularity").asInt() : 0;
                if (popularity >= minPopularity) {
                    String name = item.path("name").asText();
                    String imageUrl = item.path("images").get(0).path("url").asText();
                    String albumUrl = item.path("external_urls").path("spotify").asText();
                    String albumId = extractSpotifyId(albumUrl); // extract ID from URL
                    cardContainer.add(createCard(name, "[Popularity: " + popularity, imageUrl, albumId, "album"));
                }
            }
        }
    }

    /**
     * Filters search results by release date range.
     * Shows only items released on or after 'fromDate' and on or before 'toDate'.
     * Supports filtering tracks by their album release date.
     */
    private void filterByDate() {
        String query = searchField.getValue();
        String type = typeComboBox.getValue();
        LocalDate after = fromDate.getValue();
        LocalDate before = toDate.getValue();

        if (query == null || query.isEmpty()) return;

        JsonNode searchResults = spotifyService.search(query, type);

        if (searchResults != null) {
            JsonNode items = searchResults.path(type + "s").path("items");
            for (JsonNode item : items) {
                String name = item.path("name").asText();
                String releaseRaw = type.equals("track")
                        ? item.path("album").path("release_date").asText("")
                        : item.path("release_date").asText("");

                LocalDate releaseDate = getLocalDate(releaseRaw);

                if (releaseDate != null) {
                    if ((after == null || !releaseDate.isBefore(after)) && (before == null || !releaseDate.isAfter(before))) {
                        String imageUrl = item.path("images").get(0).path("url").asText();
                        String albumUrl = item.path("external_urls").path("spotify").asText();
                        String albumId = extractSpotifyId(albumUrl); // extract ID from URL
                        cardContainer.add(createCard(name, "(Released: " + releaseDate, imageUrl, albumId, "album"));
                    }
                }
            }
        }
    }

    /**
     * Filters music library content by the selected genre.
     * Clears all other filters, fetches playlists for the genre category,
     * and updates the card container with genre-specific playlists.
     */
    private void filterByGenre() {
        searchField.clear();
        typeComboBox.clear();
        popularityField.clear();
        fromDate.clear();
        toDate.clear();

        String genreName = genreComboBox.getValue();
        if (genreName == null) return;

        String categoryId = genreMap.get(genreName);
        JsonNode playlists = spotifyService.getPlaylistsByCategory(categoryId);

        if (playlists != null) {
            for (JsonNode item : playlists.path("playlists").path("items")) {
                String name = item.path("name").asText();
                String artist = item.path("artists").get(0).path("name").asText();
                String imageUrl = item.path("images").get(0).path("url").asText();
                String albumUrl = item.path("external_urls").path("spotify").asText();
                String albumId = extractSpotifyId(albumUrl); // extract ID from URL
                cardContainer.add(createCard(name, "by " + artist, imageUrl, albumId, "album"));
            }
        }
    }

    /**
     * Parses Spotify release date strings into LocalDate objects.
     * Handles full date, year-month, and year-only formats by defaulting missing values to the first day/month.
     *
     * @param releaseRaw raw release date string from Spotify API
     * @return parsed LocalDate or null if parsing fails
     */
    @Nullable
    private static LocalDate getLocalDate(String releaseRaw) {
        if (releaseRaw == null || releaseRaw.isEmpty()) return null;
        try {
            return LocalDate.parse(releaseRaw);
        } catch (Exception ex) {
            if (releaseRaw.matches("\\d{4}-\\d{2}")) {
                return LocalDate.parse(releaseRaw + "-01");
            } else if (releaseRaw.matches("\\d{4}")) {
                return LocalDate.parse(releaseRaw + "-01-01");
            }
        }
        return null;
    }

    /**
     * Creates a music card component displaying cover image, title, subtitle,
     * embedded Spotify player iframe, and an add-to-playlist button.
     * Also adds a click listener to track user recommendations.
     *
     * @param title     the title of the music item (track, album, artist)
     * @param subtitle  additional info such as artist name
     * @param imageUrl  cover image URL
     * @param spotifyId Spotify ID for embedding the player
     * @param type      item type (e.g. "track", "album", "artist")
     * @return a Div component representing the music card
     */
    private Div createCard(String title, String subtitle, String imageUrl, String spotifyId, String type) {
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

        Div card = new Div(detailsDiv, new Html(iframeHtml), createAddToPlaylist(title, subtitle, imageUrl, spotifyId));
        card.addClassName("library-card");
        card.getStyle()
                .set("padding", "10px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("width", "300px")
                .set("margin", "10px");
        card.addClickListener(e -> recommendationService.add(subtitle, securityService.getAuthenticatedUser().getUsername(), title));

        return card;
    }

    /**
     * Creates an "Add To Playlist" button.
     * Clicking it opens a dialog allowing users to add the current music item to their playlists.
     *
     * @param title      song/album/artist title
     * @param artist     artist name or subtitle
     * @param imageUrl   cover image URL
     * @param spotifyUrl Spotify URL of the item
     * @return Button component configured with click listener for playlist addition
     */
    private Button createAddToPlaylist(String title, String artist, String imageUrl, String spotifyUrl) {
        Button addButton = new Button("Add To Playlist", new Icon("vaadin", "plus"));
        addButton.addClickListener(e -> {
            VerticalLayout playlistLayout = new VerticalLayout();

            Dialog playlistDialog = new Dialog(playlistLayout);
            playlistDialog.addClassName("library-dialog");
            playlistDialog.getHeader().add(new Span("Choose Playlist"));

            String username = securityService.getAuthenticatedUser().getUsername();

            List<Playlist> playlists = playlistService.getAllPlaylistsByUser(userService.getUserByUsername(username).getId());

            for (Playlist playlist : playlists) {
                HorizontalLayout singleLayout = new HorizontalLayout(
                        new Span(playlist.getName()),
                        new Button(new Icon("vaadin", "plus"), event -> {
                            PlaylistSong song = new PlaylistSong();
                            song.setTitle(title);
                            song.setArtist(artist);
                            song.setCoverImage(imageUrl);
                            song.setSpotifyUrl(spotifyUrl);

                            playlistService.addSongToPlaylist(playlist.getId(), song);

                            System.out.println("ADDED: " + song.getArtist());

                            playlistDialog.close();
                        })
                );
                singleLayout.addClassName("library-dialog-single-layout");

                playlistLayout.add(singleLayout);
            }

            playlistDialog.open();
        });

        return addButton;
    }

    /**
     * Creates a card UI component for displaying an artist.
     * Includes image, title, subtitle, and a button to open the artist's Spotify page in a new browser tab.
     *
     * @param title      artist name
     * @param subtitle   artist description or label
     * @param imageUrl   artist image URL
     * @param spotifyUrl URL to the artist's Spotify page
     * @return Div component representing the artist card
     */
    private Div createArtistCard(String title, String subtitle, String imageUrl, String spotifyUrl) {
        Image image = new Image(imageUrl, "cover");
        image.setWidth("150px");
        image.setHeight("150px");

        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle().set("font-weight", "bold");

        Div subtitleDiv = new Div();
        subtitleDiv.setText(subtitle);
        subtitleDiv.getStyle().set("font-size", "small").set("color", "#555");

        Button openButton = new Button("View Artist", e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs("window.open($0, '_blank')", spotifyUrl));
        });

        Div card = new Div(image, titleDiv, subtitleDiv, openButton);
        card.getStyle()
                .set("padding", "10px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("width", "200px")
                .set("margin", "10px");

        return card;
    }

    /**
     * Extracts the Spotify ID from a full Spotify URL.
     * Splits the URL by '/' and returns the last segment.
     *
     * @param url full Spotify URL
     * @return Spotify ID string or empty if input is null/empty
     */
    private String extractSpotifyId(String url) {
        if (url == null || url.isEmpty()) return "";
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Loads an SVG icon by its name from the application's resources.
     *
     * @param iconName the name of the SVG icon file (without extension)
     * @return SvgIcon loaded from resource stream
     */
    private SvgIcon getSvgIcon(String iconName) {
        return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + iconName + ".svg")));
    }
}
