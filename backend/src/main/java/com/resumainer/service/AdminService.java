package com.resumainer.service;

import com.resumainer.dao.AdminDao;
import com.resumainer.dto.admin.AdminDashboardDto;
import org.springframework.stereotype.Service;

/**
 * Business logic for admin operations.
 * <p>
 * Will be extended with resume/user queries and mutation logic in subsequent phases.
 */
@Service
public class AdminService {

    private final AdminDao adminDao;

    public AdminService(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    /**
     * Returns dashboard summary with real user/resume counts and WIP token stats.
     */
    public AdminDashboardDto getDashboard() {
        long totalUsers = adminDao.countNonDeletedUsers();
        long totalResumes = adminDao.countNonDeletedResumes();
        return new AdminDashboardDto(totalUsers, totalResumes);
    }
}
