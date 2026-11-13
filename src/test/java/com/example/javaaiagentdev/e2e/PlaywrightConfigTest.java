package com.example.javaaiagentdev.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlaywrightConfigTest {

    @Test
    void shouldDefaultToHeadless() {
        assertTrue(PlaywrightConfig.headless());
    }

    @Test
    void shouldRunHeadedWhenRequested() {
        System.setProperty(PlaywrightConfig.HEADED_PROPERTY, "true");
        try {
            assertFalse(PlaywrightConfig.headless());
        } finally {
            System.clearProperty(PlaywrightConfig.HEADED_PROPERTY);
        }
    }
}
