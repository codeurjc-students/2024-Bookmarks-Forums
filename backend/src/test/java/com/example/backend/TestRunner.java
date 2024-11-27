package com.example.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

    }

    @Test
    public void testCommentCreationDeletion() {
        commentCreationDeletionTest.setupTest();
        commentCreationDeletionTest.commentCreationDeletionTest();
        commentCreationDeletionTest.teardown();
    }

    @Test
    public void testCommunityFull() {
        communityFullTest.setupTest();
        communityFullTest.communityFullTest();
        communityFullTest.teardown();
    }

    @Test
    public void testLandingLogin() {
        landingLoginTest.setupTest();
        landingLoginTest.landingLoginTest();
        landingLoginTest.teardown();
    }

    @Test
    public void testPostCreationDeletion() {
        postCreationDeletionTest.setupTest();
        postCreationDeletionTest.postCreationDeletionTest();
        postCreationDeletionTest.teardown();
    }

    @Test
    public void testPostEdit() {
        postEditTest.setupTest();
        postEditTest.postEditTest();
        postEditTest.teardown();
    }

    @Test
    public void testPostVisit() {
        postVisitTest.setupTest();
        postVisitTest.postVisitTest();
        postVisitTest.teardown();
    }

    @Test
    public void testSearch() {
        searchTest.setupTest();
        searchTest.searchTest();
        searchTest.teardown();
    }

    @Test
    public void testUserCreationDeletion() {
        userCreationDeletionTest.setupTest();
        userCreationDeletionTest.userCreationDeletionTest();
        userCreationDeletionTest.teardown();
    }

    @Test
    public void testUserEdit() {
        userEditTest.setupTest();
        userEditTest.userEditTest();
        userEditTest.teardown();
    }
}