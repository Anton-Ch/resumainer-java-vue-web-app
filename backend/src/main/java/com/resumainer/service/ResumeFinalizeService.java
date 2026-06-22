package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.dto.generate.ExportResultDto;
import com.resumainer.dto.generate.SavedResumeExportDto;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.model.ResumeGenerationResponse;
import com.resumainer.model.User;
import com.resumainer.model.pdf.*;
import com.resumainer.service.pdf.PagePlanBuilder;
import com.resumainer.service.pdf.ResumeRenderDataBuilder;
import com.resumainer.util.PublicCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;

/**
 * Finalizes a generation request using the OpenHTML/PDF parity pipeline
 * (Feature 008). Generates PDF-parity HTML + validated PDF, inserts
 * saved_resume records, and returns export DTO with download URLs.
 *
 * Legacy {@link ResumeTemplateRenderer} is no longer used in finalization.
 * FINALIZING status lock, JDBC transactions, and bilingual atomicity
 * are deferred to Phase 22B/22C.
 */
@Service
public class ResumeFinalizeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeFinalizeService.class);

    private static final String STORAGE_BASE = "generated_results";

    private final DataSource dataSource;
    private final GenerationRequestDao requestDao;
    private final GenerationResponseDao responseDao;
    private final SavedResumeDao savedResumeDao;
    private final GeneratedFileStorageService fileStorage;
    private final ProfilePromptDao profilePromptDao;
    private final UserDao userDao;
    private final OpenHtmlPdfGenerationService pdfGenerationService;
    private final ResumeRenderDataBuilder renderDataBuilder;
    private final PagePlanBuilder pagePlanBuilder;

    public ResumeFinalizeService(DataSource dataSource,
                                  GenerationRequestDao requestDao,
                                  GenerationResponseDao responseDao,
                                  SavedResumeDao savedResumeDao,
                                  GeneratedFileStorageService fileStorage,
                                  ProfilePromptDao profilePromptDao,
                                  UserDao userDao,
                                  OpenHtmlPdfGenerationService pdfGenerationService,
                                  ResumeRenderDataBuilder renderDataBuilder,
                                  PagePlanBuilder pagePlanBuilder) {
        this.dataSource = dataSource;
        this.requestDao = requestDao;
        this.responseDao = responseDao;
        this.savedResumeDao = savedResumeDao;
        this.fileStorage = fileStorage;
        this.profilePromptDao = profilePromptDao;
        this.userDao = userDao;
        this.pdfGenerationService = pdfGenerationService;
        this.renderDataBuilder = renderDataBuilder;
        this.pagePlanBuilder = pagePlanBuilder;
    }

    /**
     * Finalizes a generation request: generates PDF-parity HTML + PDF
     * through OpenHtmlPdfGenerationService, inserts saved_resume rows,
     * and returns export DTO with download URLs.
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

        // Phase 22B: Guard against concurrent finalization
        String currentStatus = request.getStatus();
        if ("finalizing".equals(currentStatus)) {
            throw new RuntimeException("Finalization already in progress. Please wait for it to complete.");
        }

        boolean locked = requestDao.tryMarkFinalizing(requestId, userId);
        if (!locked) {
            // Re-read: another request may have grabbed the lock
            ResumeGenerationRequest latest = requestDao.findById(requestId, userId);
            if (latest != null && "finalizing".equals(latest.getStatus())) {
                throw new RuntimeException("Finalization already in progress. Please wait for it to complete.");
            }
            throw new RuntimeException("Generation request is not ready for finalization. Current status: "
                    + (latest != null ? latest.getStatus() : "unknown"));
        }

        log.info("FINALIZATION_LOCK_ACQUIRED requestId={} userId={}", requestId, userId);

        // Load all responses
        List<ResumeGenerationResponse> allResponses = responseDao.findResponsesByRequestId(requestId);

        // Collect unique adaptation levels present in responses
        Set<Long> availableLevelIds = allResponses.stream()
                .map(ResumeGenerationResponse::getAdaptationLevelId)
                .collect(java.util.stream.Collectors.toSet());

        // Determine target level: explicit selection, auto-detect for single level, or error
        final String effectiveLevel;
        if (selectedAdaptationLevel != null && !selectedAdaptationLevel.isBlank()) {
            long requestedId = mapAdaptationLevelId(selectedAdaptationLevel);
            if (!availableLevelIds.contains(requestedId)) {
                if (availableLevelIds.size() == 1) {
                    long singleLevelId = availableLevelIds.iterator().next();
                    effectiveLevel = adaptationLevelName(singleLevelId);
                    log.info("Selected level '{}' not found — falling back to only available level: {}",
                            selectedAdaptationLevel, effectiveLevel);
                } else {
                    List<String> availableNames = availableLevelIds.stream()
                            .map(this::adaptationLevelName)
                            .sorted()
                            .toList();
                    throw new IllegalArgumentException(
                            "Selected adaptation level not found: " + selectedAdaptationLevel
                            + ". Available levels: " + String.join(", ", availableNames));
                }
            } else {
                effectiveLevel = selectedAdaptationLevel;
            }
        } else if (availableLevelIds.size() == 1) {
            effectiveLevel = adaptationLevelName(availableLevelIds.iterator().next());
            log.info("No adaptation level selected — auto-selecting single available level: {}",
                    effectiveLevel);
        } else {
            List<String> availableNames = availableLevelIds.stream()
                    .map(this::adaptationLevelName)
                    .sorted()
                    .toList();
            throw new IllegalArgumentException(
                    "No adaptation level selected. Available levels: "
                    + String.join(", ", availableNames));
        }

        final long targetLevelId = mapAdaptationLevelId(effectiveLevel);
        final String finalSelectedLevel = effectiveLevel;

        log.info("FINALIZATION_START requestId={} userId={} level={} languageMode={}",
                requestId, userId, finalSelectedLevel, request.getLanguageMode());

        List<ResumeGenerationResponse> toFinalize = allResponses.stream()
                .filter(r -> r.getAdaptationLevelId() == targetLevelId)
                .toList();

        // Load profile education, username, and contact for rendering
        String fileStorageUsername = request.getUserId().toString().replace("-", "_");
        String realUsername = loadUsername(userId);
        List<Map<String, Object>> profileEducation = profilePromptDao.loadEducation(userId);
        Map<String, Object> contactData = profilePromptDao.loadContact(userId);

        // Track created files for cleanup on failure
        List<String> createdFiles = new ArrayList<>();

        // Stage 1: Generate all artifacts — no DB writes
        List<Artifact> artifacts = new ArrayList<>();
        try {
            for (ResumeGenerationResponse response : toFinalize) {
                String languageCode = response.getLanguageId() == 1L ? "EN" : "RU";

                // 1. Load response data
                GenerationResponseDao.ResponseBundle bundle = responseDao.loadResponseBundle(response.getId());
                String publicCode = PublicCodeGenerator.generateWithRetry(
                        code -> savedResumeDao.findPublicCodeByCode(code) == null);

                // 2. Build render data and page plan
                ResumeRenderData renderData = buildRenderData(bundle, contactData,
                        profileEducation, languageCode, finalSelectedLevel);
                int totalWork = bundle.experience != null ? bundle.experience.size() : 0;
                int totalProjects = bundle.projects != null ? bundle.projects.size() : 0;
                int totalCourses = bundle.courses != null ? bundle.courses.size() : 0;
                PagePlan pagePlan = pagePlanBuilder.build(totalWork, totalProjects, totalCourses);

                // 3. Determine output directory
                String baseFileName = "resume_" + languageCode.toLowerCase();
                Path outputDir = Paths.get(STORAGE_BASE, fileStorageUsername, publicCode);
                try {
                    Files.createDirectories(outputDir);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create output directory for finalization", e);
                }

                // 4. Generate PDF-parity HTML + PDF through the unified pipeline
                log.debug("Calling pdfGenerationService.generate() for lang={}", languageCode);
                OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                        pdfGenerationService.generate(renderData, pagePlan, outputDir.toFile(), baseFileName);

                if (!pdfResult.isSuccess()) {
                    throw new RuntimeException("PDF generation failed: " + pdfResult.getErrorReason());
                }

                String htmlPath = pdfResult.getHtmlPath();
                String pdfPath = pdfResult.getPdfPath();
                int pdfPageCount = pdfResult.getPageCount();

                createdFiles.add(htmlPath);
                createdFiles.add(pdfPath);

                log.info("PDF_GENERATED requestId={} lang={} pages={}",
                        requestId, languageCode, pdfPageCount);

                // Collect artifact data — no DB writes yet
                Artifact artifact = new Artifact();
                artifact.response = response;
                artifact.languageCode = languageCode;
                artifact.publicCode = publicCode;
                artifact.publicUrl = "/" + realUsername + "/" + publicCode;
                artifact.htmlPath = htmlPath;
                artifact.pdfPath = pdfPath;
                artifact.pdfPageCount = pdfPageCount;
                artifact.coverLetter = response.getCoverLetter();
                artifact.resumeTitle = request.getVacancyTitle() != null ? request.getVacancyTitle() : "Resume";
                artifact.vacancy = request.getVacancyTitle() != null ? request.getVacancyTitle() : "";
                artifact.company = request.getCompanyName() != null ? request.getCompanyName() : "";
                artifacts.add(artifact);
            }

            // Stage 2: Single JDBC transaction for all DB writes
            return persistArtifactsInTransaction(requestId, userId, artifacts,
                    finalSelectedLevel, targetLevelId, createdFiles, realUsername);

        } catch (Exception e) {
            // File compensation: clean up generated files on failure
            for (String filePath : createdFiles) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filePath));
                } catch (Exception ignored) {
                    log.warn("Failed to clean up created file: {}", filePath);
                }
            }

            // Phase 22B: Restore request status on failure so user can retry
            safeRestoreStatus(requestId, userId);

            log.error("Finalization failed for request: {}", requestId, e);
            throw new RuntimeException("Failed to finalize resume. Please try again.", e);
        }
    }

    // ── Stage 2: Single JDBC transaction for all DB writes ───────────────

    /**
     * Persists all artifacts inside a single JDBC transaction.
     * Uses the project's custom connection pool via injected DataSource.
     */
    private ExportResultDto persistArtifactsInTransaction(UUID requestId, UUID userId,
                                                           List<Artifact> artifacts,
                                                           String adaptationLevel, long targetLevelId,
                                                           List<String> createdFiles,
                                                           String realUsername) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            ExportResultDto export = new ExportResultDto();
            List<SavedResumeExportDto> exportItems = new ArrayList<>();

            for (Artifact a : artifacts) {
                long savedId = savedResumeDao.insert(conn,
                        userId, a.resumeTitle, a.vacancy, a.company,
                        a.languageCode, adaptationLevel,
                        a.publicCode, a.publicUrl,
                        a.htmlPath, a.pdfPath, a.coverLetter,
                        requestId, a.response.getId(),
                        targetLevelId, a.response.getLanguageId());

                savedResumeDao.updatePdfMetadata(conn, savedId, "READY", a.pdfPath,
                        a.pdfPageCount, "default-v1", null, null);

                SavedResumeExportDto item = new SavedResumeExportDto();
                item.setSavedResumeId(savedId);
                item.setLanguageCode(a.languageCode);
                item.setAdaptationLevel(adaptationLevel);
                item.setHtmlDownloadUrl("/api/generate/resumes/" + savedId + "/html");
                item.setPdfDownloadUrl("/api/generate/resumes/" + savedId + "/pdf");
                item.setPdfOpenUrl("/api/generate/resumes/" + savedId + "/pdf?disposition=inline");
                item.setPublicUrlLink(a.publicUrl);
                item.setPdfAvailable(true);
                item.setPdfMessage(null);
                item.setCoverLetter(a.coverLetter);
                exportItems.add(item);
            }

            // Phase 22B: Restore request status inside transaction
            requestDao.updateStatus(conn, requestId, userId, "completed", null, false);

            conn.commit();
            log.info("TRANSACTION_COMMITTED requestId={} resumes={}", requestId, artifacts.size());

            export.setResumes(exportItems);
            return export;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    log.warn("TRANSACTION_ROLLED_BACK requestId={}", requestId);
                } catch (Exception rollbackEx) {
                    log.error("Rollback failed for request: {}", requestId, rollbackEx);
                }
            }
            throw new RuntimeException("Database error during finalization", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception autoCommitEx) {
                    log.warn("Failed to restore autoCommit for request: {}", requestId, autoCommitEx);
                }

                try {
                    conn.close(); // returns to custom pool through proxy
                } catch (Exception closeEx) {
                    log.warn("Connection close error for request: {}", requestId, closeEx);
                }
            }
        }
    }

    /**
     * Restore generation request status to completed after finalization.
     * Does not update completed_at (completed=false).
     * Failures are logged but do not hide the original error.
     */
    private void safeRestoreStatus(UUID requestId, UUID userId) {
        try {
            requestDao.updateStatus(requestId, userId, "completed", null, false);
            log.debug("Request status restored to completed: requestId={}", requestId);
        } catch (Exception e) {
            log.warn("Failed to restore request status after finalization: requestId={}", requestId, e);
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

    private String adaptationLevelName(long levelId) {
        switch ((int) levelId) {
            case 1:  return "MINIMAL";
            case 2:  return "BALANCED";
            case 3:  return "MAXIMUM";
            default: return "UNKNOWN";
        }
    }

    /** Build ResumeRenderData from response bundle + profile data (Feature 008). */
    private ResumeRenderData buildRenderData(GenerationResponseDao.ResponseBundle bundle,
                                              Map<String, Object> contactData,
                                              List<Map<String, Object>> education,
                                              String languageCode, String adaptationLevel) {
        ResumeRenderDataBuilder.RenderDataInput input = new ResumeRenderDataBuilder.RenderDataInput();
        input.languageCode = languageCode;
        input.professionalTitle = bundle.response.getProfessionalTitle();
        input.professionalSummary = bundle.response.getProfessionalSummary();
        input.professionalAspirations = bundle.response.getProfessionalAspirations();
        input.valueLine = bundle.response.getValueLine();
        input.coverLetter = bundle.response.getCoverLetter();

        // Contact data from profile.
        // ProfilePromptDao.loadContact() uses camelCase keys, but keep snake_case fallback for legacy safety.
        if (contactData != null) {
            input.fullName = firstText(contactData, "fullName", "full_name");
            input.phone = firstText(contactData, "phone");
            input.email = firstText(contactData, "resumeEmail", "email");
            input.location = firstText(contactData, "location");
            input.linkedin = firstText(contactData, "linkedinUrl", "linkedin");
            input.portfolio = firstText(contactData, "portfolioUrl", "portfolio");
            input.telegram = firstText(contactData, "telegram");
            input.whatsapp = firstText(contactData, "whatsapp");
        }

        // Education from profile.
        // ProfilePromptDao.loadEducation() returns bilingual camelCase keys.
        if (education != null) {
            for (Map<String, Object> edu : education) {
                String line = buildEducationLine(edu, languageCode);
                if (hasText(line)) {
                    input.educationLines.add(line);
                }
            }
        }

        // Work experience from generated response bundle.
        // Bullets are loaded by GenerationResponseDao.loadResponseBundle().
        if (bundle.experience != null) {
            for (com.resumainer.model.GenerationResponseExperience exp : bundle.experience) {
                ResumeRenderData.RenderWorkItem w = new ResumeRenderData.RenderWorkItem();
                w.setJobTitle(exp.getJobTitle());
                w.setCompanyName(exp.getCompanyName());
                w.setDescription(exp.getDescription());
                w.setLocation(exp.getLocation());
                w.setDateRange(formatDateRange(exp.getStartDate(), exp.getEndDate(), languageCode));
                w.setFirstPage(exp.isFirstPage());
                w.setBulletPoints(experienceBulletTexts(exp));
                input.workItems.add(w);
            }
        }

        // Projects from generated response bundle.
        // Bullets are loaded by GenerationResponseDao.loadResponseBundle().
        if (bundle.projects != null) {
            for (com.resumainer.model.GenerationResponseProject proj : bundle.projects) {
                ResumeRenderData.RenderProjectItem p = new ResumeRenderData.RenderProjectItem();
                p.setProjectName(proj.getProjectName());
                p.setRole(proj.getRole());
                p.setDescription(proj.getDescription());
                p.setDateRange(formatDateRange(proj.getStartDate(), proj.getEndDate(), languageCode));
                p.setBulletPoints(projectBulletTexts(proj));
                input.projectItems.add(p);
            }
        }

        // Courses from bundle
        if (bundle.courses != null) {
            for (com.resumainer.model.GenerationResponseCourse crs : bundle.courses) {
                ResumeRenderData.RenderCourseItem c = new ResumeRenderData.RenderCourseItem();
                c.setName(crs.getName());
                c.setProvider(crs.getProvider());
                c.setCourseFocus(crs.getCourseFocus());
                input.courseItems.add(c);
            }
        }

        // Skills from bundle
        if (bundle.skills != null) {
            Map<String, List<String>> groups = new LinkedHashMap<>();
            for (com.resumainer.model.GenerationResponseSkill sk : bundle.skills) {
                groups.computeIfAbsent(sk.getSkillGroup(), k -> new ArrayList<>())
                        .add(sk.getSkillName());
            }
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
                ResumeRenderData.RenderSkillGroup sg = new ResumeRenderData.RenderSkillGroup();
                sg.setGroupName(entry.getKey());
                sg.setSkills(entry.getValue());
                input.skillGroups.add(sg);
            }
        }

        log.info("RENDER_DATA_SEMANTICS lang={} fullNamePresent={} emailPresent={} educationLines={} workItems={} workBullets={} projects={} projectBullets={} courses={} skills={}",
                languageCode,
                hasText(input.fullName),
                hasText(input.email),
                input.educationLines.size(),
                input.workItems.size(),
                countWorkBullets(input.workItems),
                input.projectItems.size(),
                countProjectBullets(input.projectItems),
                input.courseItems.size(),
                input.skillGroups.size());

        return renderDataBuilder.buildRenderData(input);
    }

    private String buildEducationLine(Map<String, Object> edu, String languageCode) {
        if (edu == null || edu.isEmpty()) return "";

        boolean ru = "RU".equalsIgnoreCase(languageCode);

        String institution = ru
                ? firstText(edu, "institutionNameRu", "institutionNameEn", "institution", "institutionName")
                : firstText(edu, "institutionNameEn", "institutionNameRu", "institution", "institutionName");
        String degree = ru
                ? firstText(edu, "degreeRu", "degreeEn", "degree")
                : firstText(edu, "degreeEn", "degreeRu", "degree");
        String field = ru
                ? firstText(edu, "fieldOfStudyRu", "fieldOfStudyEn", "fieldOfStudy", "field")
                : firstText(edu, "fieldOfStudyEn", "fieldOfStudyRu", "fieldOfStudy", "field");

        List<String> leftParts = new ArrayList<>();
        if (hasText(degree)) leftParts.add(degree);
        if (hasText(field)) leftParts.add(field);

        String left = String.join(": ", leftParts);

        if (hasText(left) && hasText(institution)) {
            return left + " | " + institution;
        }
        if (hasText(institution)) return institution;
        return left;
    }

    private List<String> experienceBulletTexts(com.resumainer.model.GenerationResponseExperience exp) {
        if (exp == null || exp.getBullets() == null || exp.getBullets().isEmpty()) return List.of();

        return exp.getBullets().stream()
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt(
                        com.resumainer.model.GenerationResponseExperienceBullet::getBulletOrder))
                .map(com.resumainer.model.GenerationResponseExperienceBullet::getBulletText)
                .filter(this::hasText)
                .toList();
    }

    private List<String> projectBulletTexts(com.resumainer.model.GenerationResponseProject project) {
        if (project == null || project.getBullets() == null || project.getBullets().isEmpty()) return List.of();

        return project.getBullets().stream()
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt(
                        com.resumainer.model.GenerationResponseProjectBullet::getBulletOrder))
                .map(com.resumainer.model.GenerationResponseProjectBullet::getBulletText)
                .filter(this::hasText)
                .toList();
    }

    private String formatDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate, String languageCode) {
        if (startDate == null && endDate == null) return "";

        String start = formatYearMonth(startDate);
        String end = endDate != null
                ? formatYearMonth(endDate)
                : ("RU".equalsIgnoreCase(languageCode) ? "по настоящее время" : "till now");

        if (!hasText(start)) return end;
        if (!hasText(end)) return start;
        return start + " - " + end;
    }

    private String formatYearMonth(java.time.LocalDate value) {
        if (value == null) return "";
        return String.format(java.util.Locale.ROOT, "%04d-%02d", value.getYear(), value.getMonthValue());
    }

    private String firstText(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) return "";

        for (String key : keys) {
            if (key == null) continue;
            Object value = map.get(key);
            if (value != null && hasText(value.toString())) {
                return value.toString().trim();
            }
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int countWorkBullets(List<ResumeRenderData.RenderWorkItem> items) {
        if (items == null) return 0;
        return items.stream()
                .filter(java.util.Objects::nonNull)
                .map(ResumeRenderData.RenderWorkItem::getBulletPoints)
                .filter(java.util.Objects::nonNull)
                .mapToInt(List::size)
                .sum();
    }

    private int countProjectBullets(List<ResumeRenderData.RenderProjectItem> items) {
        if (items == null) return 0;
        return items.stream()
                .filter(java.util.Objects::nonNull)
                .map(ResumeRenderData.RenderProjectItem::getBulletPoints)
                .filter(java.util.Objects::nonNull)
                .mapToInt(List::size)
                .sum();
    }

    private String loadUsername(UUID userId) {
        try {
            User user = userDao.findById(userId);
            if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
                return user.getUsername();
            }
        } catch (Exception e) {
            log.warn("Failed to load username for userId={}", userId);
        }
        return userId.toString().replace("-", "_");
    }

    // ── Internal holder for generated artifact data before DB persistence ──

    private static class Artifact {
        ResumeGenerationResponse response;
        String languageCode;
        String publicCode;
        String publicUrl;
        String htmlPath;
        String pdfPath;
        int pdfPageCount;
        String coverLetter;
        String resumeTitle;
        String vacancy;
        String company;
    }
}
