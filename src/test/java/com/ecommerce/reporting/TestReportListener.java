package com.ecommerce.reporting;

import com.ecommerce.base.BaseTest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TestReportListener implements ISuiteListener, IInvokedMethodListener, ITestListener {
    private static final Path REPORT_PATH = Paths.get("target", "test-reports", "test-report.html");
    private static final Path PDF_REPORT_PATH = Paths.get("target", "test-reports", "test-report.pdf");
    private static final String SUITE_NAME = "regression testing";
    private final ExtentReportManager extentReportManager = ExtentReportManager.getInstance();

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod() && testResult.getInstance() instanceof BaseTest baseTest) {
            baseTest.captureScreenshot(testResult, "home-page");
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        result.setAttribute("testStartedAt", Instant.now().toString());
        extentReportManager.startTest(result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        result.setAttribute("testStatus", "PASS");
        extentReportManager.pass(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        result.setAttribute("testStatus", "FAIL");
        extentReportManager.fail(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        result.setAttribute("testStatus", "SKIP");
        extentReportManager.skip(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        result.setAttribute("testStatus", "FAIL");
        extentReportManager.fail(result);
    }

    @Override
    public void onStart(ITestContext context) {
        // Test context initialization is handled by TestNG.
    }

    @Override
    public void onFinish(ITestContext context) {
        // Report generation is handled once per suite in ISuiteListener.onFinish.
    }

    @Override
    public void onFinish(ISuite suite) {
        extentReportManager.flush();
        StringBuilder report = new StringBuilder();
        report.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Test Report</title><style>")
                .append("body{font-family:Arial,sans-serif;margin:24px;color:#222}")
                .append("table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left;vertical-align:top}")
                .append("th{background:#f2f2f2}.PASS{color:#187a18}.FAIL{color:#b00020}.SKIP{color:#996c00}")
                .append("pre{white-space:pre-wrap;margin:0}</style></head><body><h1>Test Execution Report</h1><p>Generated: ")
                .append(escape(Instant.now().toString()))
                .append("</p><table><thead><tr><th>Suite</th><th>Test case</th><th>Parameters</th>")
                .append("<th>Status</th><th>Duration (ms)</th><th>Screenshots</th><th>Details</th></tr></thead><tbody>");

        List<ITestContext> contexts = suite.getResults().values().stream()
                .map(result -> result.getTestContext()).toList();
        for (ITestContext context : contexts) {
            appendResults(report, SUITE_NAME, context.getPassedTests(), "PASS");
            appendResults(report, SUITE_NAME, context.getFailedTests(), "FAIL");
            appendResults(report, SUITE_NAME, context.getSkippedTests(), "SKIP");
        }

        report.append("</tbody></table></body></html>");
        try {
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(REPORT_PATH, report.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write test report: " + REPORT_PATH, exception);
        }
        writePdfReport(contexts);
    }

    private void appendResults(StringBuilder report, String suiteName, IResultMap results, String status) {
        results.getAllResults().stream()
                .sorted(Comparator.comparingLong(ITestResult::getStartMillis))
                .forEach(result -> appendResult(report, suiteName, result, status));
    }

    private void appendResult(StringBuilder report, String suiteName, ITestResult result, String status) {
        String details = result.getThrowable() == null ? "" : result.getThrowable().toString();
        long duration = result.getEndMillis() - result.getStartMillis();
        String screenshot = screenshotLink(result);
        report.append("<tr><td>").append(escape(suiteName))
                .append("</td><td>").append(escape(displayTestName(result)))
                .append("</td><td>").append(escape(formatParameters(result.getParameters())))
                .append("</td><td class='").append(status).append("'>").append(status)
                .append("</td><td>").append(duration)
                .append("</td><td>").append(screenshot)
                .append("</td><td><pre>").append(escape(details))
                .append("</pre></td></tr>");
    }

    private String screenshotLink(ITestResult result) {
        String loginPage = screenshotLink(result, "login-page", "Login page");
        String passwordPage = screenshotLink(result, "login-page-with-password", "Login page with password");
        String homePage = screenshotLink(result, "home-page", "Home page");
        return loginPage + "<br>" + passwordPage + "<br>" + homePage;
    }

    private String screenshotLink(ITestResult result, String phase, String label) {
        Object screenshotPath = result.getAttribute("screenshotPath_" + phase);
        if (screenshotPath == null) {
            Object screenshotError = result.getAttribute("screenshotError_" + phase);
            return screenshotError == null ? label + ": Not available" : label + ": Capture failed: " + escape(screenshotError.toString());
        }
        Path path = Paths.get(screenshotPath.toString()).toAbsolutePath();
        Path reportDirectory = REPORT_PATH.toAbsolutePath().getParent();
        String href = reportDirectory.relativize(path).toString().replace('\\', '/');
        String metadata = result.getAttribute("screenshotTimestamp_" + phase) + ", "
            + result.getAttribute("screenshotWidth_" + phase) + "x"
            + result.getAttribute("screenshotHeight_" + phase)
            + (Boolean.TRUE.equals(result.getAttribute("screenshotFullPage_" + phase)) ? ", full screen" : ", viewport");
        return "<a href='" + escape(href) + "' target='_blank'>" + label + " screenshot</a><br><small>"
            + escape(metadata) + "</small>";
    }

    private String formatParameters(Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return "";
        }
        return Arrays.stream(parameters)
                .map(parameter -> parameter == null ? "null" : maskSecret(parameter.toString()))
                .toList()
                .toString();
    }

    private String maskSecret(String value) {
        if (value.toLowerCase().contains("password") || value.equals("secret_sauce")) {
            return "********";
        }
        return value;
    }

    private void writePdfReport(List<ITestContext> contexts) {
        try (PDDocument document = new PDDocument()) {
            for (ITestContext context : contexts) {
                appendPdfResults(document, context.getPassedTests(), "PASS");
                appendPdfResults(document, context.getFailedTests(), "FAIL");
                appendPdfResults(document, context.getSkippedTests(), "SKIP");
            }
            Files.createDirectories(PDF_REPORT_PATH.getParent());
            document.save(PDF_REPORT_PATH.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write PDF test report: " + PDF_REPORT_PATH, exception);
        }
    }

    private void appendPdfResults(PDDocument document, IResultMap results, String status) throws IOException {
        for (ITestResult result : results.getAllResults().stream()
                .sorted(Comparator.comparingLong(ITestResult::getStartMillis)).toList()) {
            drawScreenshotPage(document, result, status, "login-page", "Login page");
            drawScreenshotPage(document, result, status, "login-page-with-password", "Login page with password");
            drawScreenshotPage(document, result, status, "home-page", "Home page");
        }
    }

    private void drawScreenshotPage(PDDocument document, ITestResult result, String status,
                                    String phase, String label) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        Object pathAttribute = result.getAttribute("screenshotPath_" + phase);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            content.newLineAtOffset(40, 770);
            content.showText("Regression Testing - " + displayTestName(result));
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
            content.newLineAtOffset(0, -22);
            content.showText(label + " | Status: " + status);
            content.newLineAtOffset(0, -18);
            content.showText(screenshotMetadata(result, phase));
            content.endText();

            if (pathAttribute != null && Files.exists(Paths.get(pathAttribute.toString()))) {
                PDImageXObject image = PDImageXObject.createFromFile(pathAttribute.toString(), document);
                float scale = Math.min(515f / image.getWidth(), 675f / image.getHeight());
                float imageWidth = image.getWidth() * scale;
                float imageHeight = image.getHeight() * scale;
                content.drawImage(image, (PDRectangle.A4.getWidth() - imageWidth) / 2, 55, imageWidth, imageHeight);
            }
        }
    }

    private String screenshotMetadata(ITestResult result, String phase) {
        return "Captured: " + result.getAttribute("screenshotTimestamp_" + phase)
                + " | Size: " + result.getAttribute("screenshotWidth_" + phase) + "x"
                + result.getAttribute("screenshotHeight_" + phase)
                + " | Mode: " + (Boolean.TRUE.equals(result.getAttribute("screenshotFullPage_" + phase))
                ? "full screen" : "viewport");
    }

    private String displayTestName(ITestResult result) {
        if ("loginWithValidCredentials".equals(result.getMethod().getMethodName())) {
            return "loginwithvalid credentials";
        }
        return result.getMethod().getMethodName();
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}