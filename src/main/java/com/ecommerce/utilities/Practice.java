package com.ecommerce.utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Practice {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        driver.get("https://the-internet.herokuapp.com/login");

        WebElement userName = driver.findElement(By.xpath("//input[@id='username']"));
        userName.sendKeys("tomsmith");

        WebElement password = driver.findElement(By.xpath("//input[@id='password']"));
        password.sendKeys("SuperSecretPassword!");

        WebElement button = driver.findElement(By.xpath("//button[@type='submit']"));
        button.click();

        WebElement flashMessage = driver.findElement(By.xpath("//div[contains(text(),'You logged into a secure area!')]"));
        String actualMessage = flashMessage.getText();

        if (actualMessage.contains("You logged into a secure area")) {
            System.out.println("Login is successful");
        } else {
            System.out.println("Login is not successful");
        }

        driver.quit();
    }
}
