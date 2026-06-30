package com.resumainer.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Phase 2 migration files exist and contain expected content.
 *
 * <p>Does not execute migrations — only checks file presence
 * and SQL structure to catch obvious errors early.
 */
class MigrationVerificationTest {

    private static final String MIGRATION_DIR = "src/main/resources/db/migration";

    private String readMigration(String filename) throws IOException {
        Path path = Paths.get(MIGRATION_DIR, filename);
        assertTrue(Files.exists(path), "Migration file must exist: " + filename);
        return Files.readString(path);
    }

    @Test
    @DisplayName("V37 adds auth columns to users")
    void v37_addsAuthColumns() throws IOException {
        String sql = readMigration("V37__add_auth_columns_to_users.sql");
        assertTrue(sql.contains("email_verified"), "V37 must add email_verified");
        assertTrue(sql.contains("email_verified_at"), "V37 must add email_verified_at");
        assertTrue(sql.contains("password_login_enabled"), "V37 must add password_login_enabled");
        assertTrue(sql.contains("ALTER TABLE users"), "V37 must ALTER TABLE users");
    }

    @Test
    @DisplayName("V38 creates auth_tokens table")
    void v38_createsAuthTokens() throws IOException {
        String sql = readMigration("V38__create_auth_tokens_table.sql");
        assertTrue(sql.contains("auth_tokens"), "V38 must create auth_tokens table");
        assertTrue(sql.contains("token_hash"), "V38 must have token_hash column");
        assertTrue(sql.contains("token_type"), "V38 must have token_type column");
        assertTrue(sql.contains("consumed_at"), "V38 must have consumed_at column for one-time use");
        assertFalse(sql.contains("raw_token"), "V38 must NOT store raw tokens");
    }

    @Test
    @DisplayName("V39 creates oauth_accounts table")
    void v39_createsOauthAccounts() throws IOException {
        String sql = readMigration("V39__create_oauth_accounts_table.sql");
        assertTrue(sql.contains("oauth_accounts"), "V39 must create oauth_accounts table");
        assertTrue(sql.contains("provider"), "V39 must have provider column");
        assertTrue(sql.contains("provider_subject"), "V39 must have provider_subject column");
        assertTrue(sql.contains("provider_email_verified"), "V39 must have provider_email_verified");
        assertFalse(sql.contains("access_token"), "V39 must NOT store provider access tokens");
        assertFalse(sql.contains("refresh_token"), "V39 must NOT store provider refresh tokens");
    }

    @Test
    @DisplayName("V40 creates persistent_logins table (Spring Security standard)")
    void v40_createsPersistentLogins() throws IOException {
        String sql = readMigration("V40__create_persistent_logins_table.sql");
        assertTrue(sql.contains("persistent_logins"), "V40 must create persistent_logins table");
        assertTrue(sql.contains("username"), "V40 must have username column");
        assertTrue(sql.contains("series"), "V40 must have series column");
        assertTrue(sql.contains("token"), "V40 must have token column");
        assertTrue(sql.contains("last_used"), "V40 must have last_used column");
        assertTrue(sql.contains("VARCHAR(255)"), "V40 username must be VARCHAR(255) for email compatibility");
    }

    @Test
    @DisplayName("V41 migrates existing test users")
    void v41_migratesTestUsers() throws IOException {
        String sql = readMigration("V41__migrate_existing_test_users.sql");
        // Normalize whitespace for reliable assertions
        String normalized = sql.replaceAll("\\s+", " ");
        assertTrue(normalized.contains("UPDATE users"), "V41 must UPDATE users");
        assertTrue(normalized.contains("email_verified = TRUE"), "V41 must set email_verified TRUE");
        assertTrue(normalized.contains("Aa123456"), "V41 must contain Aa123456 reference");
        assertTrue(normalized.contains("CAPSTONE TEST DATA ONLY"), "V41 must have CAPSTONE warning");
        assertTrue(normalized.contains("password_hash"), "V41 must update password_hash");
        // Verify BCrypt hash format (dollar signs may appear as \u0024 in normalized string)
        assertTrue(normalized.contains("$2a$12$"), "V41 BCrypt hash must be cost 12");
    }

    @Test
    @DisplayName("No audit_log table exists in migrations")
    void noAuditLogTable() throws IOException {
        Path dir = Paths.get(MIGRATION_DIR);
        assertTrue(Files.isDirectory(dir), "Migration directory must exist");

        List<Path> migrations = Files.list(dir)
                .filter(p -> p.toString().endsWith(".sql"))
                .collect(Collectors.toList());

        for (Path migration : migrations) {
            String content = Files.readString(migration);
            assertFalse(content.contains("audit_log"),
                    "No audit_log table must exist in any migration. Found in: " + migration.getFileName());
        }
    }

    @Test
    @DisplayName("All Phase 2 migration files exist with correct names")
    void phase2MigrationFilesExist() {
        assertTrue(Files.exists(Paths.get(MIGRATION_DIR, "V37__add_auth_columns_to_users.sql")));
        assertTrue(Files.exists(Paths.get(MIGRATION_DIR, "V38__create_auth_tokens_table.sql")));
        assertTrue(Files.exists(Paths.get(MIGRATION_DIR, "V39__create_oauth_accounts_table.sql")));
        assertTrue(Files.exists(Paths.get(MIGRATION_DIR, "V40__create_persistent_logins_table.sql")));
        assertTrue(Files.exists(Paths.get(MIGRATION_DIR, "V41__migrate_existing_test_users.sql")));
    }
}
