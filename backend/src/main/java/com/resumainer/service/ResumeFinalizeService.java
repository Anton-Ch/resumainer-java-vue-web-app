package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.dto.generate.ExportResultDto;
import com.resumainer.dto.generate.SavedResumeExportDto;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.model.ResumeGenerationResponse;
import com.resumainer.model.pdf.*;
import com.resumainer.service.pdf.ResumeRenderDataBuilder;
import com.resumainer.util.PublicCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
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
    private final OpenHtmlPdfGenerationService pdfGenerationService;
    private final ResumeRenderDataBuilder renderDataBuilder;

    public ResumeFinalizeService(DataSource dataSource,
                                  GenerationRequestDao requestDao,
                                  GenerationResponseDao responseDao,
                                  GenerationResponsePersonalDao personalDao,
                                  SavedResumeDao savedResumeDao,
                                  ResumeTemplateRenderer templateRenderer,
                                  GeneratedFileStorageService fileStorage,
                                  ProfilePromptDao profilePromptDao,
                                  OpenHtmlPdfGenerationService pdfGenerationService,
                                  ResumeRenderDataBuilder renderDataBuilder) {
        this.dataSource = dataSource;
        this.requestDao = requestDao;
        this.responseDao = responseDao;
        this.personalDao = personalDao;
        this.savedResumeDao = savedResumeDao;
        this.templateRenderer = templateRenderer;
        this.fileStorage = fileStorage;
        this.profilePromptDao = profilePromptDao;
        this.pdfGenerationService = pdfGenerationService;
        this.renderDataBuilder = renderDataBuilder;
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

        // Collect unique adaptation levels present in responses
        Set<Long> availableLevelIds = allResponses.stream()
                .map(ResumeGenerationResponse::getAdaptationLevelId)
                .collect(java.util.stream.Collectors.toSet());

        // Determine target level: explicit selection, auto-detect for single level, or error
        final String effectiveLevel;
        if (selectedAdaptationLevel != null && !selectedAdaptationLevel.isBlank()) {
            long requestedId = mapAdaptationLevelId(selectedAdaptationLevel);
            if (!availableLevelIds.contains(requestedId)) {
                // If selected level is invalid and only one level exists, auto-select it
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
            // No level specified and only one available — auto-select it
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

        List<ResumeGenerationResponse> toFinalize = allResponses.stream()
                .filter(r -> r.getAdaptationLevelId() == targetLevelId)
                .toList();

        // Load profile education, username, and contact for rendering
        String username = request.getUserId().toString().replace("-", "_");
        List<Map<String, Object>> profileEducation = profilePromptDao.loadEducation(userId);
        Map<String, Object> contactData = profilePromptDao.loadContact(userId);

        // Track created files for compensation
        List<String> createdFiles = new ArrayList<>();
        List<Long> createdResumeIds = new ArrayList<>();

        try {
            ExportResultDto export = new ExportResultDto();
            List<SavedResumeExportDto> exportItems = new ArrayList<>();

            for (ResumeGenerationResponse response : toFinalize) {
                String languageCode = response.getLanguageId() == 1L ? "EN" : "RU";

                // 1. Render HTML
                GenerationResponseDao.ResponseBundle bundle = responseDao.loadResponseBundle(response.getId());
                // Use collision-safe code generation with uniqueness check
                String publicCode = PublicCodeGenerator.generateWithRetry(
                        code -> savedResumeDao.findPublicCodeByCode(code) == null);

                // 2. Save HTML to disk (legacy renderer)
                String htmlPath = templateRenderer.renderAndSave(
                        bundle, profileEducation, contactData,
                        languageCode, finalSelectedLevel,
                        username, publicCode);
                createdFiles.add(htmlPath);

                // Prepare saved_resume data
                String resumeTitle = request.getVacancyTitle() != null ? request.getVacancyTitle() : "Resume";
                String vacancy = request.getVacancyTitle() != null ? request.getVacancyTitle() : "";
                String company = request.getCompanyName() != null ? request.getCompanyName() : "";
                String coverLetter = response.getCoverLetter();

                // 3. Generate PDF via new pipeline (Feature 008)
                String pdfPath = null;
                Integer pdfPageCount = null;
                try {
                    ResumeRenderData renderData = buildRenderData(bundle, contactData,
                            profileEducation, languageCode, finalSelectedLevel);
                    int totalWork = bundle.experience != null ? bundle.experience.size() : 0;
                    int totalProjects = bundle.projects != null ? bundle.projects.size() : 0;
                    int totalCourses = bundle.courses != null ? bundle.courses.size() : 0;
                    PagePlan pagePlan = renderDataBuilder.buildPagePlan(totalWork, totalProjects, totalCourses);

                    File htmlFile = new File(htmlPath);
                    File outputDir = htmlFile.getParentFile();

                    OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                            pdfGenerationService.generate(renderData, pagePlan, outputDir, "resume_" + languageCode.toLowerCase());

                    if (pdfResult.isSuccess()) {
                        pdfPath = pdfResult.getPdfPath();
                        pdfPageCount = pdfResult.getPageCount();
                        createdFiles.add(pdfResult.getHtmlPath());
                        createdFiles.add(pdfPath);
                    }
                } catch (Exception e) {
                    log.warn("PDF generation skipped for {}: {}", languageCode, e.getMessage());
                }

                // 4. Insert saved_resume row
                long savedId = savedResumeDao.insert(userId, resumeTitle, vacancy, company,
                        languageCode, finalSelectedLevel,
                        publicCode, "/candidate/" + publicCode,
                        htmlPath, pdfPath, coverLetter,
                        requestId, response.getId(),
                        targetLevelId, response.getLanguageId());
                createdResumeIds.add(savedId);

                // 5. Update PDF metadata if generated
                if (pdfPath != null && pdfPageCount != null) {
                    savedResumeDao.updatePdfMetadata(savedId, "READY", pdfPath,
                            pdfPageCount, "default-v1", null, null);
                }

                // 6. Build export DTO
                SavedResumeExportDto item = new SavedResumeExportDto();
                item.setSavedResumeId(savedId);
                item.setLanguageCode(languageCode);
                item.setAdaptationLevel(finalSelectedLevel);
                item.setHtmlDownloadUrl("/api/generate/resumes/" + savedId + "/html");
                item.setPdfDownloadUrl("/api/generate/resumes/" + savedId + "/pdf");
                item.setPdfOpenUrl("/candidate/" + publicCode);
                item.setPublicUrlLink("/candidate/" + publicCode);
                item.setPdfAvailable(pdfPath != null);
                item.setPdfMessage(pdfPath == null ? "PDF generation failed. You can retry finalization." : null);
                item.setCoverLetter(coverLetter);
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

        // Contact data from profile
        if (contactData != null) {
            input.fullName = str(contactData.get("full_name"));
            input.phone = str(contactData.get("phone"));
            input.email = str(contactData.get("email"));
            input.location = str(contactData.get("location"));
            input.linkedin = str(contactData.get("linkedin"));
            input.portfolio = str(contactData.get("portfolio"));
            input.telegram = str(contactData.get("telegram"));
            input.whatsapp = str(contactData.get("whatsapp"));
        }

        // Education from profile
        if (education != null) {
            for (Map<String, Object> edu : education) {
                String line = str(edu.get("institution")) + " — " + str(edu.get("degree"));
                input.educationLines.add(line);
            }
        }

        // Work experience from bundle
        if (bundle.experience != null) {
            for (com.resumainer.model.GenerationResponseExperience exp : bundle.experience) {
                ResumeRenderData.RenderWorkItem w = new ResumeRenderData.RenderWorkItem();
                w.setJobTitle(exp.getJobTitle());
                w.setCompanyName(exp.getCompanyName());
                w.setDescription(exp.getDescription());
                w.setLocation(exp.getLocation());
                w.setFirstPage(exp.isFirstPage());
                // Bullet points loaded separately — placeholder for Phase 3 integration
                input.workItems.add(w);
            }
        }

        // Projects from bundle
        if (bundle.projects != null) {
            for (com.resumainer.model.GenerationResponseProject proj : bundle.projects) {
                ResumeRenderData.RenderProjectItem p = new ResumeRenderData.RenderProjectItem();
                p.setProjectName(proj.getProjectName());
                p.setRole(proj.getRole());
                p.setDescription(proj.getDescription());
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

        return renderDataBuilder.buildRenderData(input);
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
