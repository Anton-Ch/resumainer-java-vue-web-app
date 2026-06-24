package com.resumainer.service;

import com.resumainer.dao.ResumeDao;
import com.resumainer.dto.home.HomeSavedResumeDto;
import com.resumainer.model.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for saved resume operations.
 * <p>
 * Validates input parameters before delegating to {@link ResumeDao}.
 * Enforces sort field whitelist (SEC-001) and delegates owner checks to DAO (SEC-002).
 * Maps results to {@link HomeSavedResumeDto} via {@link HomeSavedResumeMapper}.
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
    private final HomeSavedResumeMapper homeMapper;

    public ResumeService(ResumeDao resumeDao, HomeSavedResumeMapper homeMapper) {
        this.resumeDao = resumeDao;
        this.homeMapper = homeMapper;
    }

    /**
     * Paginated resume listing with validation. Returns canonical Home DTOs.
     */
    public PagedResponse<HomeSavedResumeDto> listResumes(UUID userId, HttpServletRequest request,
                                                          String search, String language,
                                                          String adaptationLevel,
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

        List<com.resumainer.model.SavedResume> items = resumeDao.findByUserId(userId, search, language,
                adaptationLevel, createdDate, dateFrom, dateTo, sortField, sortDir, page, size);
        long total = resumeDao.countByUserId(userId, search, language,
                adaptationLevel, createdDate, dateFrom, dateTo);

        List<HomeSavedResumeDto> dtos = items.stream()
                .map(r -> homeMapper.toDto(r, request))
                .collect(Collectors.toList());

        return new PagedResponse<>(dtos, page, size, total);
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
