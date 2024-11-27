package com.example.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static junit.framework.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class UserEditTest {

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
    void userEditTest() {

        // Login
        LoginAux loginAux = new LoginAux();
        loginAux.login(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));


        // Testing on first post page

        String newAlias = "newTestingAlias";
        String newDescription = "newTestingDescription";

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/profile/" + config.getUsername());


        // When

        WebElement editProfileButton = wait.until(presenceOfElementLocated(By.id("edit-profile-btn")));
        editProfileButton.click();

        WebElement aliasField = wait.until(presenceOfElementLocated(By.id("alias-input")));
        WebElement descriptionField = wait.until(presenceOfElementLocated(By.id("description-input")));
        WebElement submitButton = wait.until(presenceOfElementLocated(By.id("profile-edit-confirm-btn")));
        aliasField.clear();
        aliasField.sendKeys(newAlias);
        descriptionField.clear();
        descriptionField.sendKeys(newDescription);
        submitButton.click();

        // Then (goes to profile page and compares)
        WebElement aliasText = wait.until(presenceOfElementLocated(By.id("alias-text")));
        WebElement descriptionText = wait.until(presenceOfElementLocated(By.id("description-text")));

        // Wait until aliasText has non-empty text
        wait.until((ExpectedCondition<Boolean>) driver -> !aliasText.getText().isEmpty());

        // Wait until descriptionText has non-empty innerHTML
        wait.until((ExpectedCondition<Boolean>) driver -> !descriptionText.getAttribute("innerHTML").isEmpty());

        // Validate alias and description
        assertEquals("(" + newAlias + ")", aliasText.getText()); // The alias has () add in the profile page
        assertEquals(newDescription, descriptionText.getAttribute("innerHTML"));

        loginAux.logout(driver);

    }
}
