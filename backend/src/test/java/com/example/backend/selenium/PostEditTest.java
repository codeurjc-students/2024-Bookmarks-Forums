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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class PostEditTest {

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
    void postEditTest() {

        // Login
        LoginAux loginAux = new LoginAux();
        loginAux.login(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));


        // Testing on first post page

        String newTitle = "New Title";
        String newContent = "New Content";

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/post/2");


        // When

        WebElement postTitle = wait.until(presenceOfElementLocated(By.className("card-title")));
        WebElement postContent = wait.until(presenceOfElementLocated(By.className("post-content")));
        WebElement editButton = wait.until(presenceOfElementLocated(By.id("edit-post-btn")));

        String postTitleText = postTitle.getText();
        String postContentText = postContent.getText();
        editButton.click();

        // Then (goes to modify post)
        WebElement postTitle2 = wait.until(presenceOfElementLocated(By.id("postTitle")));
        WebElement postContent2 = wait.until(presenceOfElementLocated(By.id("postContent")));
        WebElement submitButton = wait.until(presenceOfElementLocated(By.id("post-edit-confirm-btn")));
        assertEquals(postTitleText, postTitle2.getAttribute("value"));
        assertEquals(postContentText, postContent2.getText());

        postTitle2.clear();
        postTitle2.sendKeys(newTitle);
        postContent2.clear();
        postContent2.sendKeys(newContent);
        submitButton.click();

        // Check on post page
        WebElement postTitle3 = wait.until(presenceOfElementLocated(By.className("card-title")));
        WebElement postContent3 = wait.until(presenceOfElementLocated(By.className("post-content")));
        assertEquals(newTitle, postTitle3.getText());
        assertEquals(newContent, postContent3.getText());

    }
}
