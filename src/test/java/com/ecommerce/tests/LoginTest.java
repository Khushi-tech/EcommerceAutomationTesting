package com.ecommerce.tests;

import com.ecommerce.base.BaseTest;
import com.ecommerce.pages.LoginPage;
import com.ecommerce.constant.Constants;
import com.ecommerce.utilities.CsvTestDataReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public class LoginTest extends BaseTest {
    private static final String NEGATIVE_LOGIN_DATA_FILE = "testdata/negative_login_test_data.csv";

    @DataProvider(name = "negativeLoginData")
    public Object[][] negativeLoginData() throws IOException {
        return CsvTestDataReader.read(NEGATIVE_LOGIN_DATA_FILE);
    }

    @Test
    public void loginWithValidCredentials() {
        LoginPage loginPage = new LoginPage(driver);
        captureScreenshot("login-page");
        loginPage.enterCredentials(props.getProperty("validUsername"), props.getProperty("validPassword"));
        captureScreenshot("login-page-with-password");
        loginPage.submitLogin();
        Assert.assertTrue(driver.getCurrentUrl().contains(Constants.INVENTORY_URL_FRAGMENT));
    }

    @Test(dataProvider = "negativeLoginData")
    public void loginWithInvalidCredentials(String scenario, String username, String password, String expectedError) {
        LoginPage loginPage = new LoginPage(driver);
        captureScreenshot("login-page");
        loginPage.enterCredentials(username, password);
        captureScreenshot("login-page-with-password");
        loginPage.submitLogin();

        Assert.assertFalse(driver.getCurrentUrl().contains(Constants.INVENTORY_URL_FRAGMENT),
                "Login should fail for scenario: " + scenario);
        Assert.assertEquals(loginPage.getErrorMessage(), expectedError,
                "Unexpected error message for scenario: " + scenario);
    }
}
