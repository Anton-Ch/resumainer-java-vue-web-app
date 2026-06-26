package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AdminDao dashboard counts and admin resume queries.
 * Follows existing DAO test patterns (UserDaoTest style).
 */
class AdminDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private AdminDao adminDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // Default: executeQuery returns a resultSet that reports no rows
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(0L);

        adminDao = new AdminDao(dataSource);
    }

    // --- T026/T028: Dashboard count tests ---

    @Test
    void countNonDeletedUsers_returnsRealCount() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(15L);

        long count = adminDao.countNonDeletedUsers();

        assertEquals(15L, count);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void countNonDeletedUsers_zeroWhenNoUsers() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(0L);

        long count = adminDao.countNonDeletedUsers();

        assertEquals(0L, count);
    }

    @Test
    void countNonDeletedUsers_usesCorrectSql() throws Exception {
        adminDao.countNonDeletedUsers();

        // Verify the SQL excludes deleted users
        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("COUNT(*)") && s.contains("users") && s.contains("is_deleted = FALSE");
                })
        );
    }

    @Test
    void countNonDeletedResumes_returnsRealCount() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(42L);

        long count = adminDao.countNonDeletedResumes();

        assertEquals(42L, count);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void countNonDeletedResumes_zeroWhenNoResumes() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(0L);

        long count = adminDao.countNonDeletedResumes();

        assertEquals(0L, count);
    }

    @Test
    void countNonDeletedResumes_usesCorrectSql() throws Exception {
        adminDao.countNonDeletedResumes();

        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("COUNT(*)") && s.contains("saved_resumes") && s.contains("is_deleted = FALSE");
                })
        );
    }

    @Test
    void countNonDeletedResumes_handlesException() throws Exception {
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> adminDao.countNonDeletedResumes());
    }

    @Test
    void countNonDeletedUsers_handlesException() throws Exception {
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> adminDao.countNonDeletedUsers());
    }

    // --- T029-T034: Admin resume listing tests ---

    @Test
    void findResumes_returnsList_whenRowsExist() throws Exception {
        // Simulate one row: first next() returns true, second returns false
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(101L);
        when(resultSet.getObject("user_id")).thenReturn(java.util.UUID.randomUUID());
        when(resultSet.getString("username")).thenReturn("anton");
        when(resultSet.getString("email")).thenReturn("anton@example.com");
        when(resultSet.getString("full_name")).thenReturn("Anton Ch.");
        when(resultSet.getString("resume_title")).thenReturn("Java Dev");
        when(resultSet.getString("vacancy")).thenReturn("Senior Java Dev");
        when(resultSet.getString("company")).thenReturn("ABC LTD");
        when(resultSet.getString("language")).thenReturn("RU");
        when(resultSet.getString("language_name")).thenReturn("Russian");
        when(resultSet.getString("adaptation_level")).thenReturn("BALANCED");
        when(resultSet.getDate("created_at")).thenReturn(java.sql.Date.valueOf("2026-06-25"));
        when(resultSet.getString("public_code")).thenReturn("ABC123");
        when(resultSet.getString("public_url_link")).thenReturn("https://example.com/anton/ABC123");
        when(resultSet.getString("cover_letter")).thenReturn("Cover letter text");
        when(resultSet.getString("pdf_status")).thenReturn("READY");
        when(resultSet.getString("pdf_file_path")).thenReturn("/pdfs/101.pdf");
        when(resultSet.getString("html_file_path")).thenReturn("/html/101.html");

        java.util.List<AdminDao.AdminSavedResumeRow> results = adminDao.findResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Anton Ch.", results.get(0).fullName);
        assertEquals("anton@example.com", results.get(0).email);
        assertEquals(101L, results.get(0).id);
        assertTrue(results.get(0).pdfFilePresent);
        assertTrue(results.get(0).htmlFilePresent);
    }

    @Test
    void findResumes_returnsEmptyList_whenNoRows() throws Exception {
        when(resultSet.next()).thenReturn(false);

        java.util.List<AdminDao.AdminSavedResumeRow> results = adminDao.findResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findResumes_setsPaginationParameters() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes(null, null, null, null, null,
                "createdAt", "desc", 2, 15);

        // Verify LIMIT and OFFSET are set
        verify(preparedStatement).setInt(anyInt(), eq(15)); // LIMIT
        verify(preparedStatement).setInt(anyInt(), eq(30)); // OFFSET (2 * 15)
    }

    @Test
    void countResumes_returnsTotalCount() throws Exception {
        when(resultSet.getLong(1)).thenReturn(48L);

        long count = adminDao.countResumes(null, null, null, null, null);

        assertEquals(48L, count);
    }

    @Test
    void findResumes_invalidSortField_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                adminDao.findResumes(null, null, null, null, null,
                        "password", "desc", 0, 10)
        );
    }

    @Test
    void findResumes_invalidSortDir_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                adminDao.findResumes(null, null, null, null, null,
                        "createdAt", "invalid", 0, 10)
        );
    }

    @Test
    void countResumes_emptyResult_returnsZero() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(0L);

        long count = adminDao.countResumes(null, "EN", null, null, null);

        assertEquals(0L, count);
    }

    @Test
    void findResumes_searchParameter_appliesToResumeFields() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes("developer", null, null, null, null,
                "createdAt", "desc", 0, 10);

        // Verify search parameter is bound to all 6 search fields
        verify(preparedStatement, atLeast(6)).setString(anyInt(), contains("developer"));
    }

    @Test
    void findResumes_languageFilter_appliesInClause() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes(null, "EN,RU", null, null, null,
                "createdAt", "desc", 0, 10);

        // Verify SQL contains IN clause
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("sr.language IN"))
        );
    }

    @Test
    void findResumes_adaptationLevelFilter_appliesInClause() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes(null, null, "BALANCED,STANDARD", null, null,
                "createdAt", "desc", 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("sr.adaptation_level IN"))
        );
    }

    @Test
    void findResumes_dateFrom_appliedAsInclusive() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes(null, null, null, "2026-06-01", null,
                "createdAt", "desc", 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("created_at >= ?::date"))
        );
    }

    @Test
    void findResumes_dateTo_appliedAsExclusiveNextDay() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes(null, null, null, null, "2026-06-25",
                "createdAt", "desc", 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("created_at < (?::date + INTERVAL '1 day')"))
        );
    }

    @Test
    void findResumes_dateFromBeforeDateTo_doesNotCrash() throws Exception {
        when(resultSet.next()).thenReturn(false);

        java.util.List<AdminDao.AdminSavedResumeRow> results = adminDao.findResumes(
                null, null, null, "2026-06-25", "2026-06-01",
                "createdAt", "desc", 0, 10);

        // Should not throw - just return empty or whatever the DB would return
        assertNotNull(results);
    }

    @Test
    void findResumes_defaultSortIsCreatedAtDesc() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findResumes(null, null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("ORDER BY sr.created_at desc");
                })
        );
    }

    @Test
    void findResumes_allowedSortFields_mapCorrectly() throws Exception {
        when(resultSet.next()).thenReturn(false);

        // Each allowed sort field should not throw
        String[] sortFields = {"resumeTitle", "vacancyTitle", "companyName",
                "language", "adaptationLevel", "createdAt",
                "ownerUsername", "ownerEmail", "ownerFullName"};

        for (String field : sortFields) {
            assertDoesNotThrow(() ->
                    adminDao.findResumes(null, null, null, null, null,
                            field, "asc", 0, 10)
            );
        }
    }

    @Test
    void findResumes_missingContactDetail_stillReturnsRow() throws Exception {
        // Simulate LEFT JOIN producing NULL for contact_detail fields
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(102L);
        when(resultSet.getObject("user_id")).thenReturn(java.util.UUID.randomUUID());
        when(resultSet.getString("username")).thenReturn("johndoe");
        when(resultSet.getString("email")).thenReturn("john@example.com");
        when(resultSet.getString("full_name")).thenReturn(null); // LEFT JOIN null
        when(resultSet.getString("resume_title")).thenReturn("Python Dev");
        when(resultSet.getString("vacancy")).thenReturn("Backend Dev");
        when(resultSet.getString("company")).thenReturn("XYZ Corp");
        when(resultSet.getString("language")).thenReturn("EN");
        when(resultSet.getString("language_name")).thenReturn("English");
        when(resultSet.getString("adaptation_level")).thenReturn("STANDARD");
        when(resultSet.getDate("created_at")).thenReturn(java.sql.Date.valueOf("2026-06-20"));
        when(resultSet.getString("public_code")).thenReturn("DEF456");
        when(resultSet.getString("public_url_link")).thenReturn(null);
        when(resultSet.getString("cover_letter")).thenReturn(null);
        when(resultSet.getString("pdf_status")).thenReturn(null);
        when(resultSet.getString("pdf_file_path")).thenReturn(null);
        when(resultSet.getString("html_file_path")).thenReturn(null);

        java.util.List<AdminDao.AdminSavedResumeRow> results = adminDao.findResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertNull(results.get(0).fullName); // null because LEFT JOIN
        assertEquals("john@example.com", results.get(0).email);
        assertFalse(results.get(0).pdfFilePresent);
        assertFalse(results.get(0).htmlFilePresent);
    }

    // --- Phase 3: Admin resume soft-delete tests ---

    @Test
    void adminSoftDeleteResume_returnsTrue_whenUpdated() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = adminDao.adminSoftDeleteResume(101L);

        assertTrue(result);
    }

    @Test
    void adminSoftDeleteResume_returnsFalse_whenNoRowUpdated() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = adminDao.adminSoftDeleteResume(999L);

        assertFalse(result);
    }

    @Test
    void adminSoftDeleteResume_setsIsDeletedAndDeletedAt() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        adminDao.adminSoftDeleteResume(101L);

        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("UPDATE saved_resumes")
                            && s.contains("is_deleted = TRUE")
                            && s.contains("deleted_at = NOW()")
                            && s.contains("WHERE id = ?")
                            && !s.contains("user_id");  // not owner-scoped
                })
        );
    }

    @Test
    void adminSoftDeleteResume_setsResumeIdParameter() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        adminDao.adminSoftDeleteResume(101L);

        verify(preparedStatement).setLong(1, 101L);
    }

    @Test
    void adminSoftDeleteResume_skipsAlreadyDeletedResume() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = adminDao.adminSoftDeleteResume(101L);

        assertFalse(result);
        // SQL includes AND is_deleted = FALSE to prevent re-deleting
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("is_deleted = FALSE"))
        );
    }

    @Test
    void adminSoftDeleteResume_notOwnerScoped() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        adminDao.adminSoftDeleteResume(101L);

        // Admin delete must not be owner-scoped
        verify(connection).prepareStatement(
                argThat(sql -> !sql.toString().contains("user_id"))
        );
    }

    @Test
    void adminSoftDeleteResume_handlesException() throws Exception {
        when(preparedStatement.executeUpdate()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> adminDao.adminSoftDeleteResume(101L));
    }

    @Test
    void adminSoftDeleteResume_doesNotHardDelete() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        adminDao.adminSoftDeleteResume(101L);

        // Verify no DELETE statement is used
        verify(connection).prepareStatement(
                argThat(sql -> !sql.toString().toUpperCase().startsWith("DELETE"))
        );
    }

    // --- Phase 4: Admin users listing tests ---

    @Test
    void findUsers_excludesDeletedUsers() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("u.is_deleted = FALSE"))
        );
    }

    @Test
    void findUsers_returnsList_whenRowsExist() throws Exception {
        java.util.UUID userId = java.util.UUID.randomUUID();
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject("id", java.util.UUID.class)).thenReturn(userId);
        when(resultSet.getString("full_name")).thenReturn("John Doe");
        when(resultSet.getString("username")).thenReturn("johndoe");
        when(resultSet.getString("email")).thenReturn("john@example.com");
        when(resultSet.getString("role_code")).thenReturn("USER");
        when(resultSet.getString("role_name")).thenReturn("Regular User");
        when(resultSet.getString("status_code")).thenReturn("ACTIVE");
        when(resultSet.getString("status_name")).thenReturn("Active");
        when(resultSet.getString("permission_code")).thenReturn("ALLOWED");
        when(resultSet.getString("permission_name")).thenReturn("Allowed");
        when(resultSet.getBoolean("is_privileged")).thenReturn(false);
        when(resultSet.getLong("resumes_count")).thenReturn(3L);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2026, 6, 1, 12, 0)));

        java.util.List<AdminDao.AdminUserRow> results = adminDao.findUsers(
                null, null, null, null, null, null, null,
                null, null, 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("johndoe", results.get(0).username);
        assertEquals("john@example.com", results.get(0).email);
        assertEquals("John Doe", results.get(0).fullName);
        assertEquals("USER", results.get(0).roleCode);
        assertEquals("Active", results.get(0).statusName);
        assertEquals(3L, results.get(0).resumesCount);
    }

    @Test
    void findUsers_returnsEmptyList_whenNoRows() throws Exception {
        when(resultSet.next()).thenReturn(false);

        java.util.List<AdminDao.AdminUserRow> results = adminDao.findUsers(
                null, null, null, null, null, null, null,
                null, null, 0, 10);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findUsers_missingContactDetail_stillReturnsRow() throws Exception {
        java.util.UUID userId = java.util.UUID.randomUUID();
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject("id", java.util.UUID.class)).thenReturn(userId);
        when(resultSet.getString("full_name")).thenReturn(null); // LEFT JOIN null
        when(resultSet.getString("username")).thenReturn("johndoe");
        when(resultSet.getString("email")).thenReturn("john@example.com");
        when(resultSet.getString("role_code")).thenReturn("USER");
        when(resultSet.getString("role_name")).thenReturn("Regular User");
        when(resultSet.getString("status_code")).thenReturn("ACTIVE");
        when(resultSet.getString("status_name")).thenReturn("Active");
        when(resultSet.getString("permission_code")).thenReturn("ALLOWED");
        when(resultSet.getString("permission_name")).thenReturn("Allowed");
        when(resultSet.getBoolean("is_privileged")).thenReturn(false);
        when(resultSet.getLong("resumes_count")).thenReturn(0L);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2026, 6, 1, 12, 0)));

        java.util.List<AdminDao.AdminUserRow> results = adminDao.findUsers(
                null, null, null, null, null, null, null,
                null, null, 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertNull(results.get(0).fullName); // null because LEFT JOIN
        assertEquals("john@example.com", results.get(0).email);
    }

    @Test
    void findUsers_searchParameter_appliesToAllSearchFields() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers("searchTerm", null, null, null, null, null, null,
                null, null, 0, 10);

        // Verify SQL has search on username, email, and full_name
        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("LOWER(u.username) LIKE")
                            && s.contains("LOWER(u.email) LIKE")
                            && s.contains("LOWER(cd.full_name) LIKE");
                })
        );
    }

    @Test
    void findUsers_roleFilter_appliesCorrectly() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, "ADMIN", null, null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("r.code IN"))
        );
    }

    @Test
    void findUsers_statusFilter_appliesCorrectly() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, "ACTIVE", null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("us.code IN"))
        );
    }

    @Test
    void findUsers_permissionFilter_appliesCorrectly() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, "ALLOWED", null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("up.code IN"))
        );
    }

    @Test
    void findUsers_rightsFilterPrivileged_appliesCorrectly() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, "PRIVILEGED", null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("u.is_privileged = TRUE"))
        );
    }

    @Test
    void findUsers_rightsFilterNonPrivileged_appliesCorrectly() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, "NON_PRIVILEGED", null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("u.is_privileged = FALSE"))
        );
    }

    @Test
    void findUsers_dateFrom_appliedAsInclusive() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, null, "2026-06-01", null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("u.created_at >= ?::date"))
        );
    }

    @Test
    void findUsers_dateTo_appliedAsExclusiveNextDay() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, null, null, "2026-06-25",
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("u.created_at < (?::date + INTERVAL '1 day')"))
        );
    }

    @Test
    void findUsers_JOINsLookupTables() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("JOIN role r") && s.contains("JOIN user_status us")
                            && s.contains("JOIN user_permission up")
                            && s.contains("LEFT JOIN contact_detail cd");
                })
        );
    }

    @Test
    void findUsers_LEFTJOINSavedResumesForCount() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("LEFT JOIN saved_resumes sr")
                            && s.contains("COUNT(sr.id)");
                })
        );
    }

    @Test
    void findUsers_defaultSortIsCreatedAtDesc() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUsers(null, null, null, null, null, null, null,
                null, null, 0, 10);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("ORDER BY u.created_at desc"))
        );
    }

    @Test
    void findUsers_invalidSortField_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                adminDao.findUsers(null, null, null, null, null, null, null,
                        "password", "asc", 0, 10)
        );
    }

    @Test
    void countUsers_returnsTotalCount() throws Exception {
        when(resultSet.getLong(1)).thenReturn(12L);

        long count = adminDao.countUsers(null, null, null, null, null, null, null);

        assertEquals(12L, count);
    }

    @Test
    void countUsers_emptyResult_returnsZero() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(0L);

        long count = adminDao.countUsers(null, null, null, null, null, null, null);

        assertEquals(0L, count);
    }

    // --- Phase 5: Admin user details tests ---

    @Test
    void findUserDetails_returnsRow_whenUserExists() throws Exception {
        java.util.UUID userId = java.util.UUID.randomUUID();
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject("id")).thenReturn(userId);
        when(resultSet.getString("username")).thenReturn("johndoe");
        when(resultSet.getString("account_email")).thenReturn("john@example.com");
        when(resultSet.getString("role_code")).thenReturn("USER");
        when(resultSet.getString("role_name")).thenReturn("Regular User");
        when(resultSet.getString("status_code")).thenReturn("ACTIVE");
        when(resultSet.getString("status_name")).thenReturn("Active");
        when(resultSet.getString("permission_code")).thenReturn("ALLOWED");
        when(resultSet.getString("permission_name")).thenReturn("Allowed");
        when(resultSet.getBoolean("is_privileged")).thenReturn(false);
        when(resultSet.getString("default_lang_code")).thenReturn("EN");
        when(resultSet.getString("default_lang_name")).thenReturn("English");
        when(resultSet.getString("secondary_lang_code")).thenReturn(null);
        when(resultSet.getString("secondary_lang_name")).thenReturn(null);
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getString("full_name")).thenReturn("John Doe");
        when(resultSet.getString("professional_title")).thenReturn("Developer");
        when(resultSet.getString("phone")).thenReturn("+123456789");
        when(resultSet.getString("resume_email")).thenReturn("resume@example.com");
        when(resultSet.getString("location")).thenReturn("Almaty");
        when(resultSet.getString("linkedin_url")).thenReturn("https://linkedin.com/in/johndoe");
        when(resultSet.getString("portfolio_url")).thenReturn(null);
        when(resultSet.getString("telegram")).thenReturn(null);
        when(resultSet.getString("whatsapp")).thenReturn(null);
        when(resultSet.getString("skills")).thenReturn("Java, Spring");
        when(resultSet.getString("api_languages")).thenReturn("English, Russian");
        when(resultSet.getString("professional_aspirations")).thenReturn("Grow");
        when(resultSet.getString("achievements")).thenReturn(null);
        when(resultSet.getString("general_information")).thenReturn(null);
        when(resultSet.getString("ready_for_relocation")).thenReturn("YES");
        when(resultSet.getString("ready_for_business_trips")).thenReturn("NO");
        when(resultSet.getDate("date_of_birth")).thenReturn(java.sql.Date.valueOf("1993-01-01"));
        when(resultSet.getString("citizenship")).thenReturn("Kazakhstan");

        AdminDao.AdminUserDetailsRow row = adminDao.findUserDetails(userId);

        assertNotNull(row);
        assertEquals("john@example.com", row.accountEmail);
        assertEquals("resume@example.com", row.resumeEmail);
        assertEquals("John Doe", row.fullName);
        assertEquals("USER", row.roleCode);
        assertEquals("Active", row.statusName);
        assertEquals("EN", row.defaultLanguageCode);
    }

    @Test
    void findUserDetails_excludesDeletedUsers() throws Exception {
        java.util.UUID userId = java.util.UUID.randomUUID();
        when(resultSet.next()).thenReturn(false);

        AdminDao.AdminUserDetailsRow row = adminDao.findUserDetails(userId);

        assertNull(row);
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("is_deleted = FALSE"))
        );
    }

    @Test
    void findUserDetails_usesLEFTJOINForContact() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUserDetails(java.util.UUID.randomUUID());

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("LEFT JOIN contact_detail cd"))
        );
    }

    @Test
    void findUserDetails_usesLEFTJOINForAdditionalInfo() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUserDetails(java.util.UUID.randomUUID());

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("LEFT JOIN additional_profile_info api"))
        );
    }

    @Test
    void findUserDetails_joinsLookupTables() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUserDetails(java.util.UUID.randomUUID());

        verify(connection).prepareStatement(
                argThat(sql -> {
                    String s = sql.toString();
                    return s.contains("JOIN role r") && s.contains("JOIN user_status us")
                            && s.contains("JOIN user_permission up");
                })
        );
    }

    @Test
    void findUserDetails_doesNotSelectPasswordHash() throws Exception {
        when(resultSet.next()).thenReturn(false);

        adminDao.findUserDetails(java.util.UUID.randomUUID());

        verify(connection).prepareStatement(
                argThat(sql -> !sql.toString().toLowerCase().contains("password_hash"))
        );
    }

    @Test
    void findUserDetails_setsUserIdParameter() throws Exception {
        when(resultSet.next()).thenReturn(false);
        java.util.UUID userId = java.util.UUID.randomUUID();

        adminDao.findUserDetails(userId);

        verify(preparedStatement).setObject(1, userId);
    }

    @Test
    void findUserDetails_handlesException() throws Exception {
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () ->
                adminDao.findUserDetails(java.util.UUID.randomUUID())
        );
    }

    // --- Phase 6: Access update and user soft-delete tests ---

    @Test
    void existsAndNotDeleted_returnsTrue_whenUserExists() throws Exception {
        when(resultSet.next()).thenReturn(true);

        boolean exists = adminDao.existsAndNotDeleted(java.util.UUID.randomUUID());

        assertTrue(exists);
    }

    @Test
    void existsAndNotDeleted_returnsFalse_whenUserNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        boolean exists = adminDao.existsAndNotDeleted(java.util.UUID.randomUUID());

        assertFalse(exists);
    }

    @Test
    void updateUserAccess_returnsTrue_whenUpdated() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        java.util.UUID userId = java.util.UUID.randomUUID();

        boolean result = adminDao.updateUserAccess(userId, 1L, 1L, 1L, false);

        assertTrue(result);
        verify(preparedStatement).setLong(1, 1L); // role_id
        verify(preparedStatement).setObject(5, userId); // WHERE id
    }

    @Test
    void updateUserAccess_returnsFalse_whenUserNotFound() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = adminDao.updateUserAccess(java.util.UUID.randomUUID(), 1L, 1L, 1L, false);

        assertFalse(result);
    }

    @Test
    void updateUserAccess_setsUpdatedAtToNow() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        adminDao.updateUserAccess(java.util.UUID.randomUUID(), 1L, 1L, 1L, false);

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("updated_at = NOW()"))
        );
    }

    @Test
    void findRoleIdByCode_returnsId_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);

        Long id = adminDao.findRoleIdByCode("USER");

        assertEquals(1L, id);
        verify(preparedStatement).setString(1, "USER");
    }

    @Test
    void findRoleIdByCode_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        Long id = adminDao.findRoleIdByCode("UNKNOWN");

        assertNull(id);
    }

    @Test
    void findStatusIdByCode_returnsId_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(2L);

        Long id = adminDao.findStatusIdByCode("BLOCKED");

        assertEquals(2L, id);
    }

    @Test
    void findPermissionIdByCode_returnsId_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(2L);

        Long id = adminDao.findPermissionIdByCode("FORBIDDEN");

        assertEquals(2L, id);
    }

    @Test
    void findUserAccessState_returnsState() throws Exception {
        java.util.UUID userId = java.util.UUID.randomUUID();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("role_code")).thenReturn("ADMIN");
        when(resultSet.getString("status_code")).thenReturn("ACTIVE");

        AdminDao.UserAccessState state = adminDao.findUserAccessState(userId);

        assertNotNull(state);
        assertEquals("ADMIN", state.roleCode);
        assertEquals("ACTIVE", state.statusCode);
        verify(preparedStatement).setObject(1, userId);
    }

    @Test
    void findUserAccessState_returnsNull_whenUserNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        AdminDao.UserAccessState state = adminDao.findUserAccessState(java.util.UUID.randomUUID());

        assertNull(state);
    }

    @Test
    void adminSoftDeleteUser_marksUserDeleted() throws Exception {
        // Mock all 6 executeUpdate calls to return 1 (success)
        when(preparedStatement.executeUpdate()).thenReturn(1, 1, 1, 1, 1, 1);
        java.util.UUID userId = java.util.UUID.randomUUID();

        adminDao.adminSoftDeleteUser(userId);

        verify(preparedStatement, atLeast(1)).setObject(1, userId);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
    }

    @Test
    void adminSoftDeleteUser_skipsWhenAlreadyDeleted() throws Exception {
        // First executeUpdate (user update) returns 0 → user not found/already deleted
        when(preparedStatement.executeUpdate()).thenReturn(0);
        java.util.UUID userId = java.util.UUID.randomUUID();

        adminDao.adminSoftDeleteUser(userId);

        verify(connection).rollback();
        // commit should NOT be called when user not found
        verify(connection, never()).commit();
    }

    @Test
    void adminSoftDeleteUser_updatesAllCascadeTables() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1, 1, 1, 1, 1, 1);
        java.util.UUID userId = java.util.UUID.randomUUID();

        adminDao.adminSoftDeleteUser(userId);

        // Verify SQL patterns for each cascade
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("UPDATE users") && sql.toString().contains("is_deleted = TRUE"))
        );
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("UPDATE saved_resumes"))
        );
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("UPDATE work_experience"))
        );
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("UPDATE education"))
        );
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("UPDATE project"))
        );
        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("UPDATE course_certificate"))
        );
    }

    @Test
    void adminSoftDeleteUser_doesNotHardDelete() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1, 1, 1, 1, 1, 1);

        adminDao.adminSoftDeleteUser(java.util.UUID.randomUUID());

        // Verify NO hard-delete (DELETE FROM) statements are used
        verify(connection, never()).prepareStatement(
                argThat(sql -> sql.toString().toUpperCase().startsWith("DELETE "))
        );
    }

    @Test
    void adminSoftDeleteUser_setsStatusToBlocked() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1, 1, 1, 1, 1, 1);

        adminDao.adminSoftDeleteUser(java.util.UUID.randomUUID());

        verify(connection).prepareStatement(
                argThat(sql -> sql.toString().contains("code = " + "'BLOCKED'"))
        );
    }
}
