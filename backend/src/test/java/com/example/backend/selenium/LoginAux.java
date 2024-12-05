package com.example.backend.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class LoginAux {

    TestConfig config = TestConfig.getInstance();

    private final String LOCALHOST = config.getLocalhost();

    // This is run by other classes. It expects the backend to be already started and WITH THE SAMPLE DATA
    void login(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/login");

        // When

        String username = config.getUsername();
        String password = config.getPassword();

        // Auto-redirects to login page

        WebElement loginGreeting = wait.until(presenceOfElementLocated(By.id("login-greeting-text")));

        if (driver.getCurrentUrl().equals(LOCALHOST + ":" + port + "/login") && loginGreeting.isDisplayed()) {
            WebElement usernameField2 = wait.until(presenceOfElementLocated(By.id("username-field")));
            usernameField2.sendKeys(username);
            driver.findElement(By.id("password-field")).sendKeys(password);

            WebElement submitButton2 = wait.until(elementToBeClickable(By.id("login-submit")));
            submitButton2.click();
        }

        // Then

        WebElement landingGreeting = wait.until(presenceOfElementLocated(By.id("landing-greeting")));

        assertEquals(LOCALHOST + ":" + port + "/", driver.getCurrentUrl(), "URL should be the landing page");
        assertTrue(landingGreeting.isDisplayed(), "Landing greeting should be displayed");
        await().atMost(Duration.ofSeconds(5)).until(() -> true); // waits for username text to load
        assertTrue(landingGreeting.getText().contains("Muy buenas, " + username), "Landing greeting should contain alias");

    }

    void logout(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        driver.findElement(By.id("navbarDropdown")).click();
        driver.findElement(By.id("logout-navbar-btn")).click();

        // Check that navbar has login button
        WebElement loginButton = wait.until(presenceOfElementLocated(By.id("login-nav-btn")));
        assertTrue(loginButton.isDisplayed(), "Login button should be displayed");
    }
}
