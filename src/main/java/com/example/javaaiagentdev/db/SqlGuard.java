package com.example.javaaiagentdev.db;

public final class SqlGuard {

    public static final int MAX_ROWS = 1_000;
    public static final int TIMEOUT_SECONDS = 5;

    private SqlGuard() {
    }

    public static String validate(String sql) {
        return sql == null ? null : sql.strip();
    }
}
