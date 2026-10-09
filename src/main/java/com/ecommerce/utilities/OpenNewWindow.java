package com.ecommerce.utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class OpenNewWindow {

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Open the application URL.
            driver.get("https://qaplayground.com/practice/tabs-windows?utm_source=chatgpt.com");

            // Store the parent window handle.
            String parentWindowHandle = driver.getWindowHandle();

            // Locate the web elements and perform actions.
            WebElement elementTabA = wait.until(ExpectedConditions.elementToBeClickable(By.id("tw-tab-a")));
            elementTabA.click();

            WebElement elementTabB = wait.until(ExpectedConditions.elementToBeClickable(By.id("tw-tab-b")));
            elementTabB.click();

            WebElement elementTabC = wait.until(ExpectedConditions.elementToBeClickable(By.id("tw-tab-c")));
            elementTabC.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(4));

            // Get all child windows and switch to the one with the expected title.
            Set<String> windowHandles = driver.getWindowHandles();
            for (String windowHandle : windowHandles) {
                if (windowHandle.equals(parentWindowHandle)) {
                    continue;
                }

                driver.switchTo().window(windowHandle);
                if (driver.getTitle().contains("Practice Elements")) {
                    System.out.println(driver.getCurrentUrl());
                    break;
                }
            }

            driver.switchTo().window(parentWindowHandle);
        } finally {
            driver.quit();
        }
    }
}
