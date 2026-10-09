package com.ecommerce.tests;

import com.ecommerce.base.BaseTest;
import com.ecommerce.pages.LoginPage;
import com.ecommerce.constant.Constants;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class LoginDataDrivenTest extends BaseTest {
    private static final String DATA_FILE = "src/test/resources/testdata/login_test_data_generated.xls";
    private static final DataFormatter FORMATTER = new DataFormatter();

    @DataProvider(name = "loginData")
    public Object[][] loginData() throws IOException {
        try (FileInputStream fis = new FileInputStream(DATA_FILE);
             HSSFWorkbook workbook = new HSSFWorkbook(fis)) {
            HSSFSheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getLastRowNum();
            int cols = sheet.getRow(0).getLastCellNum();
            Object[][] data = new Object[rows][3];
            for (int i = 1; i <= rows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                data[i - 1][0] = resolveCredential(FORMATTER.formatCellValue(row.getCell(0)));
                data[i - 1][1] = resolveCredential(FORMATTER.formatCellValue(row.getCell(1)));
                data[i - 1][2] = FORMATTER.formatCellValue(row.getCell(2));
            }
            return data;
        }
    }

    private String resolveCredential(String value) {
        if ("${TEST_USERNAME}".equals(value)) {
            return requiredSecret("TEST_USERNAME", "test.username");
        }
        if ("${TEST_PASSWORD}".equals(value)) {
            return requiredSecret("TEST_PASSWORD", "test.password");
        }
        return value;
    }

    private String requiredSecret(String environmentVariable, String systemProperty) {
        String secret = System.getenv(environmentVariable);
        if (secret == null || secret.isBlank()) {
            secret = System.getProperty(systemProperty);
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Missing credential. Set " + environmentVariable
                    + " or the -D" + systemProperty + " system property.");
        }
        return secret;
    }

    @Test(dataProvider = "loginData")
    public void loginTest(String username, String password, String expected) {
        LoginPage loginPage = new LoginPage(driver);
        captureScreenshot("login-page");
        loginPage.enterCredentials(username, password);
        captureScreenshot("login-page-with-password");
        loginPage.submitLogin();

        boolean passed = driver.getCurrentUrl().contains(Constants.INVENTORY_URL_FRAGMENT);
        if ("PASS".equalsIgnoreCase(expected)) {
            Assert.assertTrue(passed, "Expected login to succeed for: " + username);
        } else {
            Assert.assertFalse(passed, "Expected login to fail for: " + username);
        }
    }
}
