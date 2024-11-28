package com.example.backend.rest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

class RESTUserTest {

    @LocalServerPort
    private int port = 8443;

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
    void testGetCurrentUser() {

        // Without logging in
        given().when().get("/users/me").then().statusCode(401);

        // Logging in
        String authCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"BookReader_14\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // After logging in, verifying access
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("username", equalTo("BookReader_14"));
    }

}
