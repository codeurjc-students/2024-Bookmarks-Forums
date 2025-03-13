package com.example.backend.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRunner {

    private CommentCreationDeletionTest commentCreationDeletionTest;
    private PostCreationDeletionTest postCreationDeletionTest;
    private PostEditTest postEditTest;
    private UserEditTest userEditTest;
    private CommunityFullTest communityFullTest;
    private LandingLoginTest landingLoginTest;
    private PostVisitTest postVisitTest;
    private SearchTest searchTest;
    private UserCreationDeletionTest userCreationDeletionTest;
    private AdminTest adminTest;
    private ChatTest chatTest;

    @BeforeEach
    public void setup() {
        commentCreationDeletionTest = new CommentCreationDeletionTest();
        communityFullTest = new CommunityFullTest();
        landingLoginTest = new LandingLoginTest();
        postCreationDeletionTest = new PostCreationDeletionTest();
        postEditTest = new PostEditTest();
        postVisitTest = new PostVisitTest();
        searchTest = new SearchTest();
        userCreationDeletionTest = new UserCreationDeletionTest();
        userEditTest = new UserEditTest();
        adminTest = new AdminTest();
        chatTest = new ChatTest();
    }

    @AfterEach
    public void teardown() {
        commentCreationDeletionTest.teardown();
        communityFullTest.teardown();
        landingLoginTest.teardown();
        postCreationDeletionTest.teardown();
        postEditTest.teardown();
        postVisitTest.teardown();
        searchTest.teardown();
        userCreationDeletionTest.teardown();
        userEditTest.teardown();
        adminTest.teardown();
        chatTest.teardown();
    }

    @Test
    @Order(1)
    public void testCommentCreationDeletion() {
        commentCreationDeletionTest.setupTest();
        commentCreationDeletionTest.commentCreationDeletionTest();
        commentCreationDeletionTest.teardown();
    }

    @Test
    @Order(2)
    public void testCommunityFull() {
        communityFullTest.setupTest();
        communityFullTest.communityFullTest();
        communityFullTest.teardown();
    }

    @Test
    @Order(3)
    public void testLandingLogin() {
        landingLoginTest.setupTest();
        landingLoginTest.landingLoginTest();
        landingLoginTest.teardown();
    }

    @Test
    @Order(4)
    public void testPostCreationDeletion() {
        postCreationDeletionTest.setupTest();
        postCreationDeletionTest.postCreationDeletionTest();
        postCreationDeletionTest.teardown();
    }

    @Test
    @Order(5)
    public void testPostEdit() {
        postEditTest.setupTest();
        postEditTest.postEditTest();
        postEditTest.teardown();
    }

    @Test
    @Order(6)
    public void testPostVisit() {
        postVisitTest.setupTest();
        postVisitTest.postVisitTest();
        postVisitTest.teardown();
    }

    @Test
    @Order(7)
    public void testSearch() {
        searchTest.setupTest();
        searchTest.searchTest();
        searchTest.teardown();
    }

    @Test
    @Order(8)
    public void testUserCreationDeletion() {
        userCreationDeletionTest.setupTest();
        userCreationDeletionTest.userCreationDeletionTest();
        userCreationDeletionTest.teardown();
    }

    @Test
    @Order(9)
    public void testUserEdit() {
        userEditTest.setupTest();
        userEditTest.userEditTest();
        userEditTest.teardown();
    }

    @Test
    @Order(10)
    public void testAdmin() {
        adminTest.setupTest();
        adminTest.adminTest();
        adminTest.teardown();
    }

    @Test
    @Order(11)
    public void testChat() {
        chatTest.setupTest();
        chatTest.chatTest();
        chatTest.teardown();
    }
}