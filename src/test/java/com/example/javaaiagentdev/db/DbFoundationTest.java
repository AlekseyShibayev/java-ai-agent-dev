package com.example.javaaiagentdev.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers(disabledWithoutDocker = true)
class DbFoundationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("devagent")
            .withUsername("owner")
            .withPassword("owner")
            .withEnv("DB_PASSWORD", "app")
            .withEnv("DB_READONLY_PASSWORD", "ro")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("docker/initdb/01-roles.sh"),
                    "/docker-entrypoint-initdb.d/01-roles.sh");

    @Test
    void rolesAreLeastPrivilege() throws Exception {
        try (Connection owner = connect("owner", "owner")) {
            owner.createStatement().execute("create table probe(id int primary key)");
            owner.createStatement().execute("insert into probe values (1)");
        }

        try (Connection app = connect("app", "app")) {
            assertEquals(1, count(app, "select count(*) from probe"));
            app.createStatement().execute("insert into probe values (2)");
        }

        try (Connection readOnly = connect("read_only", "ro")) {
            assertEquals(2, count(readOnly, "select count(*) from probe"));
            assertThrows(SQLException.class,
                    () -> readOnly.createStatement().execute("insert into probe values (3)"));
            assertThrows(SQLException.class,
                    () -> readOnly.createStatement().execute("create table forbidden(id int)"));
        }
    }

    private Connection connect(String user, String password) throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), user, password);
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}
