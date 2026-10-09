package com.ecommerce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class BasePage {
    protected final WebDriver driver;
    private final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void waitForTitleContains(String text) {
        wait.until(ExpectedConditions.titleContains(text));
    }

    protected void enterText(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        WebElement el = driver.findElement(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        driver.findElement(locator).click();
    }

    protected String getText(By locator) {
        return driver.findElement(locator).getText();
    }
}
