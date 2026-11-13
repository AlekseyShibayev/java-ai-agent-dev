package com.example.javaaiagentdev.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SqlGuardTest {

    @Test
    void allowsSelect() {
        assertEquals("select 1", SqlGuard.validate(" select 1; "));
    }

    @Test
    void allowsCte() {
        assertTrue(SqlGuard.validate("with x as (select 1) select * from x").startsWith("with"));
    }

    @Test
    void rejectsUpdate() {
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("update t set x=1"));
    }

    @Test
    void rejectsMultipleStatements() {
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("select 1; select 2"));
    }

    @Test
    void rejectsComments() {
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("select 1 -- comment"));
    }

    @Test
    void rejectsSelectInto() {
        assertThrows(IllegalArgumentException.class, () -> SqlGuard.validate("select * into x from t"));
    }
}
