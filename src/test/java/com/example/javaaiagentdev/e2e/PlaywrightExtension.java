package com.example.javaaiagentdev.e2e;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Opens a session before each test; on failure saves screenshot and trace before closing.
 */
final class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback {

    private PlaywrightSession session;

    @Override
    public void beforeEach(ExtensionContext context) {
        session = PlaywrightSession.open();
    }

    PlaywrightSession session() {
        return session;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        PlaywrightSession current = session;
        session = null;
        if (current == null) {
            return;
        }
        try {
            context.getExecutionException().ifPresent(failure -> captureArtifacts(context, current, failure));
        } finally {
            current.close();
        }
    }

    private static void captureArtifacts(ExtensionContext context, PlaywrightSession current, Throwable failure) {
        try {
            current.captureArtifacts(context.getRequiredTestMethod().getName());
        } catch (RuntimeException e) {
            // Keep the original assertion visible instead of masking it with an artifact error.
            failure.addSuppressed(e);
        }
    }
}
