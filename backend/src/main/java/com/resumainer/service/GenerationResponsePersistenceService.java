package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.model.*;
import com.resumainer.service.ai.AiClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Persists parsed AI responses within a single JDBC transaction.
 * Also enforces one-active-generation-per-user rule.
 * All persistence failures roll back the transaction.
 */
@Service
public class GenerationResponsePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(GenerationResponsePersistenceService.class);

    private final DataSource dataSource;
    private final GenerationResponseDao responseDao;
    private final GenerationResponsePersonalDao personalDao;
    private final GenerationRequestDao requestDao;
    private final AiUsageLogDao usageLogDao;

    private static final long DRAFT_STATUS_ID = 1L;
    private static final long EN_LANG_ID = 1L;
    private static final long RU_LANG_ID = 2L;

    public GenerationResponsePersistenceService(DataSource dataSource,
                                                  GenerationResponseDao responseDao,
                                                  GenerationResponsePersonalDao personalDao,
                                                  GenerationRequestDao requestDao,
                                                  AiUsageLogDao usageLogDao) {
        this.dataSource = dataSource;
        this.responseDao = responseDao;
        this.personalDao = personalDao;
        this.requestDao = requestDao;
        this.usageLogDao = usageLogDao;
    }

    /**
     * Persists all parsed variants for a generation request in a single transaction.
     *
     * @param requestId the generation request ID
     * @param userId    the authenticated user ID
     * @param variants  parsed response variants from AiResponseParser
     * @throws AiClientException       if one-active-generation check fails
     * @throws IllegalStateException   if transaction fails
     */
    public void persistResponses(UUID requestId, UUID userId, List<AiResponseParser.ParsedVariant> variants) {
        // T061: Check one active generation per user
        if (requestDao.hasProcessingRequest(userId)) {
            throw new AiClientException("Generation already in progress. Please wait for it to complete.");
        }

        // Update request to processing
        requestDao.updateStatus(requestId, userId, "processing", null, false);

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Insert each variant as a response row
            UUID[] responseIds = new UUID[variants.size()];
            for (int i = 0; i < variants.size(); i++) {
                AiResponseParser.ParsedVariant variant = variants.get(i);

                ResumeGenerationResponse response = new ResumeGenerationResponse();
                response.setGenerationRequestId(requestId);
                response.setLanguageId(mapLanguageId(variant.languageCode));
                response.setAdaptationLevelId(mapAdaptationLevelId(variant.adaptationLevel));
                response.setStatusId(DRAFT_STATUS_ID);
                response.setProfessionalTitle(variant.professionalTitle);
                response.setValueLine(variant.valueLine);
                response.setProfessionalSummary(variant.professionalSummary);
                response.setProfessionalAspirations(variant.professionalAspirations);
                response.setCoverLetter(variant.coverLetter);

                responseDao.insertResponse(response, conn);
                responseIds[i] = response.getId();

                // Insert child sections
                insertExperience(response.getId(), variant.experience, conn);
                insertCourses(response.getId(), variant.courses, conn);
                insertProjects(response.getId(), variant.projects, conn);
                insertSkills(response.getId(), variant.skills, conn);

                // Insert personal info
                if (variant.personalInfo != null) {
                    GenerationResponsePersonal personal = mapPersonalInfo(response.getId(), variant);
                    personalDao.insert(personal, conn);
                }
            }

            // Update request status to completed
            requestDao.updateStatus(requestId, userId, "completed", null, true);

            conn.commit();
            log.info("Persisted {} response variants for request: {}", variants.size(), requestId);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    log.warn("Rolled back transaction for request: {}", requestId);
                } catch (SQLException re) {
                    log.error("Rollback failed for request: {}", requestId, re);
                }
            }
            // Update request status to failed
            try {
                requestDao.updateStatus(requestId, userId, "failed",
                        "Generation failed. Please try again.", false);
            } catch (Exception ignore) {
                // Best-effort
            }
            if (e instanceof AiClientException) throw (AiClientException) e;
            throw new IllegalStateException("Failed to save generation results. Please try again.", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

    // --- Private helpers ---

    private void insertExperience(UUID responseId, List<AiResponseParser.ExperienceItem> items, Connection conn) throws SQLException {
        for (int i = 0; i < items.size(); i++) {
            AiResponseParser.ExperienceItem item = items.get(i);
            GenerationResponseExperience exp = new GenerationResponseExperience();
            exp.setResponseId(responseId);
            exp.setJobTitle(item.jobTitle);
            exp.setCompanyName(item.companyName);
            exp.setDescription(item.description);
            exp.setLocation(item.location);
            exp.setFirstPage(item.isFirstPage);
            exp.setStartDate(parseDate(item.startDate));
            exp.setEndDate(parseDate(item.endDate));
            exp.setOrderInResume(i);
            responseDao.insertExperience(exp, conn);
        }
    }

    private void insertCourses(UUID responseId, List<AiResponseParser.CourseItem> items, Connection conn) throws SQLException {
        for (int i = 0; i < items.size(); i++) {
            AiResponseParser.CourseItem item = items.get(i);
            GenerationResponseCourse course = new GenerationResponseCourse();
            course.setResponseId(responseId);
            course.setName(item.name);
            course.setProvider(item.provider);
            course.setFirstPage(i < 7); // Page 1: max 7 most relevant courses
            course.setCourseFocus(item.courseFocus);
            course.setOrderInResume(i);
            responseDao.insertCourse(course, conn);
        }
    }

    private void insertProjects(UUID responseId, List<AiResponseParser.ProjectItem> items, Connection conn) throws SQLException {
        for (int i = 0; i < items.size(); i++) {
            AiResponseParser.ProjectItem item = items.get(i);
            GenerationResponseProject project = new GenerationResponseProject();
            project.setResponseId(responseId);
            project.setProjectName(item.projectName);
            project.setRole(item.role);
            project.setDescription(item.description);
            project.setStartDate(parseDate(item.startDate));
            project.setEndDate(parseDate(item.endDate));
            project.setOrderInResume(i);
            responseDao.insertProject(project, conn);
        }
    }

    private void insertSkills(UUID responseId, List<AiResponseParser.SkillItem> items, Connection conn) throws SQLException {
        for (int i = 0; i < items.size(); i++) {
            AiResponseParser.SkillItem item = items.get(i);
            GenerationResponseSkill skill = new GenerationResponseSkill();
            skill.setResponseId(responseId);
            skill.setSkillGroup(item.skillGroup);
            skill.setSkillName(item.skillName);
            skill.setOrderInResume(i);
            responseDao.insertSkill(skill, conn);
        }
    }

    private GenerationResponsePersonal mapPersonalInfo(UUID responseId, AiResponseParser.ParsedVariant variant) {
        AiResponseParser.PersonalInfoItem pi = variant.personalInfo;
        GenerationResponsePersonal p = new GenerationResponsePersonal();
        p.setResponseId(responseId);
        p.setLocation(pi.location != null ? pi.location : "");
        p.setSpokenLanguages(pi.spokenLanguages != null ? pi.spokenLanguages : "");
        p.setWillingnessToRelocate(pi.willingnessToRelocate != null ? pi.willingnessToRelocate : "");
        p.setWillingnessForBusinessTrips(pi.willingnessForBusinessTrips != null ? pi.willingnessForBusinessTrips : "");
        p.setCitizenship(pi.citizenship != null ? pi.citizenship : "");
        p.setDateOfBirth(pi.dateOfBirth != null ? parseDate(pi.dateOfBirth) : LocalDate.now());
        p.setWorkFormats(pi.workFormats != null ? String.join(", ", pi.workFormats) : null);
        p.setGpaGrade(null);
        p.setOrderInResume(0);
        return p;
    }

    private long mapLanguageId(String code) {
        return "EN".equals(code) ? EN_LANG_ID : RU_LANG_ID;
    }

    private long mapAdaptationLevelId(String level) {
        switch (level) {
            case "MINIMAL":  return 1L;
            case "BALANCED": return 2L;
            case "MAXIMUM":  return 3L;
            default: throw new IllegalArgumentException("Unknown adaptation level: " + level);
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            // Handle YYYY-MM or YYYY-MM-DD
            if (dateStr.length() <= 7) {
                return LocalDate.parse(dateStr + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            log.warn("Cannot parse date: {}", dateStr);
            return null;
        }
    }
}
