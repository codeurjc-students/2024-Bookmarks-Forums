package com.example.backend.rest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RESTCommunityTest {

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
    @Order(1)
    void testGetCommunityById() {
        given()
                .pathParam("id", 1)
                .when()
                .get("/communities/{id}")
                .then()
                .statusCode(200)
                .body("identifier", equalTo(1));
    }

    @Test
    @Order(2)
    void testGetCommunitiesByQuery() {
        // queryless
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "alphabetical")
                .queryParam("by", "general")
                .when()
                .get("/communities")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));

        // by name
        given()
                .queryParam("query", "Bookmarks")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "alphabetical")
                .queryParam("by", "name") // search by name, description, admin username or name+description
                .when()
                .get("/communities")
                .then()
                .statusCode(200)
                .body("name", hasItem(containsString("Bookmarks")));

        // by description
        given()
                .queryParam("query", "readers")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "alphabetical")
                .queryParam("by", "description")
                .when()
                .get("/communities")
                .then()
                .statusCode(200)
                .body("description", hasItem(containsString("readers")));

        // by admin username (AdminReader)
        given()
                .queryParam("query", "AdminReader")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "alphabetical")
                .queryParam("by", "admin")
                .when()
                .get("/communities")
                .then()
                .statusCode(200)
                .body("admin.username", hasItem(equalTo("AdminReader")));

        // No results (204)
        given()
                .queryParam("query", "NonExistentCommunity")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "alphabetical")
                .queryParam("by", "name")
                .when()
                .get("/communities")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(3)
    void testGetMembers() {
        given()
                .pathParam("id", 1)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/{id}/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        given()
                .pathParam("id", 9999)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/{id}/users")
                .then()
                .statusCode(404);

        // Get members count (count=true)
        given()
                .pathParam("id", 1)
                .queryParam("count", true)
                .when()
                .get("/communities/{id}/users")
                .then()
                .statusCode(200)
                .body(greaterThanOrEqualTo(Integer.toString(0)));

    }

    @Test
    @Order(4)
    void testGetModerators() {
        given()
                .pathParam("id", 1)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/{id}/moderators")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));

        given()
                .pathParam("id", 9999)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/{id}/moderators")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    void testGetAdmin() {

        given()
                .pathParam("id", 1)
                .when()
                .get("/communities/{id}/admins")
                .then()
                .statusCode(200)
                .body("username", equalTo("AdminReader")); //


        given()
                .pathParam("id", 9999)
                .when()
                .get("/communities/{id}/admins")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void testUpdateCommunity() {
        // Login as AdminReader (admin of community by id 1)
        String authCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // change multiple attributes
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 1)
                .contentType("application/json")
                .body(Map.of("name", "New Named Community", "description", "New description"))
                .when()
                .put("/communities/{id}")
                .then()
                .statusCode(200)
                .body("name", equalTo("New Named Community"))
                .body("description", equalTo("New description"));

        // Non-existent community
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 9999)
                .contentType("application/json")
                .body(Map.of("name", "NewName"))
                .when()
                .put("/communities/{id}")
                .then()
                .statusCode(404);

        // Unauthorized user
        String authCookie2 =
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

        given()
                .cookie("AuthToken", authCookie2)
                .pathParam("id", 1)
                .contentType("application/json")
                .body(Map.of("name", "NuevoNombre"))
                .when()
                .put("/communities/{id}")
                .then()
                .statusCode(401);

        // Not logged in
        given()
                .pathParam("id", 1)
                .contentType("application/json")
                .body(Map.of("name", "NuevoNombre"))
                .when()
                .put("/communities/{id}")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(7)
    void testDeleteCommunity() {
        // Login
        String authCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // Deletion: success
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 3)
                .when()
                .delete("/communities/{id}")
                .then()
                .statusCode(200)
                .body(equalTo("Community 3 deleted!"));

        // Non-existent community
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 9999)
                .when()
                .delete("/communities/{id}")
                .then()
                .statusCode(404);

        // Not logged in
        given()
                .pathParam("id", 3)
                .when()
                .delete("/communities/{id}")
                .then()
                .statusCode(401);

        // Not authorized
        String authCookie2 =
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

        given()
                .cookie("AuthToken", authCookie2)
                .pathParam("id", 1)
                .when()
                .delete("/communities/{id}")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(8)
    void testCreateCommunity() {
        String authCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // Not logged in
        given()
                .contentType("application/json")
                .body(Map.of("name", "NewCommunity", "description", "A new community"))
                .when()
                .post("/communities")
                .then()
                .statusCode(401);

        // Lack of params
        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .body(Map.of("description", "A new community"))
                .when()
                .post("/communities")
                .then()
                .statusCode(400);

        // Already existing name
        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .body(Map.of("name", "Bookmarks News", "description", "A new community"))
                .when()
                .post("/communities")
                .then()
                .statusCode(409);

        // New community
        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .body(Map.of("name", "UniqueCommunity", "description", "A unique community"))
                .when()
                .post("/communities")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(9)
    void testManageCommunityUsers() {
        String authCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"FanBook_785\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        String authCookie2 =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // Not logged in
        given()
                .pathParam("id", 1)
                .pathParam("username", "NewUser")
                .queryParam("action", "join")
                .when()
                .put("/communities/{id}/users/{username}")
                .then()
                .statusCode(401);

        // Non-existent user
        given()
                .cookie("AuthToken", authCookie2)
                .pathParam("id", 1)
                .pathParam("username", "NonExistentUser")
                .queryParam("action", "join")
                .when()
                .put("/communities/{id}/users/{username}")
                .then()
                .statusCode(422);

        // Banned user
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 1)
                .pathParam("username", "BannedUser")
                .queryParam("action", "join")
                .when()
                .put("/communities/{id}/users/{username}")
                .then()
                .statusCode(401);

        // Success join
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 1)
                .pathParam("username", "FanBook_785")
                .queryParam("action", "join")
                .when()
                .put("/communities/{id}/users/{username}")
                .then()
                .statusCode(200)
                .body("members.username", hasItem("FanBook_785"));

        // Leave community
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 1)
                .pathParam("username", "FanBook_785")
                .queryParam("action", "leave")
                .when()
                .put("/communities/{id}/users/{username}")
                .then()
                .statusCode(200)
                .body("members.username", not(hasItem("FanBook_785")));


    }

    @Test
    @Order(10)
    void testBanUser() {
        String authCookieAdmin =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // Ban user
        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .body(Map.of("communityID", "1", "username", "BookReader_14", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(201)
                .body("user.username", equalTo("BookReader_14"));

        // Non-existent community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .body(Map.of("communityID", "9999", "username", "UserToBan", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(404);

        // Not logged in
        given()
                .contentType("application/json")
                .body(Map.of("communityID", "1", "username", "UserToBan", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(401);

        // Not authorized
        String authCookieUnauthorizedUser =
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

        given()
                .cookie("AuthToken", authCookieUnauthorizedUser)
                .contentType("application/json")
                .body(Map.of("communityID", "1", "username", "AdminReader", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(401);

        // Non-member user
        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .body(Map.of("communityID", "1", "username", "BookReader_14", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(401);

        // Login as FanBook_785
        String fbCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"FanBook_785\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // Try to ban site admin
        given()
                .cookie("AuthToken", fbCookie)
                .contentType("application/json")
                .body(Map.of("communityID", "4", "username", "AdminReader", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(11)
    void testIsUserModeratorOfCommunity() {

        // is user mod? main call
        given()
                .pathParam("id", 1)
                .pathParam("username", "BookReader_14")
                .when()
                .get("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(200)
                .body(anyOf(equalTo("true"), equalTo("false")));


        // Non-existent community
        given()
                .pathParam("id", 9999)
                .pathParam("username", "ModeratorUser")
                .when()
                .get("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(404);

        // User not found
        given()
                .pathParam("id", 1)
                .pathParam("username", "NonExistentUser")
                .when()
                .get("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(422);

    }

    @Test
    @Order(12)
    void testUnbanUser() {
        // Unauthorized
        String authCookieUnauthorizedUser =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"FanBook_785\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        given()
                .cookie("AuthToken", authCookieUnauthorizedUser)
                .pathParam("id", 1)
                .when()
                .delete("/bans/{id}")
                .then()
                .statusCode(401);


        // No autenticado
        given()
                .pathParam("id", 1)
                .when()
                .delete("/bans/{id}")
                .then()
                .statusCode(401);

        // Authorized
        String authCookieAdmin =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        // Unban successful
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 1) // Usa un ID de ban existente
                .when()
                .delete("/bans/{id}")
                .then()
                .statusCode(200)
                .body(equalTo("Ban removed"));

        // Ban not found
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 9999)
                .when()
                .delete("/bans/{id}")
                .then()
                .statusCode(404);

    }

    // TODO: Before continuing development, run all these tests to see what we are left to work with

}