package com.example.backend.rest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
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

    @Test
    @Order(13)
    void testManageModerators() {
        // Not logged 
        given()
                .pathParam("id", 1)
                .pathParam("username", "FanBook_785")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(401);

        // Login
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

        // Community doesn't exist
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 9999) // ID no existente
                .pathParam("username", "SomeUser")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(404);

        // Unauthorized user
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
                .pathParam("username", "SomeUser")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(401);


        // User not found
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 1)
                .pathParam("username", "NonExistentUser")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(422);

        // Not member
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 1)
                .pathParam("username", "FanBook_785")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(401);


        // Trying to promote the community admin
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 1)
                .pathParam("username", "AdminReader")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(401);

        // Not valid action
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 2)
                .pathParam("username", "BookReader_14")
                .queryParam("action", "invalidAction")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(400);

        // Promoted
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 2)
                .pathParam("username", "FanBook_785")
                .queryParam("action", "add")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(200)
                .body("moderators.username", hasItem("FanBook_785"));

        // Demoted
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 2)
                .pathParam("username", "FanBook_785")
                .queryParam("action", "remove")
                .when()
                .put("/communities/{id}/moderators/{username}")
                .then()
                .statusCode(200)
                .body("moderators.username", not(hasItem("FanBook_785")));
    }

    @Test
    @Order(14)
    void testUploadGetCommunityBanner() {
        // Login as AdminReader (admin of community)
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

        // Banner file
        File testBannerFile = new File("src/test/resources/banner_example1.jpg");

        // Upload banner authenticated
        given()
                .cookie("AuthToken", authCookie)
                .multiPart("file", testBannerFile)
                .when()
                .put("/communities/{id}/pictures", 1)
                .then()
                .statusCode(201);

        // Attempt to upload banner without authentication
        given()
                .multiPart("file", testBannerFile)
                .pathParam("id", 1)
                .when()
                .put("/communities/{id}/pictures")
                .then()
                .statusCode(401);

        // Attempt to upload an invalid file type
        File invalidFile = new File("src/test/resources/text_file_example.txt");
        given()
                .cookie("AuthToken", authCookie)
                .multiPart("file", invalidFile)
                .pathParam("id", 1)
                .when()
                .put("/communities/{id}/pictures")
                .then()
                .statusCode(400)
                .body(equalTo("File is not an image"));

        // Attempt to upload a large file
        File largeFile = new File("src/test/resources/large_image_example.jpg");
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("id", 1)
                .multiPart("file", largeFile)
                .when()
                .put("/communities/{id}/pictures")
                .then()
                .statusCode(400)
                .body(equalTo("File is too large. Max size is 5MB"));

        // Get banner
        given()
                .pathParam("id", 1)
                .when()
                .get("/communities/{id}/pictures")
                .then()
                .statusCode(200)
                .contentType("image/png");

        // Non-existent community
        given()
                .pathParam("id", 9999)
                .when()
                .get("/communities/{id}/pictures")
                .then()
                .statusCode(404);

        // Community without a banner
        given()
                .pathParam("id", 2)
                .when()
                .get("/communities/{id}/pictures")
                .then()
                .statusCode(404)
                .body(equalTo("Community banner not found"));
    }

    @Test
    @Order(15)
    void testGetBanInfo() {
        // Login as AdminReader
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

        // Not logged in
        given()
                .pathParam("id", 1)
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(401);

        // Non-existent ban
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 9999)
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(404);

        // Ban user
        int banId = given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .body(Map.of("communityID", "2", "username", "BookReader_14", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(201)
                .body("user.username", equalTo("BookReader_14"))
                .extract()
                .path("id");


        // Unauthorized user
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
                .pathParam("id", banId)
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(401);

        // Get ban reason
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", banId)
                .queryParam("banInfo", "reason")
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(200)
                .body(equalTo("Violation of rules"));

        // Get ban duration
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", banId)
                .queryParam("banInfo", "duration")
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(200)
                .body(matchesPattern("^\\d{4}-\\d{2}-\\d{2}.*"));

        // Get ban status
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", banId)
                .queryParam("banInfo", "status")
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(200)
                .body(equalTo("true"));

        // Get full ban
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", banId)
                .when()
                .get("/bans/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(banId));

    }

    @Test
    @Order(16)
    void testIsUserBanned() {
        // Login as AdminReader
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
        int banId = given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .body(Map.of("communityID", "2", "username", "FanBook_785", "duration", "week", "reason", "Violation of rules"))
                .when()
                .post("/bans")
                .then()
                .statusCode(201)
                .body("user.username", equalTo("FanBook_785"))
                .extract()
                .path("id");

        // Unauthorized access
        given()
                .pathParam("username", "BookReader_14")
                .pathParam("id", 2)
                .when()
                .get("/bans/users/{username}/communities/{id}")
                .then()
                .statusCode(401);

        // Non-existent community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("username", "SomeUser")
                .pathParam("id", 9999)
                .when()
                .get("/bans/users/{username}/communities/{id}")
                .then()
                .statusCode(404);

        // Unauthorized user check
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
                .pathParam("username", "FanBook_785")
                .pathParam("id", 2)
                .when()
                .get("/bans/users/{username}/communities/{id}")
                .then()
                .statusCode(401);

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("username", "FanBook_785")
                .pathParam("id", 2)
                .when()
                .get("/bans/users/{username}/communities/{id}")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(banId)));

        // User is not banned
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("username", "BookReader_14")
                .pathParam("id", 4)
                .when()
                .get("/bans/users/{username}/communities/{id}")
                .then()
                .statusCode(200)
                .body(equalTo("-1"));
    }

    @Test
    @Order(17)
    void testGetBannedUsers() {
        // Login
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

        // Unauthorized access (Not logged in)
        given()
                .pathParam("id", 1)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/bans/communities/{id}")
                .then()
                .statusCode(401);

        // Non-existent community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 9999)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/bans/communities/{id}")
                .then()
                .statusCode(404);

        // Unauthorized user attempt to get banned users
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
                .pathParam("id", 1)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/bans/communities/{id}")
                .then()
                .statusCode(401);

        // No banned users
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 1)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/bans/communities/{id}")
                .then()
                .statusCode(204);

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 2)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/bans/communities/{id}")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(18)
    void testGetMostPopularCommunities() {
        given()
                .queryParam("size", 5)
                .when()
                .get("/communities/most-popular")
                .then()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(5));

        given()
                .when()
                .get("/communities/most-popular")
                .then()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(10));
        
    }

    @Test
    @Order(19)
    void testIsUserMember() {
        // Check for unauthorized access when user is not logged in
        given()
                .pathParam("id", 1)
                .pathParam("username", "BookReader_14")
                .when()
                .get("/communities/{id}/users/{username}")
                .then()
                .statusCode(401); 
        
        // Login
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

        // Check for a non-existent community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 9999) // Assume this ID does not exist
                .pathParam("username", "someUser")
                .when()
                .get("/communities/{id}/users/{username}")
                .then()
                .statusCode(404); // Community not found

        // Verify user is a member of the community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 1)
                .pathParam("username", "AdminReader") // Assume AdminReader is a member
                .when()
                .get("/communities/{id}/users/{username}")
                .then()
                .statusCode(200)
                .body(equalTo("true")); // User is a member

        // Verify user is not a member of the community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("id", 4)
                .pathParam("username", "NonMemberUser") // Assume this user is not a member
                .when()
                .get("/communities/{id}/users/{username}")
                .then()
                .statusCode(200)
                .body(equalTo("false")); // User is not a member
    }
}