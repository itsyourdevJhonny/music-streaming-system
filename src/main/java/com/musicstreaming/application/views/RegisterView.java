package com.musicstreaming.application.views;

import com.musicstreaming.application.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * RegisterView class provides the UI for user registration.
 * Allows users to enter a username and password to create a new account.
 * Displays notifications on input validation and navigates users to login page after registration.
 */
@AnonymousAllowed
@Route("register")
public class RegisterView extends VerticalLayout {

    // Service responsible for user-related operations such as registration
    private final UserService userService;

    // Text field for entering username with an icon suffix
    private final TextField usernameField = new TextField("Enter username");

    // Text field for entering password with an icon suffix
    private final TextField passwordField = new TextField("Enter password");

    /**
     * Constructor initializes the registration form UI components and event handlers.
     *
     * @param userService Service to handle user registration logic (injected).
     */
    public RegisterView(UserService userService) {
        this.userService = userService;
        addClassName("register-layout");

        // Add icons to the end of text fields for visual cue
        usernameField.setSuffixComponent(new Icon("vaadin", "user"));
        passwordField.setSuffixComponent(new Icon("vaadin", "key"));

        // Title of the registration form
        Span title = new Span("Register");

        // Button that triggers the registration process when clicked
        Button registerButton = new Button("Register", new Icon("vaadin", "enter"), e -> {
            String usernameValue = usernameField.getValue();
            String passwordValue = passwordField.getValue();

            // Check if either username or password fields are empty or whitespace only
            if ((usernameValue.isEmpty() || usernameValue.matches("\\s*")) &&
                    (passwordValue.isEmpty() || passwordValue.matches("\\s*"))) {
                // Show error notification prompting the user to fill the fields
                Notification.show("Please fill the fields.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                // Register the user through UserService
                userService.registerUser(usernameValue, passwordValue);

                // Navigate to the login view after successful registration
                UI.getCurrent().navigate(LoginView.class);
            }
        });

        // Container div holding the input fields and the register button
        Div fieldDiv = new Div(usernameField, passwordField, registerButton);

        // Button to navigate users who already have an account back to login view
        Button loginButton = new Button("Already have an account? Login here.",
                e -> UI.getCurrent().navigate(LoginView.class));

        // Parent div wrapping fields and navigation button, styled for layout
        Div parentDiv = new Div(fieldDiv, loginButton);
        parentDiv.addClassName("register-parent-div");

        // Add the logo icon, title, and the parent container div to the main layout
        add(getSvgIcon(), title, parentDiv);
    }

    /**
     * Loads the application logo as an SVG icon from the resources folder.
     *
     * @return SvgIcon component representing the application logo.
     */
    private SvgIcon getSvgIcon() {
        return new SvgIcon(new StreamResource("logo.svg",
                () -> getClass().getResourceAsStream("/META-INF/resources/icons/logo.svg")));
    }
}
