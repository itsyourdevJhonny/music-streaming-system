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

@AnonymousAllowed
@Route("register")
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final TextField usernameField = new TextField("Enter username");
    private final TextField passwordField = new TextField("Enter password");

    public RegisterView(UserService userService) {
        this.userService = userService;
        addClassName("register-layout");

        usernameField.setSuffixComponent(new Icon("vaadin", "user"));
        passwordField.setSuffixComponent(new Icon("vaadin", "key"));

        Span title = new Span("Register");

        Button registerbutton = new Button("Register", new Icon("vaadin", "enter"), e -> {
            String usernameValue = usernameField.getValue();
            String passwordValue = passwordField.getValue();

            if ((usernameValue.isEmpty() || usernameValue.matches("\\s*")) && (passwordValue.isEmpty() || passwordValue.matches("\\s*"))) {
                Notification.show("Please fill the fields.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                userService.registerUser(usernameValue, passwordValue);
                UI.getCurrent().navigate(LoginView.class);
            }
        });

        Div fieldDiv = new Div(usernameField, passwordField, registerbutton);

        Button loginButton = new Button("Already have an account? Login here.", e -> UI.getCurrent().navigate(LoginView.class));

        Div parentDiv = new Div(fieldDiv, loginButton);
        parentDiv.addClassName("register-parent-div");
        add(getSvgIcon(), title, parentDiv);
    }

    private SvgIcon getSvgIcon() {
        return new SvgIcon(new StreamResource("logo" + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" + "logo" + ".svg")));
    }
}