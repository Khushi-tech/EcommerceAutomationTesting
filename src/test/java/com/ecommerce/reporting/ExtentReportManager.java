package com.ecommerce.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ExtentReportManager {
    private static final Path REPORT_PATH = Paths.get("target", "test-reports", "extent-report.html");
    private static final ExtentReportManager INSTANCE = new ExtentReportManager();

    private final ExtentReports extentReports;
    private final Map<String, ExtentTest> tests = new ConcurrentHashMap<>();

    private ExtentReportManager() {
        try {
            Files.createDirectories(REPORT_PATH.getParent());
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_PATH.toString());
            sparkReporter.config().setDocumentTitle("Ecommerce Automation Report");
            sparkReporter.config().setReportName("Login Test Execution Report");
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("Suite", "regression testing");
            extentReports.setSystemInfo("Report type", "Consolidated Login Test");
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize Extent report: " + REPORT_PATH, exception);
        }
    }

    public static ExtentReportManager getInstance() {
        return INSTANCE;
    }

    public void startTest(String testName) {
        tests.put(testName, extentReports.createTest(testName));
    }

    public void pass(ITestResult result) {
        update(result, Status.PASS);
    }

    public void fail(ITestResult result) {
        update(result, Status.FAIL);
    }

    public void skip(ITestResult result) {
        update(result, Status.SKIP);
    }

    public void flush() {
        extentReports.flush();
    }

    private void update(ITestResult result, Status status) {
        String testName = result.getMethod().getMethodName();
        ExtentTest test = tests.computeIfAbsent(testName, extentReports::createTest);
        String details = result.getThrowable() == null ? "Test completed" : result.getThrowable().toString();
        test.log(status, details);
        attachScreenshots(test, result);
    }

    private void attachScreenshots(ExtentTest test, ITestResult result) {
        attachScreenshot(test, result, "login-page", "Login page");
        attachScreenshot(test, result, "login-page-with-password", "Login page with password");
        attachScreenshot(test, result, "home-page", "Home page");
    }

    private void attachScreenshot(ExtentTest test, ITestResult result, String phase, String label) {
        Object screenshotPath = result.getAttribute("screenshotPath_" + phase);
        if (screenshotPath == null) {
            return;
        }
        test.addScreenCaptureFromPath(screenshotPath.toString(), label);
    }
}
