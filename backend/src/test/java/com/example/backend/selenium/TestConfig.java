package com.example.backend.selenium;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestConfig { // Singleton pattern for testing config params.

    /*
    Configuration class:
    This class provides the port and URL for testing on the local machine side. All tests defined in this package will
    use the parameters and values defined here
     */

    private static TestConfig instance;
    private final String localhost;
    private int port; // Make port non-final to allow setting it dynamically
    private final int waitTime;

    private final String username;
    private final String password;

    // Private constructor to prevent instantiation
    private TestConfig() {
        this.localhost = "http://localhost";
        this.port = 4200; // Default port
        this.waitTime = 20;

        this.username = "BookReader_14";
        this.password = "pass";
    }

    // Synchronized method to control concurrent access
    public static synchronized TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }

    // Method to set the port dynamically
    public void setPort(int port) {
        this.port = port;
    }
}