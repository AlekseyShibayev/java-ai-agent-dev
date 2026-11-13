package com.example.javaaiagentdev.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * One browser, context and page per test. Tracing starts immediately so a failure can still be
 * exported; artifacts land in {@code build/playwright-artifacts}.
 */
final class PlaywrightSession implements AutoCloseable {

    private static final Path ARTIFACTS_DIR = Path.of("build", "playwright-artifacts");

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private boolean tracingStopped;

    private PlaywrightSession(Playwright playwright, Browser browser, BrowserContext context, Page page) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
    }

    static PlaywrightSession open() {
        Playwright playwright = Playwright.create();
        try {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(PlaywrightConfig.headless()));
            BrowserContext context = browser.newContext();
            context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            return new PlaywrightSession(playwright, browser, context, context.newPage());
        } catch (RuntimeException e) {
            playwright.close();
            throw e;
        }
    }

    static Path artifactsDir() {
        return ARTIFACTS_DIR;
    }

    Page page() {
        return page;
    }

    void openResource(String classpathResource) {
        var url = Objects.requireNonNull(PlaywrightSession.class.getResource(classpathResource),
                () -> "missing test resource: " + classpathResource);
        try {
            page.navigate(url.toURI().toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(classpathResource, e);
        }
    }

    Path captureArtifacts(String name) {
        Path dir = artifactsDir();
        try {
            Files.createDirectories(dir);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(dir.resolve(name + ".png"))
                    .setFullPage(true));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (!tracingStopped) {
            context.tracing().stop(new Tracing.StopOptions().setPath(dir.resolve(name + "-trace.zip")));
            tracingStopped = true;
        }
        return dir;
    }

    @Override
    public void close() {
        try {
            if (!tracingStopped) {
                tracingStopped = true;
                context.tracing().stop();
            }
        } finally {
            try {
                context.close();
            } finally {
                try {
                    browser.close();
                } finally {
                    playwright.close();
                }
            }
        }
    }
}
