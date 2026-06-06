package com.resumainer.service;

import com.resumainer.dao.ResumeDao;
import com.resumainer.model.PagedResponse;
import com.resumainer.model.SavedResume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service for saved resume operations.
 * <p>
 * Validates input parameters before delegating to {@link ResumeDao}.
 * Enforces sort field whitelist (SEC-001) and delegates owner checks to DAO (SEC-002).
 */
@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 20, 50);

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "resumetitle", "resume_title",
            "vacancy", "vacancy",
            "company", "company",
            "language", "language",
            "adaptationlevel", "adaptation_level",
            "createdat", "created_at"
    );

    private final ResumeDao resumeDao;

    public ResumeService(ResumeDao resumeDao) {
        this.resumeDao = resumeDao;
    }

    /**
     * Paginated resume listing with validation.
     */
    public PagedResponse<SavedResume> listResumes(UUID userId, String search,
                                                   String language, String adaptationLevel,
                                                   String createdDate, String dateFrom,
                                                   String dateTo, String sort,
                                                   int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new IllegalArgumentException("Size must be one of: " + ALLOWED_PAGE_SIZES);
        }

        String sortField = "created_at";
        String sortDir = "desc";

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            sortField = mapSortField(parts[0].trim());
            if (parts.length > 1) {
                sortDir = parts[1].trim().toLowerCase();
                if (!"asc".equals(sortDir) && !"desc".equals(sortDir)) {
                    throw new IllegalArgumentException("Invalid sort direction: " + sortDir);
                }
            }
        }

        log.debug("listResumes: userId={}, page={}, size={}, sort={}, dir={}",
                userId, page, size, sortField, sortDir);

        List<SavedResume> items = resumeDao.findByUserId(userId, search, language,
                adaptationLevel, createdDate, dateFrom, dateTo, sortField, sortDir, page, size);
        long total = resumeDao.countByUserId(userId, search, language,
                adaptationLevel, createdDate, dateFrom, dateTo);

        return new PagedResponse<>(items, page, size, total);
    }

    /**
     * Soft-delete a resume (owner-protected).
     */
    public boolean deleteResume(UUID userId, long resumeId) {
        log.debug("deleteResume: userId={}, resumeId={}", userId, resumeId);
        return resumeDao.softDelete(resumeId, userId);
    }

    private String mapSortField(String field) {
        String key = field.toLowerCase().replace("_", "");
        String dbField = SORT_FIELD_MAP.get(key);
        if (dbField == null) {
            throw new IllegalArgumentException("Invalid sort field: " + field
                    + ". Allowed: " + SORT_FIELD_MAP.keySet());
        }
        return dbField;
    }
}
