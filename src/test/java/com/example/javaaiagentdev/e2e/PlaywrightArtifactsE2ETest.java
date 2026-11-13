package com.example.javaaiagentdev.e2e;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

class PlaywrightArtifactsE2ETest extends PlaywrightE2ETest {

    @Test
    void shouldSaveScreenshotAndTrace() throws IOException {
        session.openResource("/e2e/fixture.html");

        Path dir = session.captureArtifacts("shouldSaveScreenshotAndTrace");

        assertTrue(Files.size(dir.resolve("shouldSaveScreenshotAndTrace.png")) > 0);
        assertTrue(Files.size(dir.resolve("shouldSaveScreenshotAndTrace-trace.zip")) > 0);
    }

    @Test
    void shouldSaveArtifactsWhenTestFails() throws IOException, NoSuchMethodException {
        PlaywrightExtension extension = new PlaywrightExtension();
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getExecutionException()).thenReturn(Optional.of(new AssertionError("simulated failure")));
        when(context.getRequiredTestMethod())
                .thenReturn(PlaywrightArtifactsE2ETest.class.getDeclaredMethod("shouldSaveArtifactsWhenTestFails"));

        extension.beforeEach(context);
        extension.session().openResource("/e2e/fixture.html");

        extension.afterEach(context);

        Path dir = PlaywrightSession.artifactsDir();
        assertTrue(Files.size(dir.resolve("shouldSaveArtifactsWhenTestFails.png")) > 0);
        assertTrue(Files.size(dir.resolve("shouldSaveArtifactsWhenTestFails-trace.zip")) > 0);
        assertNull(extension.session());
    }
}
