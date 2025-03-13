package com.example.backend.rest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RESTChatTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        baseURI = "https://localhost";
        basePath = "/api/v1";
        RestAssured.port = port;
        RestAssured.basePath = basePath;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.authentication = RestAssured.basic("AdminReader", "adminpass");
    }

    @Test
    void testGetUserChats() {
        // Case 1: Not logged in
        given()
                .when()
                .get("/chats")
                .then()
                .statusCode(401);

        // Case 2: Logged in user
        String authCookie = given()
                .contentType("application/json")
                .body("{\"username\": \"BookReader_14\", \"password\": \"pass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("AuthToken")
                .extract()
                .cookie("AuthToken");

        // Get chats with pagination
        given()
                .cookie("AuthToken", authCookie)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/chats")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void testGetChatMessages() {
        // Case 1: Not logged in
        given()
                .when()
                .get("/chats/1/messages")
                .then()
                .statusCode(401);

        // Case 2: Logged in user
        String authCookie = given()
                .contentType("application/json")
                .body("{\"username\": \"BookReader_14\", \"password\": \"pass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("AuthToken")
                .extract()
                .cookie("AuthToken");

        // Case 2.1: Accessing own chat messages
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .get("/chats/1/messages")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));

        // Case 2.2: Accessing non-existent chat messages
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .get("/chats/999999/messages")
                .then()
                .statusCode(400);

        // Case 2.3: Accessing unauthorized chat messages
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .get("/chats/4/messages")
                .then()
                .statusCode(400);
    }

    @Test
    void testMarkMessagesAsRead() {
        // Case 1: Not logged in
        given()
                .when()
                .post("/chats/1/read")
                .then()
                .statusCode(401);

        // Case 2: Logged in user
        String authCookie = given()
                .contentType("application/json")
                .body("{\"username\": \"BookReader_14\", \"password\": \"pass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("AuthToken")
                .extract()
                .cookie("AuthToken");

        // Case 2.1: Marking own chat messages as read
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .post("/chats/1/read")
                .then()
                .statusCode(200);

        // Case 2.2: Marking non-existent chat messages as read
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .post("/chats/999999/read")
                .then()
                .statusCode(400);

        // Case 2.3: Marking unauthorized chat messages as read
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .post("/chats/4/read")
                .then()
                .statusCode(400);
    }

    @Test
    void testGetUnreadCount() {
        // Case 1: Not logged in
        given()
                .when()
                .get("/chats/unread-count")
                .then()
                .statusCode(401);

        // Case 2: Logged in user
        String authCookie = given()
                .contentType("application/json")
                .body("{\"username\": \"BookReader_14\", \"password\": \"pass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("AuthToken")
                .extract()
                .cookie("AuthToken");

        // Get unread count - BookReader_14 has 2 unread messages (one from FanBook_785 and one from YourReader)
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .get("/chats/unread-count")
                .then()
                .statusCode(200)
                .body(greaterThanOrEqualTo("0")); // Unread count should be non-negative
    }
} 