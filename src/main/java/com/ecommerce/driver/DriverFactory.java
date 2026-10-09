package com.ecommerce.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.nio.file.Paths;
import java.util.UUID;

public class DriverFactory {
    public static WebDriver createDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (isHeadless()) {
                    options.addArguments("--headless=new");
                    options.addArguments("--window-size=1920,1080");
                    options.addArguments("--disable-dev-shm-usage");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-gpu");
                    options.addArguments("--no-first-run");
                    options.addArguments("--remote-allow-origins=*");
                    options.addArguments("--user-data-dir=" + temporaryChromeProfile());
                }
                return new ChromeDriver(options);
        }
    }

    private static boolean isHeadless() {
        String value = System.getProperty("headless");
        if (value == null || value.isBlank()) {
            value = System.getenv("HEADLESS");
        }
        return Boolean.parseBoolean(value);
    }

    private static String temporaryChromeProfile() {
        return Paths.get(System.getProperty("java.io.tmpdir"),
                "ecommerce-chrome-profile-" + UUID.randomUUID()).toString();
    }
}
