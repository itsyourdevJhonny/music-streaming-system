package com.musicstreaming.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Main entry point for the Music Streaming System application.
 *
 * This class bootstraps the Spring Boot application and configures Vaadin UI settings.
 *
 * Annotations used:
 * - @SpringBootApplication: Marks this as a Spring Boot application and enables component scanning.
 *   The SecurityAutoConfiguration.class is excluded here to disable Spring Security auto-configuration.
 * - @Theme: Applies the "managementflow" theme with Lumo Dark variant to the Vaadin UI.
 * - @PWA: Configures Progressive Web Application settings such as name, offline page, and resources.
 * - Implements AppShellConfigurator: Allows configuring the app shell (HTML page shell).
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@Theme(value = "managementflow", variant = Lumo.DARK)
@PWA(
        name = "Music Streaming System",
        shortName = "MSS",
        offlinePath = "offline.html",
        offlineResources = { "images/offline.png" }
)
public class Application implements AppShellConfigurator {

    /**
     * Main method - starts the Spring Boot application.
     *
     * @param args application arguments (unused).
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}