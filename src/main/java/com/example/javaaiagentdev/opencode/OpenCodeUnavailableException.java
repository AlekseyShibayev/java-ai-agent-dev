package com.example.javaaiagentdev.opencode;

/** The local OpenCode service cannot be reached or answered with an error. */
public class OpenCodeUnavailableException extends IllegalStateException {

    public OpenCodeUnavailableException(String message) {
        super(message);
    }

    public OpenCodeUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
