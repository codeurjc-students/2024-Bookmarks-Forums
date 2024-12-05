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
class RESTUserTest {

    @LocalServerPort
    private int port ;

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

    @Test
    @Order(51)
    void testGetUser() {
        String username = "AdminReader";

        // Login
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

        // Valid user
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("username", username)
                .when()
                .get("/users/{username}")
                .then()
                .statusCode(200)
                .body("username", equalTo(username));

        // Non-existing user
        given()
                .cookie("AuthToken", authCookie)
                .pathParam("username", "nonExistingUser")
                .when()
                .get("/users/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    void testIsUsernameTaken() {
        String takenUsername = "AdminReader";
        String availableUsername = "wowUser";

        // Username already taken
        given()
                .pathParam("username", takenUsername)
                .when()
                .get("/users/{username}/taken")
                .then()
                .statusCode(200)
                .body(equalTo("true"));

        // Username not taken
        given()
                .pathParam("username", availableUsername)
                .when()
                .get("/users/{username}/taken")
                .then()
                .statusCode(200)
                .body(equalTo("false"));
    }

    @Test
    void testGetUserFollowers() {
        String validUsername = "BookReader_14";
        String nonExistentUsername = "nonExistentUser";

        // Valid username
        given()
                .pathParam("username", validUsername)
                .when()
                .get("/users/{username}/followers")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0)); // Assuming the body returns a list

        // Non-existent username
        given()
                .pathParam("username", nonExistentUsername)
                .when()
                .get("/users/{username}/followers")
                .then()
                .statusCode(404);
    }

    @Test
    void testIsUserFollowing() {
        String followerUsername = "BookReader_14";
        String followingUsername = "AdminReader";
        String nonFollowingUsername = "YourReader";
        String nonExistentUsername = "nonExistentUser";

        // User follows the other user (true response)
        given()
                .pathParam("follower", followerUsername)
                .pathParam("following", followingUsername)
                .when()
                .get("/users/{follower}/following/{following}")
                .then()
                .statusCode(200)
                .body(equalTo("true"));

        // User does not follow the other user
        given()
                .pathParam("follower", followingUsername)
                .pathParam("following", nonFollowingUsername)
                .when()
                .get("/users/{follower}/following/{following}")
                .then()
                .statusCode(200)
                .body(equalTo("false"));

        // Non-existent username
        given()
                .pathParam("follower", followerUsername)
                .pathParam("following", nonExistentUsername)
                .when()
                .get("/users/{follower}/following/{following}")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetUserFollowing() {
        String validUsername = "BookReader_14";
        String nonExistentUsername = "nonExistentUser";

        // Valid username
        given()
                .pathParam("username", validUsername)
                .when()
                .get("/users/{username}/following")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0))
                .body("username", hasItem("AdminReader")); // BookReader_14 follows AdminReader, so it must appear

        // Non-existent username
        given()
                .pathParam("username", nonExistentUsername)
                .when()
                .get("/users/{username}/following")
                .then()
                .statusCode(404);
    }

    @Test
    void testSearchUsers() {
        String existingUsername = "BookReader_14";
        String nonExistentUsername = "nonExistentUser";
        boolean orderByCreationDate = true;

        // Existing user search with orderByCreationDate true
        given()
                .queryParam("query", existingUsername)
                .queryParam("orderByCreationDate", orderByCreationDate)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("username", hasItem(existingUsername));

        // Existing user search with orderByCreationDate false
        given()
                .queryParam("query", existingUsername)
                .queryParam("orderByCreationDate", !orderByCreationDate)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("username", hasItem(existingUsername));

        // Non-existent user search with orderByCreationDate true
        given()
                .queryParam("query", nonExistentUsername)
                .queryParam("orderByCreationDate", orderByCreationDate)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));  // Assuming the body returns an empty list
    }

    @Test
    void testGetUserCommunities() {
        String existingUsername = "BookReader_14";
        String nonExistentUsername = "nonExistentUser";

        // Case 1: Existing user as member
        given()
                .pathParam("username", existingUsername)
                .queryParam("admin", false)
                .when()
                .get("/users/{username}/communities")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0))
                .body("name", hasItem("Bookmarks Reviews")); // Checking if "Bookmarks Reviews" is in the response

        // Case 2: Existing user as admin
        given()
                .pathParam("username", existingUsername)
                .queryParam("admin", true)
                .when()
                .get("/users/{username}/communities")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0)); // Assuming the body returns a list

        // Case 3: Non-existent user
        given()
                .pathParam("username", nonExistentUsername)
                .queryParam("admin", false)
                .when()
                .get("/users/{username}/communities")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetUserCommunitiesCount() {
        String existingUsername = "BookReader_14";
        String nonExistentUsername = "nonExistentUser";

        // Case 1: Existing user as member
        given()
                .pathParam("username", existingUsername)
                .queryParam("admin", false)
                .when()
                .get("/users/{username}/communities/count")
                .then()
                .statusCode(200)
                .body(greaterThanOrEqualTo(Integer.toString(0)));

        // Case 2: Existing user as admin
        given()
                .pathParam("username", existingUsername)
                .queryParam("admin", true)
                .when()
                .get("/users/{username}/communities/count")
                .then()
                .statusCode(200)
                .body(greaterThanOrEqualTo(Integer.toString(0)));

        // Case 3: Non-existent user
        given()
                .pathParam("username", nonExistentUsername)
                .queryParam("admin", false)
                .when()
                .get("/users/{username}/communities/count")
                .then()
                .statusCode(404);

        // Case 4: Missing 'admin' parameter
        given()
                .pathParam("username", existingUsername)
                .when()
                .get("/users/{username}/communities/count")
                .then()
                .statusCode(400);
    }

    @Test
    void testGetUsersPostsCount() {
        String existingUsername = "BookReader_14";
        String nonExistentUsername = "nonExistentUser";

        // Case 1: Existing user with posts
        given()
                .pathParam("username", existingUsername)
                .when()
                .get("/users/{username}/posts/count")
                .then()
                .statusCode(200)
                .body(greaterThanOrEqualTo(Integer.toString(0)));

        // Case 2: Non-existent user
        given()
                .pathParam("username", nonExistentUsername)
                .when()
                .get("/users/{username}/posts/count")
                .then()
                .statusCode(404);
    }

    @Test
    void testRegisterUser() {
        // Valid registration data
        String username = "NewUser";
        String email = "newuser@example.com";
        String alias = "New Alias";
        String password = "Abcd!1234";

        // Existing data
        String existingUsername = "BookReader_14";
        String existingEmail = "bookreader14@gmail.com";

        // Case 1: Successful signup
        given()
                .formParam("username", username)
                .formParam("email", email)
                .formParam("alias", alias)
                .formParam("password", password)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        // Case 2: Username in use
        given()
                .formParam("username", existingUsername)
                .formParam("email", email)
                .formParam("alias", alias)
                .formParam("password", password)
                .when()
                .post("/users")
                .then()
                .statusCode(409);

        // Case 3: Email in use
        given()
                .formParam("username", username)
                .formParam("email", existingEmail)
                .formParam("alias", alias)
                .formParam("password", password)
                .when()
                .post("/users")
                .then()
                .statusCode(409);

        // Case 4: blank username or email
        given()
                .formParam("username", "")
                .formParam("email", "")
                .formParam("alias", alias)
                .formParam("password", password)
                .when()
                .post("/users")
                .then()
                .statusCode(409);

        // Case 5: blank alias
        given()
                .formParam("username", username)
                .formParam("email", email)
                .formParam("alias", "")
                .formParam("password", password)
                .when()
                .post("/users")
                .then()
                .statusCode(409);

        // Case 6: blank password
        given()
                .formParam("username", username)
                .formParam("email", email)
                .formParam("alias", alias)
                .formParam("password", "")
                .when()
                .post("/users")
                .then()
                .statusCode(409);

        // Case 7: missing parameters
        given()
                .when()
                .post("/users")
                .then()
                .statusCode(400);
    }

    @Test
    void testUpdateUser() {

        // Case 0: not logged in
        given().when().put("/users/me").then().statusCode(401);

        String authCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"YourReader\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        String username = "YourReader";

        // User trying to modify another user
        given()
                .pathParam("username", "AdminReader")
                .when()
                .put("/users/{username}")
                .then()
                .statusCode(401);

        // Case 1: Update multiple fields
        Map<String, String> userInfo = Map.of(
                "alias", "Updated Alias",
                "description", "New description",
                "email", "updated.email@example.com",
                "password", "NewPass!234"
        );

        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .body(userInfo)
                .when()
                .put("/users/{username}?action=edit", username)
                .then()
                .statusCode(200)
                .body("alias", equalTo(userInfo.get("alias")))
                .body("description", equalTo(userInfo.get("description")))
                .body("email", equalTo(userInfo.get("email")));

        // Case 2: Update only email
        String newEmail = "new.email@example.com";
        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .body("{ \"email\": \"" + newEmail + "\" }")
                .when()
                .put("/users/{username}?action=edit", username)
                .then()
                .statusCode(200)
                .body("email", equalTo(newEmail));

        // Case 3: Update only alias
        String newAlias = "CoolAlias";
        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .body("{ \"alias\": \"" + newAlias + "\" }")
                .when()
                .put("/users/{username}?action=edit", username)
                .then()
                .statusCode(200)
                .body("alias", equalTo(newAlias));

        // Assuming "AnotherUser" exists for follow/unfollow actions
        String otherUsername = "AdminReader";

        // Case 4: Follow a user
        int initialFollowingCount = given().pathParam("username", username).when().get("/users/{username}/following").then().extract().body().path("size()");
        int initialFollowersCount = given().pathParam("username", otherUsername).when().get("/users/{username}/followers").then().extract().body().path("size()");

        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .when()
                .put("/users/{username}?action=follow&otherUsername={otherUsername}", username, otherUsername)
                .then()
                .statusCode(200)
                .body("following", equalTo(initialFollowingCount + 1));

        given().pathParam("username", username).when().get("/users/{username}/following").then().body("size()", equalTo(initialFollowingCount + 1));
        given().pathParam("username", otherUsername).when().get("/users/{username}/followers").then().body("size()", equalTo(initialFollowersCount + 1));

        // Case 5: Unfollow a user
        given()
                .cookie("AuthToken", authCookie)
                .contentType("application/json")
                .when()
                .put("/users/{username}?action=unfollow&otherUsername={otherUsername}", username, otherUsername)
                .then()
                .statusCode(200)
                .body("following", equalTo(initialFollowingCount));

        given().pathParam("username", username).when().get("/users/{username}/following").then().body("size()", equalTo(initialFollowingCount));
        given().pathParam("username", otherUsername).when().get("/users/{username}/followers").then().body("size()", equalTo(initialFollowersCount));
    }

    @Test
    void testDeleteUser() {
        // No login
        given()
                .when()
                .delete("/users/{username}", "YourReader")
                .then()
                .statusCode(401);

        // Logged in
        String authCookie = given()
                .contentType("application/json")
                .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("AuthToken")
                .extract()
                .cookie("AuthToken");

        // Non existent user
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .delete("/users/{username}", "NonExistentUser")
                .then()
                .statusCode(404);

        // Community administrator user
        String userAdminCommunity = "FanBook_785";
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .delete("/users/{username}", userAdminCommunity)
                .then()
                .statusCode(403)
                .body(equalTo("User is the admin of one or more communities"));

        // Admin trying to delete their account
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .delete("/users/{username}", "AdminReader")
                .then()
                .statusCode(401);

        // Delete user account
        given()
                .contentType("application/json")
                .cookie("AuthToken", authCookie)
                .when()
                .delete("/users/{username}", "YourReader")
                .then()
                .statusCode(200)
                .body(equalTo("YourReader has been deleted"));
    }

    @Test
    void testChangeProfilePicture() {
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

        // Image file
        File testImageFile = new File("src/test/resources/profile_picture_example.jpg");

        // REST Call
        given()
                .cookie("AuthToken", authCookie)
                .multiPart("file", testImageFile)
                .when()
                .put("/users/{username}/pictures", "AdminReader")
                .then()
                .statusCode(201);
    }

    @Test
    void testGetProfilePicture() {
        String username = "AdminReader";

        given()
                .when()
                .get("/users/{username}/pictures", username)
                .then()
                .statusCode(200)
                .contentType("image/png"); // is png?
    }

    @Test
    void testGetMostPopularUsers() {
        given()
                .queryParam("size", 10)
                .when()
                .get("/users/most-popular")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void testLogout() {

        // Try logout without logging in
        given()
                .when()
                .post("/logout")
                .then()
                .statusCode(401);


        // Login in order to logout
        String authCookie = given()
                .contentType("application/json")
                .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("AuthToken")
                .extract()
                .cookie("AuthToken");

        // Logout
        given()
                .cookie("AuthToken", authCookie)
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));

        // Try to access to an auth protected content
        given()
                .when()
                .get("/users/me")
                .then()
                .statusCode(401);
    }

    @Test
    void testRefreshToken() {
        // Login
        String refreshToken = given()
                .contentType("application/json")
                .body("{\"username\": \"AdminReader\", \"password\": \"adminpass\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .cookie("refreshToken")
                .extract()
                .cookie("refreshToken");

        // Refresh token
        given()
                .cookie("refreshToken", refreshToken)
                .when()
                .post("/refresh")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));

        // Invalid token refresh
        given()
                .cookie("refreshToken", "invalidToken")
                .when()
                .post("/refresh")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILURE"));
    }
}
