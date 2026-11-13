package com.example.javaaiagentdev.db;

import java.util.Locale;
import java.util.regex.Pattern;

public final class SqlGuard {

    public static final int MAX_ROWS = 1_000;
    public static final int TIMEOUT_SECONDS = 5;

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|merge|drop|alter|create|truncate|grant|revoke|copy|call|do|vacuum|analyze|into)\\b");

    private SqlGuard() {
    }

    public static String validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL is empty");
        }
        String text = sql.strip();
        if (text.contains("--") || text.contains("/*") || text.contains("*/")) {
            throw new IllegalArgumentException("SQL comments are not allowed");
        }
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).strip();
        }
        if (text.contains(";")) {
            throw new IllegalArgumentException("Only one statement is allowed");
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("select") && !lower.startsWith("with")) {
            throw new IllegalArgumentException("Only SELECT is allowed");
        }
        if (FORBIDDEN.matcher(lower).find()) {
            throw new IllegalArgumentException("Forbidden keyword in SQL");
        }
        return text;
    }
}
