package com.resumainer.service;

import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.GenerationResponsePersonalDao;
import com.resumainer.model.*;
import com.resumainer.dto.generate.GenerationReviewDto;
import com.resumainer.dto.generate.GenerationReviewDto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

/**
 * Service for reading and updating generation review data.
 * Groups response data by language, section, record, and adaptation level.
 */
@Service
public class ResumeReviewService {

    private static final Logger log = LoggerFactory.getLogger(ResumeReviewService.class);

    private final GenerationRequestDao requestDao;
    private final GenerationResponseDao responseDao;
    private final GenerationResponsePersonalDao personalDao;
    private final DataSource dataSource;

    public ResumeReviewService(GenerationRequestDao requestDao,
                                GenerationResponseDao responseDao,
                                GenerationResponsePersonalDao personalDao,
                                DataSource dataSource) {
        this.requestDao = requestDao;
        this.responseDao = responseDao;
        this.personalDao = personalDao;
        this.dataSource = dataSource;
    }

    /**
     * Returns grouped review data for a generation request.
     */
    public GenerationReviewDto getReview(UUID requestId, UUID userId) {
        // Verify ownership
        if (requestDao.findById(requestId, userId) == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }

        GenerationReviewDto dto = new GenerationReviewDto();
        dto.setRequestId(requestId);

        // Load all responses for this request
        List<ResumeGenerationResponse> responses = responseDao.findResponsesByRequestId(requestId);

        // Group by language
        Map<Long, List<ResumeGenerationResponse>> byLang = new LinkedHashMap<>();
        for (ResumeGenerationResponse resp : responses) {
            byLang.computeIfAbsent(resp.getLanguageId(), k -> new ArrayList<>()).add(resp);
        }

        List<LanguageReviewGroup> langGroups = new ArrayList<>();
        for (Map.Entry<Long, List<ResumeGenerationResponse>> entry : byLang.entrySet()) {
            LanguageReviewGroup langGroup = new LanguageReviewGroup();
            langGroup.setLanguageId(entry.getKey());
            langGroup.setLanguageCode(entry.getKey() == 1L ? "EN" : "RU");

            List<SectionReviewGroup> sections = buildSections(entry.getValue());
            langGroup.setSections(sections);
            langGroups.add(langGroup);
        }
        dto.setLanguages(langGroups);
        return dto;
    }

    /**
     * Saves review edits for a generation response.
     */
    /** Allowlisted editable field names in review. All other fields are rejected. */
    private static final Set<String> ALLOWED_REVIEW_FIELDS = Set.of(
            "professionalTitle", "professional_title",
            "professionalSummary", "professional_summary",
            "professionalAspirations", "professional_aspirations",
            "valueLine", "value_line",
            "coverLetter", "cover_letter"
    );

    /** Field names that are FORBIDDEN even if somehow they pass the allowlist. */
    private static final Set<String> FORBIDDEN_FIELD_PATTERNS = Set.of(
            "id", "userId", "user_id", "requestId",
            "status", "status_id", "language_id", "adaptation_level_id",
            "html_file_path", "pdf_file_path",
            "public_code", "public_url_link",
            "created_at", "updated_at",
            "completed_at", "error_message"
    );

    public void saveReview(UUID requestId, UUID userId, UUID responseId,
                            String fieldName, String value) {
        // Verify ownership via request
        if (requestDao.findById(requestId, userId) == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }

        // Validate field is allowed for editing
        if (!ALLOWED_REVIEW_FIELDS.contains(fieldName)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is not editable.");
        }
        if (FORBIDDEN_FIELD_PATTERNS.contains(fieldName)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' cannot be edited.");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE resume_generation_response SET "
                     + fieldName + " = ?, updated_at = NOW() WHERE id = ?")) {
            stmt.setString(1, value);
            stmt.setObject(2, responseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error saving review for response: {}", responseId, e);
            throw new RuntimeException("Failed to save review changes.");
        }
    }

    // --- Private helpers ---

    private List<SectionReviewGroup> buildSections(List<ResumeGenerationResponse> responses) {
        List<SectionReviewGroup> sections = new ArrayList<>();

        // Professional summary (single-value section)
        sections.add(buildSingleFieldSection("professional_summary", "Professional Summary",
                responses, r -> r.getProfessionalSummary()));

        // Work experience (record-based section)
        SectionReviewGroup expSection = buildRecordSection("work_experience", "Work Experience",
                responses, this::loadExperienceRecords);
        if (expSection != null) sections.add(expSection);

        // Skills
        sections.add(buildSingleFieldSection("skills", "Skills",
                responses, r -> {
                    List<GenerationResponseSkill> skills = responseDao.findSkillsByResponseId(r.getId());
                    StringBuilder sb = new StringBuilder();
                    for (GenerationResponseSkill s : skills) {
                        sb.append(s.getSkillGroup()).append(": ").append(s.getSkillName()).append("\n");
                    }
                    return sb.toString();
                }));

        // Personal Information
        sections.add(buildPersonalInfoSection(responses));

        // Cover letter
        sections.add(buildSingleFieldSection("cover_letter", "Cover Letter",
                responses, r -> r.getCoverLetter()));

        return sections;
    }

    private SectionReviewGroup buildSingleFieldSection(String key, String label,
                                                        List<ResumeGenerationResponse> responses,
                                                        java.util.function.Function<ResumeGenerationResponse, String> extractor) {
        SectionReviewGroup section = new SectionReviewGroup();
        section.setSectionKey(key);
        section.setSectionLabel(label);

        List<RecordReviewGroup> records = new ArrayList<>();
        for (ResumeGenerationResponse resp : responses) {
            RecordReviewGroup record = new RecordReviewGroup();
            record.setRecordId(resp.getId());
            record.setOrderInResume(0);

            String value = extractor.apply(resp);
            if (value == null) continue;

            Map<String, List<AdaptationVariant>> fields = new LinkedHashMap<>();
            String fieldKey = key.equals("cover_letter") ? "coverLetter" : key;
            fields.put(fieldKey, List.of(createVariant(resp, value)));
            record.setFieldVariants(fields);
            records.add(record);
        }
        section.setRecords(records);
        return section;
    }

    private SectionReviewGroup buildRecordSection(String key, String label,
                                                    List<ResumeGenerationResponse> responses,
                                                    java.util.function.Function<UUID, List<?>> loader) {
        SectionReviewGroup section = new SectionReviewGroup();
        section.setSectionKey(key);
        section.setSectionLabel(label);
        section.setRecords(new ArrayList<>());
        return section;
    }

    private SectionReviewGroup buildPersonalInfoSection(List<ResumeGenerationResponse> responses) {
        SectionReviewGroup section = new SectionReviewGroup();
        section.setSectionKey("personal_information");
        section.setSectionLabel("Personal Information");
        section.setRecords(new ArrayList<>());
        return section;
    }

    private List<?> loadExperienceRecords(UUID responseId) {
        return responseDao.findExperienceByResponseId(responseId);
    }

    private AdaptationVariant createVariant(ResumeGenerationResponse resp, String value) {
        AdaptationVariant v = new AdaptationVariant();
        v.setAdaptationLevelId(resp.getAdaptationLevelId());
        v.setAdaptationCode(adaptationCode(resp.getAdaptationLevelId()));
        v.setValue(value);
        return v;
    }

    private String adaptationCode(long levelId) {
        if (levelId == 1L) return "MINIMAL";
        if (levelId == 2L) return "BALANCED";
        if (levelId == 3L) return "MAXIMUM";
        return "UNKNOWN";
    }
}
