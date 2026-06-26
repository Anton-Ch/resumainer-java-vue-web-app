package com.resumainer.dao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

/**
 * Data access for admin-specific cross-user queries.
 * <p>
 * Currently provides dashboard aggregate counts.
 * Will be extended with admin resume, user, and user detail queries in Phase 2+.
 */
@Repository
public class AdminDao {

    private final DataSource dataSource;

    public AdminDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns total number of non-deleted users.
     */
    public long countNonDeletedUsers() {
        // TODO: implement in Phase 2
        return 0;
    }

    /**
     * Returns total number of non-deleted saved resumes.
     */
    public long countNonDeletedResumes() {
        // TODO: implement in Phase 2
        return 0;
    }
}
