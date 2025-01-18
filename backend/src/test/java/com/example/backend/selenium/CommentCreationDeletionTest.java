package com.example.backend.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class CommentCreationDeletionTest {

    TestConfig config = TestConfig.getInstance();

    protected WebDriver driver;

    private final String LOCALHOST = config.getLocalhost();

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void commentCreationDeletionTest() {

        // Login
        LoginAux loginAux = new LoginAux();
        loginAux.login(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Testing on post ID: 1

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/post/4");

        String postTitle = "testing comment title";
        String postContent = "testing comment content";

        // When

        WebElement titleField = wait.until(presenceOfElementLocated(By.className("reply-title-input")));
        WebElement contentField = wait.until(presenceOfElementLocated(By.className("reply-content-input")));
        WebElement submitButton = wait.until(presenceOfElementLocated(By.id("post-comment-btn")));

        // Then (writes review title and content)
        titleField.sendKeys(postTitle);
        contentField.sendKeys(postContent);
        submitButton.click();

        // Check on post page

        // Get comments list block
        WebElement commentsListContainer = wait.until(presenceOfElementLocated(By.className("replies-list")));
        // Get last commentCard

        // Wait for reply-card
        wait.until(presenceOfElementLocated(By.className("reply-card")));

        wait.until(driver -> commentsListContainer.findElements(By.className("reply-card")).size() == 2);

        WebElement commentCard = commentsListContainer.findElements(By.className("reply-card"))
                .get(commentsListContainer.findElements(By.className("reply-card")).size() - 1);

        // Compare card title and content
        WebElement commentTitle = commentCard.findElement(By.className("reply-card-title"));
        WebElement commentContent = commentCard.findElement(By.className("reply-card-content"));
        assertEquals(postTitle, commentTitle.getText());
        assertEquals(postContent, commentContent.getText());

        // Delete comment
        WebElement deleteButton = commentCard.findElement(By.className("delete-reply-btn"));
        deleteButton.click();

        // Check that there is no reply-card element anymore (no more comments text is
        // displayed)
        wait.until(presenceOfElementLocated(By.id("no-more-replies-text")));

        // Logout
        loginAux.logout(driver);

    }
}
