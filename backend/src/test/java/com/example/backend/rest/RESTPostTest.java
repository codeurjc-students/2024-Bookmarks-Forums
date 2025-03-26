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
class RESTPostTest {

    @LocalServerPort
    private int port;

    private int newPostWithImageId;

    private int newPostId;

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
    void testGetPostById() {
        given()
                .pathParam("postId", 1)
                .when()
                .get("/posts/{postId}")
                .then()
                .statusCode(200);

        given()
                .pathParam("postId", 9999)
                .when()
                .get("/posts/{postId}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(2)
    void testSearchPosts() {

        // Successful search returning posts
        given()
                .queryParam("query", "Bookmarks")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "creationDate")
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("get(0).identifier", anyOf(equalTo(2), equalTo(1), equalTo(3), equalTo(4)));


        given()
                .queryParam("query", "nonExistentKeyword")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "creationDate")
                .when()
                .get("/posts")
                .then()
                .statusCode(204); // Posts not found

        given()
                .queryParam("query", "Bookmarks")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("order", "likes")
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("get(0).identifier", equalTo(2));


        given()
                .queryParam("query", "Bookmarks")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "default")
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("get(0).identifier", equalTo(1));
    }

    @Test
    @Order(3)
    void testGetCommunityPosts() {
        // Attempt to get posts from an unrecognized community ID
        given()
                .pathParam("communityID", 9999)
                .when()
                .get("/communities/{communityID}/posts")
                .then()
                .statusCode(404);

        // Get posts from an existing community
        given()
                .pathParam("communityID", 1)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "creationDate")
                .when()
                .get("/communities/{communityID}/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", anyOf(equalTo(1), equalTo(5)));

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

        // New community
        int commId = given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .body(Map.of("name", "testPostsComm", "description", "A posts testing community"))
                .when()
                .post("/communities")
                .then()
                .statusCode(201)
                .extract()
                .path("identifier");


        // Request posts from a community with no posts
        given()
                .pathParam("communityID", commId)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "creationDate")
                .when()
                .get("/communities/{communityID}/posts")
                .then()
                .statusCode(204); // No posts found

        // Test post counting
        given()
                .pathParam("communityID", 2)
                .queryParam("count", true)
                .when()
                .get("/communities/{communityID}/posts")
                .then()
                .statusCode(200)
                .body(equalTo(Integer.toString(1)));

        given()
                .pathParam("communityID", 2)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("sort", "likes")
                .when()
                .get("/communities/{communityID}/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(2))
                .body("get(0).identifier", equalTo(2));
    }

    @Test
    @Order(4)
    void testGetPostsByUsername() {
        // Attempt to retrieve posts from a non-existent user
        given()
                .pathParam("username", "nonExistentUser")
                .when()
                .get("/users/{username}/posts")
                .then()
                .statusCode(204);

        // Retrieve posts of an existing user
        given()
                .pathParam("username", "AdminReader")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "creationDate")
                .when()
                .get("/users/{username}/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(0))
                .body("get(0).identifier", equalTo(1));

        // Test search functionality with query
        given()
                .pathParam("username", "AdminReader")
                .queryParam("query", "welcome")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "lastModifiedDate")
                .when()
                .get("/users/{username}/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(1));

        // Test pagination and sorting by 'likes'
        given()
                .pathParam("username", "AdminReader")
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("order", "likes")
                .when()
                .get("/users/{username}/posts")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(2));
    }

    @Test
    @Order(5)
    void testCreatePost() {
        // Attempt to create a post without being logged in
        given()
                .contentType("multipart/form-data")
                .multiPart("title", "Test Post")
                .multiPart("content", "This is a test post content.")
                .pathParam("communityID", 1)
                .when()
                .post("/communities/{communityID}/posts")
                .then()
                .statusCode(401);

        // Authenticate as AdminReader
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

        // Successful post creation
        int communityId = 1;
        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("multipart/form-data")
                .multiPart("title", "Valid Post Title")
                .multiPart("content", "This is some valid post content.")
                .pathParam("communityID", communityId)
                .when()
                .post("/communities/{communityID}/posts")
                .then()
                .statusCode(201)
                .contentType("application/json")
                .body("title", equalTo("Valid Post Title"));

        // Invalid input (Missing title or content)
        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("multipart/form-data")
                .multiPart("content", "Content without a title")
                .pathParam("communityID", communityId)
                .when()
                .post("/communities/{communityID}/posts")
                .then()
                .statusCode(400);

        // Non-existent community
        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("multipart/form-data")
                .multiPart("title", "Test Post")
                .multiPart("content", "This is a test post content.")
                .pathParam("communityID", 9999)
                .when()
                .post("/communities/{communityID}/posts")
                .then()
                .statusCode(400);

        // User not a member of the community
        // Authenticate as BookReader_14
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

        given()
                .cookie("AuthToken", authCookie)
                .contentType("multipart/form-data")
                .multiPart("title", "Should Fail")
                .multiPart("content", "This post should fail to be created.")
                .pathParam("communityID", 5)
                .when()
                .post("/communities/{communityID}/posts")
                .then()
                .statusCode(403);

        // Post image file
        File imageFile = new File("src/test/resources/banner_example1.jpg");
        newPostWithImageId = given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("multipart/form-data")
                .multiPart("title", "Valid Post Title With Image")
                .multiPart("content", "This is some valid post content with an image.")
                .multiPart("image", imageFile)
                .pathParam("communityID", communityId)
                .when()
                .post("/communities/{communityID}/posts")
                .then()
                .statusCode(201)
                .contentType("application/json")
                .body("title", equalTo("Valid Post Title With Image"))
                .extract()
                .path("identifier");
    }

    @Test
    @Order(7)
    void testGetPostImage() {
        // Attempt to get an image for a post that does not exist
        given()
                .pathParam("postId", 9999)
                .when()
                .get("/posts/{postId}/pictures")
                .then()
                .statusCode(404);

        // Setup: Add an image to a post
        if (newPostWithImageId == 0) {
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

            File imageFile = new File("src/test/resources/banner_example1.jpg");

            newPostWithImageId =
                    given()
                            .cookie("AuthToken", authCookieAdmin)
                            .contentType("multipart/form-data")
                            .multiPart("title", "Valid Post Title With Image")
                            .multiPart("content", "This is some valid post content with an image.")
                            .multiPart("image", imageFile)
                            .pathParam("communityID", 1)
                            .when()
                            .post("/communities/{communityID}/posts")
                            .then()
                            .statusCode(201)
                            .contentType("application/json")
                            .body("title", equalTo("Valid Post Title With Image"))
                            .extract()
                            .path("identifier");
        }


        // Successful retrieval of the post image
        given()
                .pathParam("postId", newPostWithImageId)
                .when()
                .get("/posts/{postId}/pictures")
                .then()
                .statusCode(200)
                .contentType(anyOf(equalTo("image/jpeg"), equalTo("image/png")));

        // Attempt to get an image for a post that exists but has no image
        int postIdWithoutImage = 5;
        given()
                .pathParam("postId", postIdWithoutImage)
                .when()
                .get("/posts/{postId}/pictures")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void testDeletePostImage() {
        // Attempt to delete post image without logging in
        given()
                .pathParam("postId", 1)
                .when()
                .delete("/posts/{postId}/pictures")
                .then()
                .statusCode(401);

        // Authenticate as AdminReader
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

        if (newPostWithImageId == 0) {
            File imageFile = new File("src/test/resources/banner_example1.jpg");

            newPostWithImageId =
                    given()
                            .cookie("AuthToken", authCookieAdmin)
                            .contentType("multipart/form-data")
                            .multiPart("title", "Valid Post Title With Image")
                            .multiPart("content", "This is some valid post content with an image.")
                            .multiPart("image", imageFile)
                            .pathParam("communityID", 1)
                            .when()
                            .post("/communities/{communityID}/posts")
                            .then()
                            .statusCode(201)
                            .contentType("application/json")
                            .body("title", equalTo("Valid Post Title With Image"))
                            .extract()
                            .path("identifier");
        }

        // Successful deletion of the post image
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", newPostWithImageId)
                .when()
                .delete("/posts/{postId}/pictures")
                .then()
                .statusCode(200);

        // Attempt to delete the image of a non-existent post
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 9999)
                .when()
                .delete("/posts/{postId}/pictures")
                .then()
                .statusCode(404);

        // Attempt to delete an image by an unauthorized user
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

        given()
                .cookie("AuthToken", authCookie)
                .pathParam("postId", newPostWithImageId)
                .when()
                .delete("/posts/{postId}/pictures")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(8)
    void testHasUserVotedPost() {
        // Attempt to check vote status on a non-existent post
        given()
                .pathParam("postId", 9999)
                .queryParam("username", "testUser")
                .queryParam("type", "upvote")
                .when()
                .get("/posts/{postId}/votes")
                .then()
                .statusCode(404);

        // Attempt with an invalid type
        given()
                .pathParam("postId", 1)
                .queryParam("username", "testUser")
                .queryParam("type", "invalid")
                .when()
                .get("/posts/{postId}/votes")
                .then()
                .statusCode(400);

        // Attempt with missing username
        given()
                .pathParam("postId", 1)
                .queryParam("username", "")
                .queryParam("type", "upvote")
                .when()
                .get("/posts/{postId}/votes")
                .then()
                .statusCode(400);

        // Check if a user has upvoted an existing post
        given()
                .pathParam("postId", 2)
                .queryParam("username", "BookReader_14")
                .queryParam("type", "upvote")
                .when()
                .get("/posts/{postId}/votes")
                .then()
                .statusCode(200)
                .body(equalTo("true"));

        // Check if a user has downvoted an existing post
        given()
                .pathParam("postId", 1)
                .queryParam("username", "BookReader_14")
                .queryParam("type", "downvote")
                .when()
                .get("/posts/{postId}/votes")
                .then()
                .statusCode(200)
                .body(equalTo("true"));

        // Check if a user has not downvoted an existing post
        given()
                .pathParam("postId", 2)
                .queryParam("username", "AdminReader")
                .queryParam("type", "downvote")
                .when()
                .get("/posts/{postId}/votes")
                .then()
                .statusCode(200)
                .body(equalTo("false"));
    }

    @Test
    @Order(9)
    void testEditPost() {
        // Attempt to edit a post without logging in
        given()
                .pathParam("postId", 1)
                .formParam("action", "edit")
                .formParam("title", "New Title")
                .formParam("content", "Updated content for the post.")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(401);

        // Authenticate as AdminReader
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

        // Attempt to edit a non-existent post
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 9999)
                .formParam("action", "edit")
                .formParam("title", "Non-Existent Title")
                .formParam("content", "Content for a non-existent post.")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(404);

        // Attempt to edit an existing post but with invalid input (missing title)
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "edit")
                .formParam("content", "New content without a title.")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(400);

        // Successfully edit an existing post (with image)
        File imageFile = new File("src/test/resources/banner_example1.jpg");
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "edit")
                .formParam("title", "Updated Title")
                .formParam("content", "Updated content for the post.")
                .multiPart("image", imageFile)
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("title", equalTo("Updated Title"))
                .body("content", equalTo("Updated content for the post."));

        // Attempt to edit an existing post by an unauthorized user (not the author or admin)
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

        given()
                .cookie("AuthToken", authCookie)
                .pathParam("postId", 1)
                .formParam("action", "edit")
                .formParam("title", "Unauthorized Edit Attempt")
                .formParam("content", "This should not be allowed.")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(oneOf(401, 403));

        // Upvote a post
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "upvote")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("upvotes", equalTo(0)); // The post was already liked by adminReader, so it gets removed

        // Upvote again
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "upvote")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("upvotes", equalTo(1)); // Restored like

        // Downvote
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "downvote")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("upvotes", equalTo(0)) // The like gets removed
                .body("downvotes", equalTo(2)); // Adds a downvote

        // Remove downvote by liking again
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "upvote")
                .when()
                .put("/posts/{postId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("downvotes", equalTo(1))
                .body("upvotes", equalTo(1)); // The like gets removed
    }

    @Test
    @Order(10)
    void testUpdatePostImage() {
        // Attempt to update post image without logging in
        given()
                .pathParam("postId", 1)
                .formParam("action", "update")
                .multiPart("image", new File("src/test/resources/banner_example1.jpg"))
                .when()
                .put("/posts/{postId}/pictures")
                .then()
                .statusCode(401);

        // Authenticate as AdminReader
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

        // Attempt to update a non-existent post
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 9999)
                .formParam("action", "update")
                .multiPart("image", new File("src/test/resources/banner_example1.jpg"))
                .when()
                .put("/posts/{postId}/pictures")
                .then()
                .statusCode(404);

        // Attempt to update with too large image
        File largeImage = new File("src/test/resources/large_image_example.jpg");
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "update")
                .multiPart("image", largeImage)
                .when()
                .put("/posts/{postId}/pictures")
                .then()
                .statusCode(400);

        // Successfully update post image
        File validImage = new File("src/test/resources/banner_example1.jpg");
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "update")
                .multiPart("image", validImage)
                .when()
                .put("/posts/{postId}/pictures")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("hasImage", equalTo(true));

        // Delete post image
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 1)
                .formParam("action", "delete")
                .when()
                .put("/posts/{postId}/pictures")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("hasImage", equalTo(false));

        // Attempt to update post image by unauthorized user
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

        given()
                .cookie("AuthToken", authCookie)
                .pathParam("postId", 1)
                .formParam("action", "update")
                .multiPart("image", validImage)
                .when()
                .put("/posts/{postId}/pictures")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(11)
    void testDeletePost() {
        // Attempt to delete post without logging in
        given()
                .pathParam("postId", 1)
                .when()
                .delete("/posts/{postId}")
                .then()
                .statusCode(401);

        // Authenticate as AdminReader
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

        // Attempt to delete a non-existent post
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", 9999)
                .when()
                .delete("/posts/{postId}")
                .then()
                .statusCode(404);

        // Successfully delete a post
        int postId = 1;
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("postId", postId)
                .when()
                .delete("/posts/{postId}")
                .then()
                .statusCode(200);

        // Attempt to delete a post by an unauthorized user (not author, admin, or mod)
        String authCookieUser =
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

        int communityId = 1;
        newPostId =
                given()
                        .cookie("AuthToken", authCookieAdmin)
                        .contentType("multipart/form-data")
                        .multiPart("title", "Valid Post Title")
                        .multiPart("content", "This is some valid post content.")
                        .pathParam("communityID", communityId)
                        .when()
                        .post("/communities/{communityID}/posts")
                        .then()
                        .statusCode(201)
                        .contentType("application/json")
                        .body("title", equalTo("Valid Post Title"))
                        .extract()
                        .body().jsonPath().getInt("identifier");

        given()
                .cookie("AuthToken", authCookieUser)
                .pathParam("postId", newPostId)
                .when()
                .delete("/posts/{postId}")
                .then()
                .statusCode(403);

    }

    @Test
    @Order(13)
    void testGetRepliesOfPost() {

        // Attempt to get replies for a post that does not exist
        given()
                .pathParam("postId", 9999)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "creationDate")
                .when()
                .get("/posts/{postId}/replies/all")
                .then()
                .statusCode(204);

        int postIdWithReplies = 2;

        // Successfully retrieve replies of a post
        given()
                .pathParam("postId", postIdWithReplies)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "creationDate")
                .when()
                .get("/posts/{postId}/replies/all")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0));

        // Test with custom pagination and sorting
        given()
                .pathParam("postId", postIdWithReplies)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .queryParam("order", "likes")
                .when()
                .get("/posts/{postId}/replies/all")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5))
                .body("get(0).identifier", equalTo(2));


        int postIdWithoutReplies = newPostId;

        // Attempt to get replies for a post that exists but has no replies
        given()
                .pathParam("postId", postIdWithoutReplies)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("order", "creationDate")
                .when()
                .get("/posts/{postId}/replies/all")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(14)
    void testGetReplyById() {
        // Attempt to get a reply that does not exist
        given()
                .pathParam("replyId", 9999)
                .when()
                .get("/replies/{replyId}")
                .then()
                .statusCode(404);

        int existingReplyId = 2;

        // Successfully retrieve a reply by its ID
        given()
                .pathParam("replyId", existingReplyId)
                .when()
                .get("/replies/{replyId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("identifier", equalTo(existingReplyId));
    }

    @Test
    @Order(15)
    void testSearchReplies() {
        // Searching for replies by title
        given()
                .queryParam("criteria", "title")
                .queryParam("query", "New")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Searching for replies by content
        given()
                .queryParam("criteria", "content")
                .queryParam("query", "looking forward")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Searching for replies by author
        given()
                .queryParam("criteria", "author")
                .queryParam("query", "BookReader_14")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Searching with invalid criteria should fallback to default search
        given()
                .queryParam("criteria", "invalidCriteria")
                .queryParam("query", "new")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));


        // Search with no results
        given()
                .queryParam("criteria", "title")
                .queryParam("query", "NonExistentTitle")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/replies")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(16)
    void testSearchRepliesByPost() {
        int existingPostId = 2;
        int nonExistentPostId = 9999;

        // Searching for replies by title
        given()
                .pathParam("postId", existingPostId)
                .queryParam("criteria", "title")
                .queryParam("query", "New")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/posts/{postId}/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Searching for replies by content
        given()
                .pathParam("postId", existingPostId)
                .queryParam("criteria", "content")
                .queryParam("query", "forward")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/posts/{postId}/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Searching for replies by author
        given()
                .pathParam("postId", existingPostId)
                .queryParam("criteria", "author")
                .queryParam("query", "bookreader")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/posts/{postId}/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Searching with invalid criteria should fallback to default search
        given()
                .pathParam("postId", existingPostId)
                .queryParam("criteria", "invalidCriteria")
                .queryParam("query", "New")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/posts/{postId}/replies")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("get(0).identifier", equalTo(2));

        // Search replies for a non-existent post
        given()
                .pathParam("postId", nonExistentPostId)
                .queryParam("criteria", "title")
                .queryParam("query", "Doesn't matter")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/posts/{postId}/replies")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(17)
    void testCreateReply() {
        Map<String, String> replyData = Map.of(
                "title", "Test Reply Title",
                "content", "This is a test reply content"
        );

        given()
                .contentType("application/json")
                .pathParam("postId", 1)
                .body(replyData)
                .when()
                .post("/posts/{postId}/replies")
                .then()
                .statusCode(401);

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

        int postId = 2;

        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .pathParam("postId", postId)
                .body(replyData)
                .when()
                .post("/posts/{postId}/replies")
                .then()
                .statusCode(201)
                .contentType("application/json")
                .body("title", equalTo("Test Reply Title"))
                .body("content", equalTo("This is a test reply content"));

        // Only content (missing title) = invalid reply
        Map<String, String> replyDataInvalid = Map.of(
                "content", "Content without a title"
        );

        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .pathParam("postId", postId)
                .body(replyDataInvalid)
                .when()
                .post("/posts/{postId}/replies")
                .then()
                .statusCode(400);

        // Too long title
        Map<String, String> replyDataLongTitle = Map.of(
                "title", "A".repeat(151),
                "content", "This is a valid reply content"
        );

        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .pathParam("postId", postId)
                .body(replyDataLongTitle)
                .when()
                .post("/posts/{postId}/replies")
                .then()
                .statusCode(413);

        // Too long content
        Map<String, String> replyDataLongContent = Map.of(
                "title", "Valid Reply Title",
                "content", "B".repeat(501)
        );

        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .pathParam("postId", postId)
                .body(replyDataLongContent)
                .when()
                .post("/posts/{postId}/replies")
                .then()
                .statusCode(413);

        int nonExistentPostId = 9999;

        given()
                .cookie("AuthToken", authCookieAdmin)
                .contentType("application/json")
                .pathParam("postId", nonExistentPostId)
                .body(replyData)
                .when()
                .post("/posts/{postId}/replies")
                .then()
                .statusCode(404);

    }

    @Test
    @Order(18)
    void testLikeReply() {
        given()
                .pathParam("replyId", 1)
                .queryParam("action", "like")
                .when()
                .put("/replies/{replyId}")
                .then()
                .statusCode(401); // Esperar Unauthorized

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

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("replyId", 9999)
                .queryParam("action", "like")
                .when()
                .put("/replies/{replyId}")
                .then()
                .statusCode(404);

        int existingReplyId = 2;

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("replyId", existingReplyId)
                .queryParam("action", "like")
                .when()
                .put("/replies/{replyId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("likes", equalTo(1));

        // Remove like
        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("replyId", existingReplyId)
                .queryParam("action", "like")
                .when()
                .put("/replies/{replyId}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("likes", equalTo(0));

        String bannedAuthCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"ZBadDude\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        given()
                .cookie("AuthToken", bannedAuthCookie)
                .pathParam("replyId", 2)
                .queryParam("action", "like")
                .when()
                .put("/replies/{replyId}")
                .then()
                .statusCode(401);

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("replyId", existingReplyId)
                .queryParam("action", "invalid")
                .when()
                .put("/replies/{replyId}")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(20)
    void testDeleteReply() {
        int existingReplyId = 2;

        given()
                .pathParam("replyId", existingReplyId)
                .when()
                .delete("/replies/{replyId}")
                .then()
                .statusCode(401);

        // AdminReader
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

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("replyId", existingReplyId)
                .when()
                .delete("/replies/{replyId}")
                .then()
                .statusCode(200);

        given()
                .cookie("AuthToken", authCookieAdmin)
                .pathParam("replyId", 9999)
                .when()
                .delete("/replies/{replyId}")
                .then()
                .statusCode(404);

        String authCookieUser =
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

        // New reply by AdminReader
        Map<String, String> replyData = Map.of(
                "title", "Test Reply Title 2",
                "content", "This is a test reply content 2"
        );
        int notRemovableReplyId =
                given()
                        .cookie("AuthToken", authCookieAdmin)
                        .contentType("application/json")
                        .pathParam("postId", 2)
                        .body(replyData)
                        .when()
                        .post("/posts/{postId}/replies")
                        .then()
                        .statusCode(201)
                        .contentType("application/json")
                        .body("title", equalTo("Test Reply Title 2"))
                        .body("content", equalTo("This is a test reply content 2"))
                        .extract()
                        .body().jsonPath().getInt("identifier");


        String fanBookUserCookie =
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
                .cookie("AuthToken", fanBookUserCookie)
                .pathParam("replyId", notRemovableReplyId)
                .when()
                .delete("/replies/{replyId}")
                .then()
                .statusCode(oneOf(401, 403));

        String bannedAuthCookie =
                given()
                        .contentType("application/json")
                        .body("{\"username\": \"ZBadDude\", \"password\": \"pass\"}")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(200)
                        .cookie("AuthToken")
                        .extract()
                        .cookie("AuthToken");

        given()
                .cookie("AuthToken", bannedAuthCookie)
                .pathParam("replyId", notRemovableReplyId)
                .when()
                .delete("/replies/{replyId}")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(21)
    void testGetMostLikedPostsOfMostFollowedUsers() {
        // Attempt to access without being logged in
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/following/posts/most-liked")
                .then()
                .statusCode(401);

        // Authenticate
        String authCookieUser =
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
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/following/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0)); // Ensure at least one post is returned

        // Test pagination
        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/users/me/following/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5));

        // When there are no posts found
        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/users/me/following/posts/most-liked")
                .then()
                .statusCode(204);

        // Authenticate as admin
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

        // Admin should also retrieve posts successfully
        given()
                .cookie("AuthToken", authCookieAdmin)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/following/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(22)
    void testGetMostLikedPostsOfUserCommunities() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/communities/posts/most-liked")
                .then()
                .statusCode(401);

        // Authenticate as a standard user
        String authCookieUser =
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

        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/communities/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(0));

        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/users/me/communities/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5));

        // Scenario where no posts in certain page
        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/users/me/communities/posts/most-liked")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(23)
    void testGetMostRecentPostsOfUserCommunities() {
        // Not logged in
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/communities/posts/most-recent")
                .then()
                .statusCode(401);

        // Auth
        String authCookieUser =
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

        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/me/communities/posts/most-recent")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0));

        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/users/me/communities/posts/most-recent")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5));

        given()
                .cookie("AuthToken", authCookieUser)
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/users/me/communities/posts/most-recent")
                .then()
                .statusCode(204);

    }

    @Test
    @Order(24)
    void testGetMostRecentPostsOfAllCommunities() {
        // Not logged in
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/most-popular/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(0));

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/most-popular/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0));

        given()
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/communities/most-popular/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5));

        given()
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/communities/most-popular/posts/most-liked")
                .then()
                .statusCode(204);

        given()
                .queryParam("page", 0)
                .queryParam("size", 100)
                .when()
                .get("/communities/most-popular/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(100));
    }

    @Test
    @Order(25)
    void testGetMostLikedPostsOfMostFollowedUsersGeneral() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(0));

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0));

        given()
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/users/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5));

        given()
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/users/posts/most-liked")
                .then()
                .statusCode(204);

        given()
                .queryParam("page", 0)
                .queryParam("size", 100)
                .when()
                .get("/users/posts/most-liked")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(100));
    }

    @Test
    @Order(26)
    void testGetMostRecentPostsOfMostFollowedCommunities() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/most-popular/posts/most-recent")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(0));

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/communities/most-popular/posts/most-recent")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0));

        given()
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/communities/most-popular/posts/most-recent")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(5));

        given()
                .queryParam("page", 1000)
                .queryParam("size", 10)
                .when()
                .get("/communities/most-popular/posts/most-recent")
                .then()
                .statusCode(204);

        given()
                .queryParam("page", 0)
                .queryParam("size", 100)
                .when()
                .get("/communities/most-popular/posts/most-recent")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", lessThanOrEqualTo(100));
    }
}