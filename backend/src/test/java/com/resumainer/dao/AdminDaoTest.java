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
}
