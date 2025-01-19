package com.example.backend.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class PostCreationDeletionTest {

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
    void postCreationDeletionTest() {

        // Login
        LoginAux loginAux = new LoginAux();

        loginAux.login(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Post creation

        // Given

        String newTitle = "New Title";
        String newContent = "New Content";
        String username = config.getUsername();

        driver.get(LOCALHOST + ":" + config.getPort() + "/community/1");

        WebElement communityName = wait.until(presenceOfElementLocated(By.id("community-name-text")));
        wait.until((ExpectedCondition<Boolean>) driver -> !communityName.getText().isEmpty());
        String community = communityName.getText();

        WebElement createPostButton = wait.until(presenceOfElementLocated(By.id("community-new-post-btn")));
        createPostButton.click();

        // When

        WebElement postTitle = wait.until(presenceOfElementLocated(By.id("postTitle")));
        WebElement postContent = wait.until(presenceOfElementLocated(By.id("postContent")));
        WebElement submitButton = wait.until(presenceOfElementLocated(By.id("post-create-confirm-btn")));
        postTitle.sendKeys(newTitle);
        postContent.sendKeys(newContent);
        submitButton.click();

        // save current date and format as a string with this formatting: "DD-MM-YYYY a
        // las hh:mm"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy 'a las' HH:mm");
        String saveDate = LocalDateTime.now().format(formatter);

        // Then (post must show correct content)
        WebElement postTitle2 = wait.until(presenceOfElementLocated(By.className("card-title")));
        WebElement postContent2 = wait.until(presenceOfElementLocated(By.className("post-content")));
        WebElement postAuthor = wait.until(presenceOfElementLocated(By.id("post-author-username")));
        WebElement postDate = wait.until(presenceOfElementLocated(By.id("post-date")));
        WebElement postCommunity = wait.until(presenceOfElementLocated(By.className("branding-hyperlink-text")));

        // Wait 5 seconds for the post to be created
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Compare
        assertEquals(newTitle, postTitle2.getText());
        assertEquals(newContent, postContent2.getText());
        assertEquals(username, postAuthor.getText());
        assertEquals(saveDate, postDate.getText());
        assertEquals(community, postCommunity.getText());

        // Post deletion

        // Given
        String postId = driver.getCurrentUrl().substring(driver.getCurrentUrl().lastIndexOf("/") + 1);

        // When

        WebElement deletePostButton = wait.until(presenceOfElementLocated(By.id("delete-post-btn")));
        deletePostButton.click();

        // Wait for 3 seconds
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement deletePostConfirmButton = wait.until(presenceOfElementLocated(By.id("confirm-btn")));
        deletePostConfirmButton.click();

        // Then (go to post page and check that it redirects to the corresponding error
        // page)
        driver.get(LOCALHOST + ":" + config.getPort() + "/post/" + postId);
        WebElement errorText = wait.until(presenceOfElementLocated(By.className("error-text")));
        assertEquals("No se ha encontrado el post.", errorText.getText());

        loginAux.logout(driver);

    }
}
