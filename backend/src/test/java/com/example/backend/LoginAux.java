package com.example.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class LoginAux {

    TestConfig config = TestConfig.getInstance();

    private final String LOCALHOST = config.getLocalhost();

    // This is run by other classes. It expects the backend to be already started and WITH THE SAMPLE DATA
    void login(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));


        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/login");

        // When

        String username = "BookReader_14";
        String password = "pass";

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

        assertEquals("URL should be the landing page", LOCALHOST + ":" + port + "/", driver.getCurrentUrl());
        assertTrue(landingGreeting.isDisplayed(), "Landing greeting should be displayed");
        assertTrue(landingGreeting.getText().contains("Muy buenas, " + username), "Landing greeting should contain alias");

    }
}
