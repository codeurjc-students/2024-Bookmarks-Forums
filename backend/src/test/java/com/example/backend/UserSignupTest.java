package com.example.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class UserSignupTest {

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
    void test() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // Given
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/signup");

        // When

        String username = "testingUser";
        String password = "Abcd!1234";
        String email = "testingEmail@address.com";
        String alias = "testingAlias";

        WebElement usernameField = wait.until(presenceOfElementLocated(By.id("inputUsername")));

        usernameField.sendKeys(username);
        driver.findElement(By.id("inputPassword")).sendKeys(password);
        driver.findElement(By.id("inputConfirmPassword")).sendKeys(password);
        driver.findElement(By.id("inputEmail")).sendKeys(email);
        driver.findElement(By.id("inputAlias")).sendKeys(alias);

        //wait until submit is clickable

        // Espera a que el botón de envío sea clicable
        WebElement submitButton = wait.until(elementToBeClickable(By.id("register-submit")));
        submitButton.click();

        // Auto-redirects to login page

        WebElement loginGreeting = wait.until(presenceOfElementLocated(By.id("login-greeting-text")));

        if (driver.getCurrentUrl().equals(LOCALHOST + ":" + port + "/login") && loginGreeting.isDisplayed()) {
            WebElement usernameField2 = wait.until(presenceOfElementLocated(By.id("username-field")));
            usernameField2.sendKeys(username);
            driver.findElement(By.id("password-field")).sendKeys(password);

            WebElement submitButton2 = wait.until(elementToBeClickable(By.id("login-submit")));
            submitButton2.click();
        }

        // Then (logout)

        WebElement landingGreeting = wait.until(presenceOfElementLocated(By.id("landing-greeting")));

        assertEquals("URL should be the landing page", LOCALHOST + ":" + port + "/", driver.getCurrentUrl());
        assertTrue(landingGreeting.isDisplayed(), "Landing greeting should be displayed");
        assertTrue(landingGreeting.getText().contains("Muy buenas, " + username), "Landing greeting should contain alias");

        driver.findElement(By.id("navbarDropdown")).click();
        driver.findElement(By.id("logout-navbar-btn")).click();

        WebElement defaultGreeting = wait.until(presenceOfElementLocated(By.id("default-greeting")));

        assertEquals("URL should be the landing page", LOCALHOST + ":" + port + "/", driver.getCurrentUrl());
        assertTrue(defaultGreeting.isDisplayed(), "Landing greeting should be displayed");

    }
}
