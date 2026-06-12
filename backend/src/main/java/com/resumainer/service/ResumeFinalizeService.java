package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.dto.generate.ExportResultDto;
import com.resumainer.dto.generate.SavedResumeExportDto;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.model.ResumeGenerationResponse;
import com.resumainer.util.PublicCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Finalizes a generation request: renders HTML, saves to disk, and creates
 * saved_resume records. Handles file compensation on DB failure.
 */
@Service
public class ResumeFinalizeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeFinalizeService.class);

    private final DataSource dataSource;
    private final GenerationRequestDao requestDao;
    private final GenerationResponseDao responseDao;
    private final GenerationResponsePersonalDao personalDao;
    private final SavedResumeDao savedResumeDao;
    private final ResumeTemplateRenderer templateRenderer;
    private final GeneratedFileStorageService fileStorage;
    private final ProfilePromptDao profilePromptDao;

    public ResumeFinalizeService(DataSource dataSource,
                                  GenerationRequestDao requestDao,
                                  GenerationResponseDao responseDao,
                                  GenerationResponsePersonalDao personalDao,
                                  SavedResumeDao savedResumeDao,
                                  ResumeTemplateRenderer templateRenderer,
                                  GeneratedFileStorageService fileStorage,
                                  ProfilePromptDao profilePromptDao) {
        this.dataSource = dataSource;
        this.requestDao = requestDao;
        this.responseDao = responseDao;
        this.personalDao = personalDao;
        this.savedResumeDao = savedResumeDao;
        this.templateRenderer = templateRenderer;
        this.fileStorage = fileStorage;
        this.profilePromptDao = profilePromptDao;
    }

    /**
     * Finalizes a generation request: renders HTML, saves files, inserts saved_resume rows.
     *
     * @param requestId              the generation request ID
     * @param userId                 the authenticated user ID
     * @param selectedAdaptationLevel MINIMAL, BALANCED, or MAXIMUM
     * @return ExportResultDto with download URLs
     */
    public ExportResultDto finalizeRequest(UUID requestId, UUID userId, String selectedAdaptationLevel) {
        ResumeGenerationRequest request = requestDao.findById(requestId, userId);
        if (request == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }

        // Load all responses
        List<ResumeGenerationResponse> allResponses = responseDao.findResponsesByRequestId(requestId);

        // Filter by selected adaptation level
        long targetLevelId = mapAdaptationLevelId(selectedAdaptationLevel);
        List<ResumeGenerationResponse> toFinalize = allResponses.stream()
                .filter(r -> r.getAdaptationLevelId() == targetLevelId)
                .toList();

        if (toFinalize.isEmpty()) {
            throw new IllegalArgumentException("Selected adaptation level not found: " + selectedAdaptationLevel);
        }

        // Load profile education and username for rendering
        // Use full userId with hyphens replaced for safe path segments (unique, collision-avoidant)
        String username = request.getUserId().toString().replace("-", "_");
        List<Map<String, Object>> profileEducation = profilePromptDao.loadEducation(userId);

        // Track created files for compensation
        List<String> createdFiles = new ArrayList<>();
        List<Long> createdResumeIds = new ArrayList<>();

        try {
            ExportResultDto export = new ExportResultDto();
            List<SavedResumeExportDto> exportItems = new ArrayList<>();

            for (ResumeGenerationResponse response : toFinalize) {
                String languageCode = response.getLanguageId() == 1L ? "EN" : "RU";
                String adaptLower = selectedAdaptationLevel.toLowerCase();

                // 1. Render HTML
                GenerationResponseDao.ResponseBundle bundle = responseDao.loadResponseBundle(response.getId());
                // Use collision-safe code generation with uniqueness check
                String publicCode = PublicCodeGenerator.generateWithRetry(
                        code -> savedResumeDao.findPublicCodeByCode(code) == null);

                // 2. Save HTML to disk
                String htmlPath = templateRenderer.renderAndSave(
                        bundle, profileEducation,
                        languageCode, selectedAdaptationLevel,
                        username, publicCode);
                createdFiles.add(htmlPath);

                // 3. Prepare saved_resume data
                String resumeTitle = request.getVacancyTitle() != null ? request.getVacancyTitle() : "Resume";
                String vacancy = request.getVacancyTitle() != null ? request.getVacancyTitle() : "";
                String company = request.getCompanyName() != null ? request.getCompanyName() : "";

                // 4. Insert saved_resume row
                long savedId = savedResumeDao.insert(userId, resumeTitle, vacancy, company,
                        languageCode, selectedAdaptationLevel,
                        publicCode, "/candidate/" + publicCode,
                        htmlPath, null, // pdf_file_path = null in feat/007
                        requestId, response.getId(),
                        targetLevelId, response.getLanguageId());
                createdResumeIds.add(savedId);

                // 5. Build export DTO
                SavedResumeExportDto item = new SavedResumeExportDto();
                item.setSavedResumeId(savedId);
                item.setLanguageCode(languageCode);
                item.setAdaptationLevel(selectedAdaptationLevel);
                item.setHtmlDownloadUrl("/api/resumes/" + savedId + "/html");
                item.setPdfDownloadUrl("/api/resumes/" + savedId + "/pdf");
                item.setPdfOpenUrl("/candidate/" + publicCode);
                item.setPublicUrlLink("/candidate/" + publicCode);
                item.setPdfAvailable(false);
                item.setPdfMessage("PDF generation is not available yet. It will be available in a future update.");
                exportItems.add(item);
            }

            export.setResumes(exportItems);
            log.info("Finalized {} resumes for request: {}", exportItems.size(), requestId);
            return export;

        } catch (Exception e) {
            // File compensation: clean up orphaned files
            for (String filePath : createdFiles) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filePath));
                } catch (Exception ignored) {
                    log.warn("Failed to clean up orphaned file: {}", filePath);
                }
            }
            log.error("Finalization failed for request: {}", requestId, e);
            throw new RuntimeException("Failed to finalize resume. Please try again.", e);
        }
    }

    private long mapAdaptationLevelId(String level) {
        switch (level) {
            case "MINIMAL":  return 1L;
            case "BALANCED": return 2L;
            case "MAXIMUM":  return 3L;
            default: throw new IllegalArgumentException("Unknown adaptation level: " + level);
        }
    }
}
