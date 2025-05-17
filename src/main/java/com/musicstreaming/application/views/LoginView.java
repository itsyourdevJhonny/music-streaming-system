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

@Route("login")
@PageTitle("Login | MSS")
@AnonymousAllowed
public class LoginView extends AppLayout implements BeforeEnterObserver {
    private final LoginForm loginForm = new LoginForm();
    private final String OAUTH_URL = "/oauth2/authorization/google";

    public LoginView(@Autowired Environment env) {
        addClassName("form");
        createLoginLayout(env);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }

    private void createLoginLayout(@Autowired Environment env) {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setUsername("Username");
        i18nForm.setForgotPassword("Forgot password?");
        i18nForm.setTitle("Music Streaming System");
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Incorrect email or password");
        i18nErrorMessage.setMessage("Check that you have entered the correct username and password and try again.");
        i18n.setErrorMessage(i18nErrorMessage);

        loginForm.getElement().setAttribute("no-autofocus", "");
        loginForm.addClassName("login-form");
        loginForm.setI18n(i18n);
        loginForm.setAction("login");
        loginForm.addForgotPasswordListener(event -> UI.getCurrent().navigate(ForgotPasswordView.class));
        loginForm.addLoginListener(e -> VaadinSession.getCurrent().setAttribute("user", e.getUsername()));

        Button registerButton = new Button("No account yet? Register here.");
        registerButton.addClassName("login-register-button");
        registerButton.addClickListener(event -> UI.getCurrent().navigate(RegisterView.class));

        setContent(getVerticalLayout(registerButton));
    }

    private VerticalLayout getVerticalLayout(Button registerButton) {
        StreamResource resource = new StreamResource("google.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/google.svg"));

        Div firstLine = new Div();
        firstLine.addClassName("first-line");

        Span orText = new Span("Or");

        Div lastLine = new Div();
        lastLine.addClassName("last-line");

        HorizontalLayout orLayout = new HorizontalLayout(firstLine, orText, lastLine);
        orLayout.addClassName("login-or-layout");

        SvgIcon googleIcon = new SvgIcon(resource);

        /*Button googleButton = new Button("LOGIN WITH GOOGLE", googleIcon);
        googleButton.setEnabled(false);
        googleButton.addClassName("google-button");
        googleButton.addClickListener(event -> UI.getCurrent().getPage().setLocation(OAUTH_URL));*/

        VerticalLayout formLayout = new VerticalLayout(loginForm, orLayout, /*googleButton*/ createLoginButtons(), registerButton);
        formLayout.addClassName("login-form-layout");
        return formLayout;
    }

    private VerticalLayout createLoginButtons() {
        Button googleButton = new Button("Google", getSvgIcon("google"));
        Button facebookButton = new Button("Facebook", getSvgIcon("facebook"));
        Button phoneNumberButton = new Button("Phone Number", getSvgIcon("phone-number"));

        VerticalLayout loginButtonsLayout = new VerticalLayout(new Span("LOGIN WITH"), googleButton, facebookButton, phoneNumberButton);
        loginButtonsLayout.addClassName("login-buttons-layout");

        return loginButtonsLayout;
    }

    private SvgIcon getSvgIcon(String iconName) {
        return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + iconName + ".svg")));
    }
}