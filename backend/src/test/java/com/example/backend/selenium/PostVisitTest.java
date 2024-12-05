package com.example.backend.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class PostVisitTest {

    TestConfig config = TestConfig.getInstance();

    protected WebDriver driver;

    private final String LOCALHOST = config.getLocalhost();

    @BeforeEach
    public void setupTest() {
        driver = new ChromeDriver();
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void postVisitTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Testing on landing page

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/");

        // When

        // Locate Popular Users post list
        WebElement postsListColumn = wait.until(presenceOfElementLocated(By.className("col-lg-7")));
        // wait 5 seconds
        wait.until(elementToBeClickable(By.className("post-card")));
        List<WebElement> postsList = postsListColumn.findElements(By.className("post-card"));

        // Get first card title and content
        WebElement firstPost = postsList.getFirst();
        String firstPostTitle = firstPost.findElement(By.className("card-title")).getText();
        String firstPostContent = firstPost.findElement(By.className("card-post-content")).getText();
        String firstPostAuthor = firstPost.findElement(By.className("card-user-username")).getText();
        String firstPostDate = firstPost.findElement(By.className("card-post-date")).getText();
        String firstPostCommunity = firstPost.findElement(By.className("card-community-name-text")).getText();
        firstPost.click();

        //Then

        // Redirects to post page
        WebElement postTitle = wait.until(presenceOfElementLocated(By.className("card-title")));
        WebElement postContent = wait.until(presenceOfElementLocated(By.className("post-content")));
        WebElement postAuthor = wait.until(presenceOfElementLocated(By.id("post-author-username")));
        WebElement postDate = wait.until(presenceOfElementLocated(By.id("post-date")));
        WebElement postCommunity = wait.until(presenceOfElementLocated(By.className("branding-hyperlink-text")));
        // Compare
        await().atMost(Duration.ofSeconds(config.getWaitTime())).until(() -> true); // waits for posts to finish loading
        assertEquals(firstPostTitle, postTitle.getText());
        assertEquals(firstPostContent, postContent.getText());
        assertEquals(firstPostAuthor, postAuthor.getText());
        assertEquals(firstPostDate, postDate.getText());
        assertEquals(firstPostCommunity, postCommunity.getText());



    }
}
