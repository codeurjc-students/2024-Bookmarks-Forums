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
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

class ChatTest {

    TestConfig config = TestConfig.getInstance();
    protected WebDriver driver;
    private final String LOCALHOST = config.getLocalhost();

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080"); // Set a standard window size
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void chatTest() {
        // Login as BookReader
        LoginAux loginAux = new LoginAux();
        loginAux.login(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Navigate to chat page
        int port = config.getPort();
        driver.get(LOCALHOST + ":" + port + "/chats");

        // Wait for the chat page to load

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wait.until(presenceOfElementLocated(By.className("chat-list-container")));
        wait.until(presenceOfElementLocated(By.className("chat-card")));

        // Test empty state message
        WebElement emptyStateMessage = wait.until(presenceOfElementLocated(By.className("no-chat-selected")));
        assertTrue(emptyStateMessage.getText().contains("Selecciona un chat de la izquierda"));

        // Test chat list loading
        List<WebElement> chatItems = driver.findElements(By.className("chat-item"));
        assertFalse(chatItems.isEmpty(), "Chat list should not be empty");

        // Test opening a chat
        WebElement firstChat = wait.until(elementToBeClickable(By.className("chat-item")));
        firstChat.click();

        // Wait for chat content to load

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wait.until(presenceOfElementLocated(By.className("messages-container")));
        wait.until(presenceOfElementLocated(By.className("message-input-container")));

        // Test sending a message
        WebElement messageInput = wait.until(presenceOfElementLocated(By.className("message-input-field")));
        String testMessage = "Hello, this is a test message!";
        messageInput.sendKeys(testMessage);

        WebElement sendButton = wait.until(elementToBeClickable(By.className("primary-btn")));
        sendButton.click();

        // Wait for message to appear in the chat

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wait.until(presenceOfElementLocated(By.className("message")));
        List<WebElement> messages = driver.findElements(By.className("message"));
        boolean messageFound = false;
        for (WebElement message : messages) {
            if (message.getText().contains(testMessage)) {
                messageFound = true;
                break;
            }
        }
        assertTrue(messageFound, "Sent message should appear in the chat");

        // Test chat header
        WebElement chatHeader = wait.until(presenceOfElementLocated(By.className("chat-header")));
        assertTrue(chatHeader.isDisplayed(), "Chat header should be visible");

        // Test profile picture and username in chat header
        WebElement profilePicture = wait.until(presenceOfElementLocated(By.className("chat-profile-picture")));
        assertTrue(profilePicture.isDisplayed(), "Profile picture should be visible");

        WebElement username = wait.until(presenceOfElementLocated(By.className("chat-username")));
        assertTrue(username.isDisplayed(), "Username should be visible");

        // Test message timestamp
        WebElement messageTimestamp = wait.until(presenceOfElementLocated(By.className("message-timestamp")));
        assertTrue(messageTimestamp.isDisplayed(), "Message timestamp should be visible");

        // Test input field is cleared after sending
        assertTrue(messageInput.getText().isEmpty(), "Message input should be cleared after sending");

        // Test chat list updates
        WebElement chatList = wait.until(presenceOfElementLocated(By.className("chat-list-container")));
        assertTrue(chatList.isDisplayed(), "Chat list should be visible");

        // Test active chat highlighting
        WebElement activeChat = wait.until(presenceOfElementLocated(By.cssSelector(".chat-item.active")));
        assertTrue(activeChat.isDisplayed(), "Active chat should be highlighted");

        // Logout
        loginAux.logoutFromChatPage(driver);
    }
} 