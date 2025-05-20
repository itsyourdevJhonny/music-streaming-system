package com.musicstreaming.application.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * View class representing the Login page of the Music Streaming System.
 * Provides form for user login, password recovery navigation,
 * and options for social media login buttons (Google, Facebook, Phone).
 */
@Route("login")
@PageTitle("Login | MSS")
@AnonymousAllowed
public class LoginView extends AppLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    // OAuth URL endpoint for Google login
    private final String OAUTH_URL = "/oauth2/authorization/google";

    /**
     * Constructor that initializes the login layout and UI components.
     *
     * @param env Spring Environment instance (injected automatically).
     */
    public LoginView(@Autowired Environment env) {
        addClassName("form");
        createLoginLayout(env);
    }

    /**
     * BeforeEnterObserver method.
     * Checks for "error" query parameter to display login error message.
     *
     * @param beforeEnterEvent Navigation event details.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Check if URL contains error parameter (login failure)
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);  // Display error in login form
        }
    }

    /**
     * Configures the login form UI, including localization and event listeners.
     *
     * @param env Spring Environment instance (unused here but can be for configs).
     */
    private void createLoginLayout(@Autowired Environment env) {
        // Create default i18n (internationalization) settings for the login form
        LoginI18n i18n = LoginI18n.createDefault();

        // Customize form field labels and texts
        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setUsername("Username");
        i18nForm.setForgotPassword("Forgot password?");
        i18nForm.setTitle("Music Streaming System");
        i18n.setForm(i18nForm);

        // Customize error message shown on login failure
        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Incorrect email or password");
        i18nErrorMessage.setMessage("Check that you have entered the correct username and password and try again.");
        i18n.setErrorMessage(i18nErrorMessage);

        // Disable autofocus on first input element
        loginForm.getElement().setAttribute("no-autofocus", "");
        loginForm.addClassName("login-form");

        // Apply the i18n localization settings to the form
        loginForm.setI18n(i18n);

        // Set login action URL (endpoint handled by Spring Security)
        loginForm.setAction("login");

        // Add listener to navigate to forgot password page
        loginForm.addForgotPasswordListener(event -> UI.getCurrent().navigate(ForgotPasswordView.class));

        // Add listener to store username in Vaadin session after successful login attempt
        loginForm.addLoginListener(e -> VaadinSession.getCurrent().setAttribute("user", e.getUsername()));

        // Button to navigate to registration page for users without account
        Button registerButton = new Button("No account yet? Register here.");
        registerButton.addClassName("login-register-button");
        registerButton.addClickListener(event -> UI.getCurrent().navigate(RegisterView.class));

        // Set the main content area to the assembled login form and buttons layout
        setContent(getVerticalLayout(registerButton));
    }

    /**
     * Builds the vertical layout containing the login form, social login options,
     * and registration button.
     *
     * @param registerButton Button that navigates to the registration page.
     * @return VerticalLayout containing the entire login page content.
     */
    private VerticalLayout getVerticalLayout(Button registerButton) {
        // Load Google icon SVG resource for buttons
        StreamResource resource = new StreamResource("google.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/google.svg"));

        // Horizontal line divs for UI decoration around the "Or" text
        Div firstLine = new Div();
        firstLine.addClassName("first-line");

        Span orText = new Span("Or");

        Div lastLine = new Div();
        lastLine.addClassName("last-line");

        // Layout containing the two horizontal lines and the "Or" label between them
        HorizontalLayout orLayout = new HorizontalLayout(firstLine, orText, lastLine);
        orLayout.addClassName("login-or-layout");

        // SVG icon loaded for Google button (commented out below)
        SvgIcon googleIcon = new SvgIcon(resource);

        /*
         * The Google OAuth button code is commented out.
         * Uncomment and enable if you want to activate Google OAuth login.
         *
         * Button googleButton = new Button("LOGIN WITH GOOGLE", googleIcon);
         * googleButton.setEnabled(false);
         * googleButton.addClassName("google-button");
         * googleButton.addClickListener(event -> UI.getCurrent().getPage().setLocation(OAUTH_URL));
         */

        // Compose the main vertical layout containing login form, "Or" layout,
        // social login buttons, and registration button.
        VerticalLayout formLayout = new VerticalLayout(loginForm, orLayout, /*googleButton*/ createLoginButtons(), registerButton);
        formLayout.addClassName("login-form-layout");
        return formLayout;
    }

    /**
     * Creates a vertical layout containing social login buttons.
     * Currently includes buttons for Google, Facebook, and Phone Number login.
     *
     * @return VerticalLayout with social login buttons.
     */
    private VerticalLayout createLoginButtons() {
        // Social login buttons with SVG icons
        Button googleButton = new Button("Google", getSvgIcon("google"));
        Button facebookButton = new Button("Facebook", getSvgIcon("facebook"));
        Button phoneNumberButton = new Button("Phone Number", getSvgIcon("phone-number"));

        // Layout containing the label and social login buttons
        VerticalLayout loginButtonsLayout = new VerticalLayout(new Span("LOGIN WITH"), googleButton, facebookButton, phoneNumberButton);
        loginButtonsLayout.addClassName("login-buttons-layout");

        return loginButtonsLayout;
    }

    /**
     * Helper method to load an SVG icon from the resources folder.
     *
     * @param iconName Name of the icon file (without extension).
     * @return SvgIcon component loaded from resource.
     */
    private SvgIcon getSvgIcon(String iconName) {
        return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + iconName + ".svg")));
    }
}
