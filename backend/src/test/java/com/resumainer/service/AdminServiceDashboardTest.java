package com.resumainer.service;

import com.resumainer.dao.AdminDao;
import com.resumainer.dto.admin.AdminDashboardDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AdminService dashboard composition.
 * Verifies that real DAO counts are combined with WIP token stats.
 */
class AdminServiceDashboardTest {

    private AdminDao adminDao;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminDao = mock(AdminDao.class);
        adminService = new AdminService(adminDao);
    }

    @Test
    void getDashboard_returnsRealUserCount() {
        when(adminDao.countNonDeletedUsers()).thenReturn(10L);
        when(adminDao.countNonDeletedResumes()).thenReturn(25L);

        AdminDashboardDto dto = adminService.getDashboard();

        assertEquals(10L, dto.getTotalUsers());
        verify(adminDao).countNonDeletedUsers();
    }

    @Test
    void getDashboard_returnsRealResumeCount() {
        when(adminDao.countNonDeletedUsers()).thenReturn(10L);
        when(adminDao.countNonDeletedResumes()).thenReturn(25L);

        AdminDashboardDto dto = adminService.getDashboard();

        assertEquals(25L, dto.getTotalResumes());
        verify(adminDao).countNonDeletedResumes();
    }

    @Test
    void getDashboard_returnsTokenSentAsWipZero() {
        when(adminDao.countNonDeletedUsers()).thenReturn(10L);
        when(adminDao.countNonDeletedResumes()).thenReturn(25L);

        AdminDashboardDto dto = adminService.getDashboard();

        assertEquals(0L, dto.getTotalTokensSent());
        assertTrue(dto.isTotalTokensSentWip());
    }

    @Test
    void getDashboard_returnsTokenGeneratedAsWipZero() {
        when(adminDao.countNonDeletedUsers()).thenReturn(10L);
        when(adminDao.countNonDeletedResumes()).thenReturn(25L);

        AdminDashboardDto dto = adminService.getDashboard();

        assertEquals(0L, dto.getTotalTokensGenerated());
        assertTrue(dto.isTotalTokensGeneratedWip());
    }

    @Test
    void getDashboard_zeroCounts() {
        when(adminDao.countNonDeletedUsers()).thenReturn(0L);
        when(adminDao.countNonDeletedResumes()).thenReturn(0L);

        AdminDashboardDto dto = adminService.getDashboard();

        assertEquals(0L, dto.getTotalUsers());
        assertEquals(0L, dto.getTotalResumes());
    }
}
