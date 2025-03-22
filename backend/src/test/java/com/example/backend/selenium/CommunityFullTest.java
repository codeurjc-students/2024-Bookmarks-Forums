package com.example.backend.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

class CommunityFullTest {

    TestConfig config = TestConfig.getInstance();

    protected WebDriver driver;
    protected JavascriptExecutor js;

    private final String LOCALHOST = config.getLocalhost();

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080"); // Set a standard window size
        driver = new ChromeDriver(options);
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void communityFullTest() {

        // Login
        LoginAux loginAux = new LoginAux();
        loginAux.login(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getWaitTime()));

        // Community creation

        // Given

        String newCommunityName = "New Community";
        String newCommunityDescription = "New Community Description";

        // When

        WebElement communityCreationButton = wait.until(presenceOfElementLocated(By.id("new-community-btn-link")));
        communityCreationButton.click();

        WebElement showDescriptionButton = wait
                .until(presenceOfElementLocated(By.className("hyperlink-btn-in-header")));
        showDescriptionButton.click();

        WebElement communityNameField = wait.until(presenceOfElementLocated(By.id("communityName")));
        WebElement communityDescriptionField = wait.until(presenceOfElementLocated(By.id("communityDescription")));

        communityNameField.clear();
        communityNameField.sendKeys(newCommunityName);
        communityDescriptionField.clear();
        communityDescriptionField.sendKeys(newCommunityDescription);

        WebElement submitButton = wait.until(presenceOfElementLocated(By.id("confirm-changes-btn")));
        submitButton.click();

        WebElement confirmButton = wait.until(presenceOfElementLocated(By.id("confirm-modal-btn")));
        wait.until((ExpectedCondition<Boolean>) driver -> confirmButton.isEnabled());
        wait.until(elementToBeClickable(By.id("confirm-modal-btn")));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Try regular click first, if it fails use JavaScript click
        try {
            confirmButton.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", confirmButton);
        }

        // Then (check that the community name and description are displayed on the
        // community page)
        WebElement community = wait.until(presenceOfElementLocated(By.id("community-name-text")));
        wait.until((ExpectedCondition<Boolean>) driver -> !community.getText().isEmpty());
        await().atMost(Duration.ofSeconds(config.getWaitTime())).until(() -> true);
        String communityName = community.getText();

        WebElement showDescriptionButton2 = wait
                .until(presenceOfElementLocated(By.className("hyperlink-btn-in-header")));
        showDescriptionButton2.click();

        WebElement communityDescription = wait.until(presenceOfElementLocated(By.id("community-description-text")));
        wait.until((ExpectedCondition<Boolean>) driver -> !communityDescription.getText().isEmpty());
        String communityDescriptionText = communityDescription.getText();

        assertEquals(newCommunityName, communityName);
        assertEquals(newCommunityDescription, communityDescriptionText);

        // Community modification

        // Given

        String newCommunityName2 = "New Community 2";
        String newCommunityDescription2 = "New Community Description 2";

        WebElement editCommunityButton = wait.until(presenceOfElementLocated(By.id("edit-community-btn")));
        editCommunityButton.click();

        // When

        WebElement communityNameField2 = wait.until(presenceOfElementLocated(By.id("communityName")));
        WebElement showDescriptionButton3 = wait
                .until(presenceOfElementLocated(By.className("hyperlink-btn-in-header")));
        showDescriptionButton3.click();
        WebElement communityDescriptionField2 = wait.until(presenceOfElementLocated(By.id("communityDescription")));
        WebElement submitButton2 = wait.until(presenceOfElementLocated(By.id("confirm-changes-btn")));

        communityNameField2.clear();
        communityNameField2.sendKeys(newCommunityName2);
        communityDescriptionField2.clear();
        communityDescriptionField2.sendKeys(newCommunityDescription2);
        submitButton2.click();

        WebElement confirmButton2 = wait.until(presenceOfElementLocated(By.id("confirm-modal-btn")));
        wait.until((ExpectedCondition<Boolean>) driver -> confirmButton2.isEnabled());
        wait.until(elementToBeClickable(By.id("confirm-modal-btn")));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Try regular click first, if it fails use JavaScript click
        try {
            confirmButton2.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", confirmButton2);
        }

        // Then (Check elements changed)

        WebElement community2 = wait.until(presenceOfElementLocated(By.id("community-name-text")));
        wait.until((ExpectedCondition<Boolean>) driver -> !community2.getText().isEmpty());
        String communityName2 = community2.getText();

        WebElement showDescriptionButton4 = wait
                .until(presenceOfElementLocated(By.className("hyperlink-btn-in-header")));
        showDescriptionButton4.click();

        WebElement communityDescription2 = wait.until(presenceOfElementLocated(By.id("community-description-text")));
        wait.until((ExpectedCondition<Boolean>) driver -> !communityDescription2.getText().isEmpty());
        String communityDescriptionText2 = communityDescription2.getText();

        assertEquals(newCommunityName2, communityName2);
        assertEquals(newCommunityDescription2, communityDescriptionText2);

        // Community deletion

        // Given
        // Wait for the delete button to be both present and clickable

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement deleteCommunityButton = wait.until(elementToBeClickable(By.id("delete-community-btn")));
        
        // Scroll the delete button into view
        js.executeScript("arguments[0].scrollIntoView(true);", deleteCommunityButton);
        
        // Wait a bit for any animations to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Try regular click first, if it fails use JavaScript click
        try {
            deleteCommunityButton.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", deleteCommunityButton);
        }

        // When

        WebElement confirmButton3 = wait.until(presenceOfElementLocated(By.id("confirm-modal-btn")));
        wait.until((ExpectedCondition<Boolean>) driver -> confirmButton3.isEnabled());
        await().atMost(Duration.ofSeconds(config.getWaitTime())).until(() -> true); // waits for modal animation to finish
        
        try {
            confirmButton3.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", confirmButton3);
        }

        // Then (community should no longer display its page)
        // Get community ID from URL
        String communityId = driver.getCurrentUrl().substring(driver.getCurrentUrl().lastIndexOf("/") + 1);
        driver.get(LOCALHOST + ":" + config.getPort() + "/community/" + communityId);
        WebElement errorText = wait.until(presenceOfElementLocated(By.className("error-title")));
        assertEquals("Comunidad no encontrada: 404", errorText.getText());

        loginAux.logout(driver);

    }
}
