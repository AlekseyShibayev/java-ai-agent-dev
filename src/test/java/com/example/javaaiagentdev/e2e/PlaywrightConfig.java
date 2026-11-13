package com.example.javaaiagentdev.e2e;

/**
 * Headless by default; {@code ./gradlew e2e -Pheaded} sets the property to watch the browser.
 */
final class PlaywrightConfig {

    static final String HEADED_PROPERTY = "e2e.headed";

    private PlaywrightConfig() {
    }

    static boolean headless() {
        return !Boolean.getBoolean(HEADED_PROPERTY);
    }
}
