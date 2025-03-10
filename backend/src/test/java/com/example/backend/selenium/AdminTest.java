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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class AdminTest {

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
    void adminTest() {
        // Login as admin
        LoginAux loginAux = new LoginAux();
        loginAux.loginAsAdmin(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Navigate to admin page
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/admin");

        // Wait for the page to load and verify admin elements are present
        wait.until(presenceOfElementLocated(By.id("mostBannedUsersChart")));
        wait.until(presenceOfElementLocated(By.id("mostDislikedUsersChart")));
        wait.until(presenceOfElementLocated(By.className("admin-user-card")));

        // Test search functionality
        WebElement searchInput = wait.until(presenceOfElementLocated(By.className("search-input")));
        searchInput.sendKeys("BookReader");
        WebElement searchButton = wait.until(presenceOfElementLocated(By.className("search-btn")));
        searchButton.click();

        // Wait for search results
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify search results contain BookReader
        WebElement userCard = wait.until(presenceOfElementLocated(By.className("admin-user-card")));
        assertTrue(userCard.getText().contains("BookReader"));

        // Test clear search
        WebElement clearSearchButton = wait.until(presenceOfElementLocated(By.className("clear-search-btn")));
        clearSearchButton.click();

        // Wait for results to clear
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that disable button is disabled and has correct title for admin users
        List<WebElement> userCards = driver.findElements(By.className("admin-user-card"));
        for (WebElement card : userCards) {
            if (card.getText().contains("AdminReader")) {
                WebElement disableButton = card.findElement(By.className("negative-btn"));
                assertTrue(disableButton.isEnabled() == false, "Disable button should be disabled for admin users");
                assertEquals("No puedes deshabilitar cuentas de administradores del sitio", 
                    disableButton.getAttribute("title"), 
                    "Disable button should show correct title for admin users");
            }
        }

        // Verify that disable button has correct title for non-admin users
        for (WebElement card : userCards) {
            if (!card.getText().contains("AdminReader")) {
                WebElement disableButton = card.findElement(By.className("negative-btn"));
                assertEquals("Deshabilitar cuenta", 
                    disableButton.getAttribute("title"), 
                    "Disable button should show correct title for non-admin users");
            }
        }

        // Test user disable functionality for non-admin user
        WebElement disableButton = wait.until(presenceOfElementLocated(By.className("negative-btn")));
        disableButton.click();

        // Wait for modal to appear
        wait.until(presenceOfElementLocated(By.id("disableDurationModal")));

        // Select duration (1 day)
        WebElement durationSlider = wait.until(presenceOfElementLocated(By.id("durationSlider")));
        durationSlider.sendKeys("0"); // Set to first option (1 day)

        // Confirm disable action
        WebElement confirmButton = wait.until(presenceOfElementLocated(By.id("confirm-modal-btn")));
        confirmButton.click();

        // Wait for modal to disappear and verify user is disabled
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify user status shows as disabled
        WebElement userStatus = wait.until(presenceOfElementLocated(By.className("admin-user-status")));
        assertTrue(userStatus.getText().contains("Cuenta deshabilitada"));

        // Test user enable functionality
        WebElement enableButton = wait.until(presenceOfElementLocated(By.className("primary-btn")));
        enableButton.click();

        // Wait for confirmation modal
        wait.until(presenceOfElementLocated(By.className("alert-modal")));

        // Confirm enable action
        WebElement enableConfirmButton = wait.until(presenceOfElementLocated(By.id("confirm-modal-btn")));
        enableConfirmButton.click();

        // Wait for modal to disappear and verify user is enabled
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify user status shows as active
        WebElement activeStatus = wait.until(presenceOfElementLocated(By.className("admin-user-status")));
        assertTrue(activeStatus.getText().contains("Cuenta activa"));

        // Logout
        loginAux.logoutFromAdminPage(driver);
    }
} 