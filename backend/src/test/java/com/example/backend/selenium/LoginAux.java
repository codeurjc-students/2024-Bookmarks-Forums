package com.example.backend.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

class LoginAux {

    TestConfig config = TestConfig.getInstance();

    private final String LOCALHOST = config.getLocalhost();

    // This is run by other classes. It expects the backend to be already started
    // and WITH THE SAMPLE DATA
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

        // Waits for 3 seconds to allow the page to load

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement landingGreeting = wait.until(presenceOfElementLocated(By.id("landing-greeting")));

        assertEquals(LOCALHOST + ":" + port + "/", driver.getCurrentUrl(), "URL should be the landing page");
        assertTrue(landingGreeting.isDisplayed(), "Landing greeting should be displayed");

        // Wait for 3 seconds to allow the username to be displayed
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(landingGreeting.getText().contains("Muy buenas, " + username),
                "Landing greeting should contain alias");

    }

    void logout(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        driver.findElement(By.id("navbarDropdown")).click();
        driver.findElement(By.id("logout-navbar-btn")).click();

        // Check that navbar has login button
        WebElement loginButton = wait.until(presenceOfElementLocated(By.id("login-nav-btn")));
        assertTrue(loginButton.isDisplayed(), "Login button should be displayed");
    }

    void loginAsAdmin(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        driver.get(LOCALHOST + ":" + config.getPort() + "/login");

        WebElement loginGreeting = wait.until(presenceOfElementLocated(By.id("login-greeting-text")));

        if (driver.getCurrentUrl().equals(LOCALHOST + ":" + config.getPort() + "/login")
                && loginGreeting.isDisplayed()) {
            WebElement usernameField = wait.until(presenceOfElementLocated(By.id("username-field")));
            usernameField.sendKeys("AdminReader");
            driver.findElement(By.id("password-field")).sendKeys("adminpass");

            WebElement submitButton = wait.until(elementToBeClickable(By.id("login-submit")));
            submitButton.click();
        }

        // Wait for login and redirect
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement landingGreeting = wait.until(presenceOfElementLocated(By.id("landing-greeting")));
        assertEquals(LOCALHOST + ":" + config.getPort() + "/", driver.getCurrentUrl(),
                "URL should be the landing page");
        assertTrue(landingGreeting.isDisplayed(), "Landing greeting should be displayed");

        // Wait for 3 seconds to allow the username to be displayed
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(landingGreeting.getText().contains("Muy buenas, AdminReader"),
                "Landing greeting should contain admin");
    }

    void logoutFromAdminPage(WebDriver driver) { // This is used to logout from the admin page. Behaves just like
                                                 // logout, but it redirects to the login page, no navbar is present

        driver.findElement(By.id("navbarDropdown")).click();
        driver.findElement(By.id("logout-navbar-btn")).click();

        // Check that we are redirected to the login page by checking that the URL is
        // the login page

        // wait for the page to load
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(LOCALHOST + ":" + config.getPort() + "/login", driver.getCurrentUrl(),
                "URL should be the login page");
    }

    void logoutFromChatPage(WebDriver driver) {
        driver.findElement(By.id("navbarDropdown")).click();
        driver.findElement(By.id("logout-navbar-btn")).click();

        // Check that we are redirected to the login page
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(LOCALHOST + ":" + config.getPort() + "/login", driver.getCurrentUrl(),
                "URL should be the login page");
    }
}
