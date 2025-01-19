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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class UserCreationDeletionTest {

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
    void userCreationDeletionTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

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

        // wait until submit is clickable

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

        // Then (check logged in)

        WebElement landingGreeting = wait.until(presenceOfElementLocated(By.id("landing-greeting")));

        assertEquals(LOCALHOST + ":" + port + "/", driver.getCurrentUrl(), "URL should be the landing page");
        assertTrue(landingGreeting.isDisplayed(), "Landing greeting should be displayed");
        assertTrue(landingGreeting.getText().contains("Muy buenas, " + username),
                "Landing greeting should contain alias");

        driver.findElement(By.id("navbarDropdown")).click();
        driver.findElement(By.id("my-profile-dropdown-btn")).click();

        WebElement deleteAccountButton = wait.until(presenceOfElementLocated(By.id("delete-account-btn")));
        deleteAccountButton.click();

        // Wait for 3 seconds
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement confirmButton = wait.until(presenceOfElementLocated(By.xpath("//button[@title='Confirmar']")));
        confirmButton.click();

        // Go to profile page (check that it redirects to the no user found error page)
        driver.get(LOCALHOST + ":" + port + "/profile/" + username);

        WebElement errorTitle = wait.until(presenceOfElementLocated(By.className("error-title")));
        assertEquals("Usuario no encontrado: 404", errorTitle.getText());

    }
}
