package com.example.backend;

import lombok.Getter;

@Getter
public class TestConfig { // Singleton pattern for testing config params.
    private static TestConfig instance;
    private final String localhost;
    private final int port;

    // Private constructor to prevent instantiation
    private TestConfig() {
        this.localhost = "http://localhost";
        this.port = 4200;
    }

    // Synchronized method to control concurrent access
    public static synchronized TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }

}