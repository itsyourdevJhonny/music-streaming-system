package com.musicstreaming.application.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * View class for password recovery page.
 * Allows users to enter their email address to recover their password.
 */
@Route("forgot-password")
@PageTitle("ForgotPassword | MSS")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    /**
     * Constructor that builds the Forgot Password UI.
     * Sets up the page title and content layout.
     */
    public ForgotPasswordView() {
        add(createTitle(), createContent());
        addClassName("forgot-password-main-layout");
    }

    /**
     * Creates the main content layout of the forgot password page.
     * Includes a description, email input field, and action buttons.
     *
     * @return VerticalLayout containing the form components.
     */
    private VerticalLayout createContent() {
        // Description text explaining the purpose of the page
        H2 description = new H2("Recover your account through email address.");
        description.addClassName("forgot-password-description");

        // Email input field for user to enter their registered email
        EmailField emailField = new EmailField("Email");
        emailField.setPlaceholder("Enter your email");
        emailField.setRequired(true);
        emailField.addClassName("forgot-password-email");

        // Button to trigger password recovery process
        Button sendButton = new Button("Recover", e -> {
            if (emailField.isEmpty()) {
                // Show error if email is empty
                Notification.show("Please enter your email address.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else if (emailField.isInvalid()) {
                // Show error if email format is invalid
                Notification.show("Please enter a valid email address.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                // Here you would add backend call to send recovery email
                Notification.show("If this email is registered, recovery instructions will be sent.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Navigate back to login page after showing message
                UI.getCurrent().navigate(LoginView.class);
            }
        });
        sendButton.addClassName("forgot-password-button");

        // Button to navigate back to the login page without recovery
        Button backButton = new Button("Back", e -> UI.getCurrent().navigate(LoginView.class));
        backButton.addClassName("forgot-password-back");

        // Layout container for all form elements
        VerticalLayout contentLayout = new VerticalLayout(description, emailField, sendButton, backButton);
        contentLayout.addClassName("forgot-password-content-layout");

        return contentLayout;
    }

    /**
     * Creates the page title header.
     *
     * @return H1 component representing the page title.
     */
    private H1 createTitle() {
        H1 title = new H1("Recover your password");
        title.addClassName("forgot-password-title");
        return title;
    }
}
