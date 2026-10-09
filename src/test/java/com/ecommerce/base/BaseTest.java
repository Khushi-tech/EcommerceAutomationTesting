package com.ecommerce.base;

import com.ecommerce.constant.Constants;
import com.ecommerce.driver.DriverFactory;
import com.ecommerce.utilities.Log;
import com.ecommerce.utilities.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import java.io.InputStream;
import java.util.Properties;

public class BaseTest {
    protected WebDriver driver;
    protected Properties props;

    @BeforeMethod
    public void setUp() throws Exception{
        Log.info("Setting up the test environment");
        props = new Properties();
        //Find config.properties from the test resources → open it → read it into the Properties object.
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(Constants.CONFIG_FILE_PATH)) {
            if (input == null) {
                throw new IllegalStateException(Constants.CONFIG_FILE_PATH + " not found in test resources");
            }
            props.load(input);//browser=chrome
             //baseUrl=https://www.saucedemo.com/
        }

        props.setProperty("validUsername", requiredSecret("TEST_USERNAME", "test.username"));
        props.setProperty("validPassword", requiredSecret("TEST_PASSWORD", "test.password"));

        driver = DriverFactory.createDriver(props.getProperty("browser", Constants.DEFAULT_BROWSER));
        driver.manage().window().maximize();
        driver.get(props.getProperty("baseUrl", Constants.DEFAULT_BASE_URL));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void captureScreenshot(String phase) {
        captureScreenshot(Reporter.getCurrentTestResult(), phase);
    }

    public void captureScreenshot(ITestResult testResult, String phase) {
        if (driver == null) {
            return;
        }
        try {
                ScreenshotUtils.CaptureResult capture = ScreenshotUtils.capture(driver,
                    testResult.getMethod().getQualifiedName() + "_" + phase);
                testResult.setAttribute("screenshotPath_" + phase, capture.path());
                testResult.setAttribute("screenshotTimestamp_" + phase, capture.timestamp());
                testResult.setAttribute("screenshotWidth_" + phase, capture.width());
                testResult.setAttribute("screenshotHeight_" + phase, capture.height());
                testResult.setAttribute("screenshotFullPage_" + phase, capture.fullPage());
        } catch (RuntimeException exception) {
            testResult.setAttribute("screenshotError_" + phase, exception.getMessage());
        }
    }

    private String requiredSecret(String environmentVariable, String systemProperty) {
        String value = getFirstNonBlank(
                System.getenv(environmentVariable),
                System.getProperty(systemProperty),
                defaultCredentialValue(environmentVariable));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing credential. Set " + environmentVariable
                    + " or the -D" + systemProperty + " system property.");
        }
        return value;
    }

    private String defaultCredentialValue(String environmentVariable) {
        if ("TEST_USERNAME".equals(environmentVariable)) {
            return "standard_user";
        }
        if ("TEST_PASSWORD".equals(environmentVariable)) {
            return "secret_sauce";
        }
        return null;
    }

    private String getFirstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
