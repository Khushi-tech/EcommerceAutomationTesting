package com.ecommerce.utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;

import javax.imageio.ImageIO;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class ScreenshotUtils {
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final DateTimeFormatter DISPLAY_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z");

    public static CaptureResult capture(WebDriver driver, String testName) {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));

            LocalDateTime capturedAt = LocalDateTime.now();
            String timestamp = capturedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = safeTestName + "_" + timestamp + "_" + UUID.randomUUID() + ".png";
            File destination = new File(SCREENSHOT_DIR, fileName);

            focusBrowserWindow(driver);
            BufferedImage image = captureFullScreen();
            ImageIO.write(image, "png", destination);

            return new CaptureResult(destination.getAbsolutePath(),
                    capturedAt.atZone(java.time.ZoneId.systemDefault()).format(DISPLAY_TIMESTAMP_FORMAT),
                    image.getWidth(), image.getHeight(), true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture screenshot for test: " + testName, e);
        }
    }

    private static BufferedImage captureFullScreen() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Desktop screenshot requires a visible Windows display");
        }
        Rectangle screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return new Robot().createScreenCapture(screenBounds);
    }

    private static void focusBrowserWindow(WebDriver driver) {
        if (driver instanceof ChromiumDriver chromiumDriver) {
            Map<String, Object> targets = chromiumDriver.executeCdpCommand("Target.getTargets", Map.of());
            String currentUrl = driver.getCurrentUrl();
            Object targetInfos = targets.get("targetInfos");
            if (targetInfos instanceof Iterable<?> infos) {
                for (Object targetInfo : infos) {
                    if (targetInfo instanceof Map<?, ?> info
                            && currentUrl.equals(info.get("url"))) {
                        chromiumDriver.executeCdpCommand("Target.activateTarget", Map.of(
                                "targetId", info.get("targetId")));
                        break;
                    }
                }
            }
            Map<String, Object> window = chromiumDriver.executeCdpCommand("Browser.getWindowForTarget", Map.of());
            int windowId = ((Number) window.get("windowId")).intValue();
            chromiumDriver.executeCdpCommand("Browser.setWindowBounds", Map.of(
                    "windowId", windowId,
                    "bounds", Map.of("windowState", "maximized", "focused", true)));
            try {
                Thread.sleep(250);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while focusing the browser window", exception);
            }
        }
    }

    public record CaptureResult(String path, String timestamp, int width, int height, boolean fullPage) {
    }
}
