package com.musicstreaming.application.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;


@Route("forgot-password")
@PageTitle("ForgotPassword | MSS")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    public ForgotPasswordView() {
        add(createTitle(),  createContent());
        addClassName("forgot-password-main-layout");
    }

    private VerticalLayout createContent() {
        H2 description = new H2("Recover your account through email address.");
        description.addClassName("forgot-password-description");

        EmailField emailField = new EmailField("Email");
        emailField.setPlaceholder("Enter your email");
        emailField.setRequired(true);
        emailField.addClassName("forgot-password-email");

        Button sendButton = new Button("Recover", e -> UI.getCurrent().navigate(LoginView.class));
        sendButton.addClassName("forgot-password-button");

        Button backButton = new Button("Back", e -> UI.getCurrent().navigate(LoginView.class));
        backButton.addClassName("forgot-password-back");

        VerticalLayout contentLayout = new VerticalLayout(description, emailField, sendButton, backButton);
        contentLayout.addClassName("forgot-password-content-layout");

        return contentLayout;
    }

    private H1 createTitle() {
        H1 title = new H1("Recover your password");
        title.addClassName("forgot-password-title");
        return title;
    }
}
