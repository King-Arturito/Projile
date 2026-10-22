package io.github.kingarturito.projile.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.sql.*;
import java.util.UUID;

/** Never uses the application's configured database. Each test JVM gets a new schema. */
public final class TestDatabase {
    private static String url;
    private static String username;
    private static String password;
    private static PostgreSQLContainer container;
    private static final String SCHEMA = "auth_test_" + UUID.randomUUID().toString().replace("-", "");

    private TestDatabase() { }

    public static synchronized void configure(DynamicPropertyRegistry properties) {
        start();
        properties.add("spring.datasource.url", TestDatabase::url);
        properties.add("spring.datasource.username", () -> username);
        properties.add("spring.datasource.password", () -> password);
        properties.add("spring.liquibase.default-schema", () -> SCHEMA);
        properties.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
        properties.add("spring.liquibase.contexts", () -> "auth-tests");
    }

    public static synchronized void start() {
        if (url != null) return;
        String supplied = System.getenv("AUTH_TEST_DATABASE_URL");
        if (supplied == null || supplied.isBlank()) {
            container = new PostgreSQLContainer("postgres:16-alpine").withDatabaseName("projile_auth_test");
            container.start();
            supplied = container.getJdbcUrl(); username = container.getUsername(); password = container.getPassword();
        } else {
            if (!supplied.startsWith("jdbc:postgresql:") || supplied.toLowerCase().contains("currentschema=")) {
                throw new IllegalArgumentException("AUTH_TEST_DATABASE_URL must be a PostgreSQL JDBC URL without currentSchema; use a disposable database.");
            }
            username = System.getenv().getOrDefault("AUTH_TEST_DATABASE_USERNAME", "postgres");
            password = System.getenv().getOrDefault("AUTH_TEST_DATABASE_PASSWORD", "");
        }
        try (Connection connection = DriverManager.getConnection(supplied, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + SCHEMA);
        } catch (SQLException failure) {
            throw new IllegalStateException("Cannot create an isolated auth test schema; configure a disposable PostgreSQL database or Docker.", failure);
        }
        url = supplied + (supplied.contains("?") ? "&" : "?") + "currentSchema=" + SCHEMA;
    }

    public static String url() { start(); return url; }
    public static String username() { start(); return username; }
    public static String password() { start(); return password; }
    public static String schema() { return SCHEMA; }
}
