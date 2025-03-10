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
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class SearchTest {

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
    void searchTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Go to Search Page

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/");

        // When

        WebElement exploreButton = wait.until(presenceOfElementLocated(By.id("explore-nav-btn")));
        exploreButton.click();

        // Then

        WebElement searchPageTitle = wait.until(presenceOfElementLocated(By.className("page-title")));

        // Wait for 8 seconds
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("Aquí tienes todo Bookmarks Forums", searchPageTitle.getText());

        WebElement searchInput = wait.until(presenceOfElementLocated(By.id("searchTerm")));

        // Search for post
        String searchTerm = "welcome";
        searchInput.sendKeys(searchTerm);
        searchInput.sendKeys(Keys.ENTER);

        WebElement searchResult = wait.until(presenceOfElementLocated(By.className("post-card")));
        WebElement postTitle = searchResult.findElement(By.className("card-title"));
        WebElement postContent = searchResult.findElement(By.className("card-post-content"));

        // Check that either the postTitle or postContent contains the word "welcome"
        boolean titleContains = postTitle.getText().toLowerCase().contains("welcome");
        boolean contentContains = postContent.getText().toLowerCase().contains("welcome");
        assertTrue(titleContains || contentContains);

        // Clear search
        WebElement clearButton = wait.until(presenceOfElementLocated(By.className("clear-icon-button")));
        clearButton.click();
        assertEquals("", searchInput.getAttribute("value"));

        // Search for community

        // Given

        String communitySearchTerm = "forums";
        searchInput.sendKeys(communitySearchTerm);

        // When

        WebElement searchButton = wait.until(presenceOfElementLocated(By.className("search-icon-button")));
        searchButton.click();

        // Then

        WebElement communitySearchResult = wait.until(presenceOfElementLocated(By.className("community-card")));
        WebElement communityTitle = communitySearchResult.findElement(By.className("card-text"));
        assertTrue(communityTitle.getText().toLowerCase().contains(communitySearchTerm));

        // Clear search
        clearButton.click();
        assertEquals("", searchInput.getAttribute("value"));

        // Search for user

        // Given

        String userSearchTerm = "admin";
        searchInput.sendKeys(userSearchTerm);

        // When

        searchButton.click();

        // Then

        WebElement userSearchResult = wait.until(presenceOfElementLocated(By.className("community-member-card")));
        WebElement userName = userSearchResult.findElement(By.className("community-member-username"));
        assertTrue(userName.getText().toLowerCase().contains(userSearchTerm));

        // Clear search
        clearButton.click();
        assertEquals("", searchInput.getAttribute("value"));

        // Search for non-existent term

        // Given

        String nonExistentSearchTerm = "nonexistent";
        searchInput.sendKeys(nonExistentSearchTerm);

        // When

        searchButton.click();

        // Then (there should be no post, community or member card elements)

        List<WebElement> postCards = driver.findElements(By.className("post-card"));
        List<WebElement> communityCards = driver.findElements(By.className("community-card"));
        List<WebElement> memberCards = driver.findElements(By.className("community-member-card"));

        assertTrue(postCards.isEmpty());
        assertTrue(communityCards.isEmpty());
        assertTrue(memberCards.isEmpty());

        // Clear search
        clearButton.click();
        assertEquals("", searchInput.getAttribute("value"));

        // Search for community and click on it

        // Given

        String communitySearchTerm2 = "forums";
        searchInput.sendKeys(communitySearchTerm2);

        // When

        searchButton.click();

        // Then

        WebElement communitySearchResult2 = wait.until(presenceOfElementLocated(By.className("community-card")));
        WebElement communityTitle2 = communitySearchResult2.findElement(By.className("card-text"));
        String communityName2 = communityTitle2.getText();
        assertTrue(communityTitle2.getText().toLowerCase().contains(communitySearchTerm2));

        communitySearchResult2.click();

        // Then (check that the community name is displayed on the community page)
        WebElement community = wait.until(presenceOfElementLocated(By.id("community-name-text")));
        wait.until((ExpectedCondition<Boolean>) driver -> !community.getText().isEmpty());
        await().atMost(Duration.ofSeconds(config.getWaitTime())).until(() -> true);
        String communityName = community.getText();

        assertEquals(communityName2, communityName);

    }
}
