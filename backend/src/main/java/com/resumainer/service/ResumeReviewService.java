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

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for reading and updating generation review data.
 * Groups response data by language, section, record, and adaptation level.
 * Save uses section-aware updateKey format: "sectionKey:recordId:fieldName:adaptationCode[:groupIdx]"
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

    // ────────────────────────────────────────────────────────────────────────────
    // GET review: build hierarchical DTO
    // ────────────────────────────────────────────────────────────────────────────

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

    // ────────────────────────────────────────────────────────────────────────────
    // Section builders
    // ────────────────────────────────────────────────────────────────────────────

    private List<SectionReviewGroup> buildSections(List<ResumeGenerationResponse> responses) {
        List<SectionReviewGroup> sections = new ArrayList<>();

        // 1. Professional Positioning (top-level response fields)
        sections.add(buildProfessionalPositioningSection(responses));

        // 2. Work Experience (child records)
        List<SectionReviewGroup> workExp = buildChildRecordsSection(
                "work_experience", "Work Experience",
                responses, this::loadExperienceRecords,
                (exp, resp) -> {
                    GenerationResponseExperience e = (GenerationResponseExperience) exp;
                    return List.of(
                            fieldVariant(e.getId(), "jobTitle", e.getJobTitle(), e.getResponseId(), resp, "work_experience"),
                            fieldVariant(e.getId(), "companyName", e.getCompanyName(), e.getResponseId(), resp, "work_experience"),
                            fieldVariant(e.getId(), "description", e.getDescription(), e.getResponseId(), resp, "work_experience")
                    );
                });
        if (workExp != null) {
            loadBulletsForSection(workExp, "work_experience", true);
            sections.addAll(workExp);
        }

        // 3. Courses (child records)
        List<SectionReviewGroup> courses = buildChildRecordsSection(
                "courses", "Courses & Certifications",
                responses, this::loadCourseRecords,
                (crs, resp) -> {
                    GenerationResponseCourse c = (GenerationResponseCourse) crs;
                    return List.of(
                            fieldVariant(c.getId(), "courseName", c.getName(), c.getResponseId(), resp, "courses"),
                            fieldVariant(c.getId(), "provider", c.getProvider(), c.getResponseId(), resp, "courses"),
                            fieldVariant(c.getId(), "courseFocus", c.getCourseFocus(), c.getResponseId(), resp, "courses")
                    );
                });
        if (courses != null) sections.addAll(courses);

        // 4. Projects (child records)
        List<SectionReviewGroup> projects = buildChildRecordsSection(
                "projects", "Projects & Volunteering",
                responses, this::loadProjectRecords,
                (prj, resp) -> {
                    GenerationResponseProject p = (GenerationResponseProject) prj;
                    return List.of(
                            fieldVariant(p.getId(), "projectName", p.getProjectName(), p.getResponseId(), resp, "projects"),
                            fieldVariant(p.getId(), "role", p.getRole(), p.getResponseId(), resp, "projects"),
                            fieldVariant(p.getId(), "description", p.getDescription(), p.getResponseId(), resp, "projects")
                    );
                });
        if (projects != null) {
            loadBulletsForSection(projects, "projects", true);
            sections.addAll(projects);
        }

        // 5. Skills (grouped by skill_group)
        sections.add(buildSkillsSection(responses));

        // 6. Personal Information (from generation_response_personal table)
        sections.add(buildPersonalInfoSection(responses));

        return sections;
    }

    // ─── Professional Positioning ──────────────────────────────────────────────

    private SectionReviewGroup buildProfessionalPositioningSection(List<ResumeGenerationResponse> responses) {
        SectionReviewGroup section = new SectionReviewGroup();
        section.setSectionKey("professional_positioning");
        section.setSectionLabel("Professional Positioning");
        section.setRecords(new ArrayList<>());

        for (ResumeGenerationResponse resp : responses) {
            RecordReviewGroup record = new RecordReviewGroup();
            record.setRecordId(resp.getId());
            record.setOrderInResume(0);

            Map<String, List<AdaptationVariant>> fields = new LinkedHashMap<>();
            // Each top-level field → AdaptationVariant with updateKey
            fields.put("professionalTitle", List.of(
                    createVariant(resp, "professional_positioning", resp.getId(), "professionalTitle", resp.getProfessionalTitle())));
            fields.put("valueLine", List.of(
                    createVariant(resp, "professional_positioning", resp.getId(), "valueLine", resp.getValueLine())));
            fields.put("professionalSummary", List.of(
                    createVariant(resp, "professional_positioning", resp.getId(), "professionalSummary", resp.getProfessionalSummary())));
            fields.put("professionalAspirations", List.of(
                    createVariant(resp, "professional_positioning", resp.getId(), "professionalAspirations", resp.getProfessionalAspirations())));
            if (resp.getCoverLetter() != null) {
                fields.put("coverLetter", List.of(
                        createVariant(resp, "professional_positioning", resp.getId(), "coverLetter", resp.getCoverLetter())));
            }
            record.setFieldVariants(fields);
            section.getRecords().add(record);
        }
        return section;
    }

    // ─── Child record sections (work_experience, courses, projects) ────────────

    @FunctionalInterface
    private interface ChildRecordLoader {
        List<?> load(UUID responseId);
    }

    @FunctionalInterface
    private interface FieldExtractor {
        List<Map.Entry<String, AdaptationVariant>> extract(Object record, ResumeGenerationResponse resp);
    }

    private List<SectionReviewGroup> buildChildRecordsSection(
            String sectionKey, String sectionLabel,
            List<ResumeGenerationResponse> responses,
            ChildRecordLoader loader,
            FieldExtractor extractor) {

        List<SectionReviewGroup> groups = new ArrayList<>();
        for (ResumeGenerationResponse resp : responses) {
            List<?> records = loader.load(resp.getId());
            if (records.isEmpty()) continue;

            SectionReviewGroup section = new SectionReviewGroup();
            section.setSectionKey(sectionKey);
            section.setSectionLabel(sectionLabel);

            List<RecordReviewGroup> recordGroups = new ArrayList<>();
            for (Object rec : records) {
                UUID pk = getRecordId(rec);
                RecordReviewGroup rg = new RecordReviewGroup();
                rg.setRecordId(pk);
                rg.setOrderInResume(getOrderInResume(rec));

                Map<String, List<AdaptationVariant>> fields = new LinkedHashMap<>();
                List<Map.Entry<String, AdaptationVariant>> fieldEntries = extractor.extract(rec, resp);
                for (Map.Entry<String, AdaptationVariant> entry : fieldEntries) {
                    fields.put(entry.getKey(), List.of(entry.getValue()));
                }
                rg.setFieldVariants(fields);
                recordGroups.add(rg);
            }
            section.setRecords(recordGroups);
            groups.add(section);
        }
        return groups.isEmpty() ? null : groups;
    }

    private List<?> loadExperienceRecords(UUID responseId) {
        return responseDao.findExperienceByResponseId(responseId);
    }

    private List<?> loadCourseRecords(UUID responseId) {
        return responseDao.findCoursesByResponseId(responseId);
    }

    private List<?> loadProjectRecords(UUID responseId) {
        return responseDao.findProjectsByResponseId(responseId);
    }

    private UUID getRecordId(Object record) {
        if (record instanceof GenerationResponseExperience) return ((GenerationResponseExperience) record).getId();
        if (record instanceof GenerationResponseCourse) return ((GenerationResponseCourse) record).getId();
        if (record instanceof GenerationResponseProject) return ((GenerationResponseProject) record).getId();
        throw new IllegalArgumentException("Unknown record type: " + record.getClass());
    }

    /** Load bullet points for each record in work_experience or projects sections (Feature 008). */
    private void loadBulletsForSection(List<SectionReviewGroup> sectionGroups, String sectionKey, boolean isExperience) {
        for (SectionReviewGroup section : sectionGroups) {
            if (section.getRecords() == null) continue;
            for (RecordReviewGroup record : section.getRecords()) {
                List<BulletReviewItem> bulletItems = new ArrayList<>();
                if (isExperience) {
                    List<GenerationResponseExperienceBullet> bullets = responseDao.findExperienceBullets(record.getRecordId());
                    for (GenerationResponseExperienceBullet b : bullets) {
                        BulletReviewItem item = new BulletReviewItem();
                        item.setBulletOrder(b.getBulletOrder());
                        item.setBulletText(b.getBulletText());
                        item.setEdited(b.isEdited());
                        item.setUpdateKey(sectionKey + ":" + record.getRecordId() + ":bulletPoints:" + b.getBulletOrder());
                        bulletItems.add(item);
                    }
                } else {
                    List<GenerationResponseProjectBullet> bullets = responseDao.findProjectBullets(record.getRecordId());
                    for (GenerationResponseProjectBullet b : bullets) {
                        BulletReviewItem item = new BulletReviewItem();
                        item.setBulletOrder(b.getBulletOrder());
                        item.setBulletText(b.getBulletText());
                        item.setEdited(b.isEdited());
                        item.setUpdateKey(sectionKey + ":" + record.getRecordId() + ":bulletPoints:" + b.getBulletOrder());
                        bulletItems.add(item);
                    }
                }
                record.setBullets(bulletItems);
            }
        }
    }

    private int getOrderInResume(Object record) {
        if (record instanceof GenerationResponseExperience) return ((GenerationResponseExperience) record).getOrderInResume();
        if (record instanceof GenerationResponseCourse) return ((GenerationResponseCourse) record).getOrderInResume();
        if (record instanceof GenerationResponseProject) return ((GenerationResponseProject) record).getOrderInResume();
        return 0;
    }

    private Map.Entry<String, AdaptationVariant> fieldVariant(UUID recordId, String fieldName, String value,
                                                                UUID responseId, ResumeGenerationResponse resp,
                                                                String sectionKey) {
        return new AbstractMap.SimpleEntry<>(fieldName,
                childVariant(recordId, fieldName, value, responseId, resp, sectionKey));
    }

    private AdaptationVariant childVariant(UUID recordId, String fieldName, String value,
                                            UUID responseId, ResumeGenerationResponse resp,
                                            String sectionKey) {
        AdaptationVariant v = new AdaptationVariant();
        v.setResponseId(responseId);
        v.setAdaptationLevelId(resp.getAdaptationLevelId());
        v.setAdaptationCode(adaptationCode(resp.getAdaptationLevelId()));
        v.setValue(value != null ? value : "");
        v.setUpdateKey(sectionKey + ":" + recordId + ":" + fieldName + ":" + v.getAdaptationCode());
        return v;
    }

    // ─── Skills (grouped by skill_group) ────────────────────────────────────────

    private SectionReviewGroup buildSkillsSection(List<ResumeGenerationResponse> responses) {
        SectionReviewGroup section = new SectionReviewGroup();
        section.setSectionKey("skills");
        section.setSectionLabel("Skills");
        section.setRecords(new ArrayList<>());

        for (ResumeGenerationResponse resp : responses) {
            List<GenerationResponseSkill> skills = responseDao.findSkillsByResponseId(resp.getId());
            if (skills.isEmpty()) continue;

            // Group by skill_group
            Map<String, List<String>> grouped = new LinkedHashMap<>();
            for (GenerationResponseSkill s : skills) {
                grouped.computeIfAbsent(s.getSkillGroup(), k -> new ArrayList<>()).add(s.getSkillName());
            }

            int groupIdx = 0;
            for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                RecordReviewGroup record = new RecordReviewGroup();
                String groupRecordId = resp.getId() + "~g" + groupIdx; // composite key for skills
                record.setRecordId(resp.getId()); // use response UUID as base
                record.setOrderInResume(groupIdx);

                Map<String, List<AdaptationVariant>> fields = new LinkedHashMap<>();
                String groupName = entry.getKey();
                String skillsCsv = String.join(", ", entry.getValue());

                AdaptationVariant groupNameVariant = createVariant(resp,
                        "skills", resp.getId(), "groupName", groupName);
                groupNameVariant.setUpdateKey("skills:" + resp.getId() + ":groupName:" + adaptationCode(resp.getAdaptationLevelId()) + ":" + groupIdx);

                AdaptationVariant skillsVariant = createVariant(resp,
                        "skills", resp.getId(), "skills", skillsCsv);
                skillsVariant.setUpdateKey("skills:" + resp.getId() + ":skills:" + adaptationCode(resp.getAdaptationLevelId()) + ":" + groupIdx);

                fields.put("groupName", List.of(groupNameVariant));
                fields.put("skills", List.of(skillsVariant));

                record.setFieldVariants(fields);
                section.getRecords().add(record);
                groupIdx++;
            }
        }
        return section;
    }

    // ─── Personal Information ──────────────────────────────────────────────────

    private SectionReviewGroup buildPersonalInfoSection(List<ResumeGenerationResponse> responses) {
        SectionReviewGroup section = new SectionReviewGroup();
        section.setSectionKey("personal_information");
        section.setSectionLabel("Personal Information");
        section.setRecords(new ArrayList<>());

        for (ResumeGenerationResponse resp : responses) {
            GenerationResponsePersonal personal = personalDao.findByResponseId(resp.getId());
            if (personal == null) continue;

            RecordReviewGroup record = new RecordReviewGroup();
            record.setRecordId(personal.getId());
            record.setOrderInResume(0);

            Map<String, List<AdaptationVariant>> fields = new LinkedHashMap<>();
            fields.put("location", List.of(
                    createVariant(resp, "personal_information", personal.getId(), "location", personal.getLocation())));
            fields.put("spokenLanguages", List.of(
                    createVariant(resp, "personal_information", personal.getId(), "spokenLanguages", personal.getSpokenLanguages())));
            fields.put("willingnessToRelocate", List.of(
                    createVariant(resp, "personal_information", personal.getId(), "willingnessToRelocate", personal.getWillingnessToRelocate())));
            fields.put("willingnessForBusinessTrips", List.of(
                    createVariant(resp, "personal_information", personal.getId(), "willingnessForBusinessTrips", personal.getWillingnessForBusinessTrips())));
            fields.put("citizenship", List.of(
                    createVariant(resp, "personal_information", personal.getId(), "citizenship", personal.getCitizenship())));
            fields.put("dateOfBirth", List.of(
                    createVariant(resp, "personal_information", personal.getId(), "dateOfBirth",
                            personal.getDateOfBirth() != null ? personal.getDateOfBirth().toString() : "")));
            if (personal.getWorkFormats() != null) {
                fields.put("workFormats", List.of(
                        createVariant(resp, "personal_information", personal.getId(), "workFormats", personal.getWorkFormats())));
            }

            record.setFieldVariants(fields);
            section.getRecords().add(record);
        }
        return section;
    }

    // ─── Variant creation (shared) ─────────────────────────────────────────────

    private AdaptationVariant createVariant(ResumeGenerationResponse resp,
                                             String sectionKey, UUID recordId,
                                             String fieldName, String value) {
        AdaptationVariant v = new AdaptationVariant();
        v.setResponseId(resp.getId());
        v.setAdaptationLevelId(resp.getAdaptationLevelId());
        v.setAdaptationCode(adaptationCode(resp.getAdaptationLevelId()));
        v.setValue(value != null ? value : "");
        v.setUpdateKey(sectionKey + ":" + recordId + ":" + fieldName + ":" + v.getAdaptationCode());
        return v;
    }

    private String adaptationCode(long levelId) {
        if (levelId == 1L) return "MINIMAL";
        if (levelId == 2L) return "BALANCED";
        if (levelId == 3L) return "MAXIMUM";
        return "UNKNOWN";
    }

    // ────────────────────────────────────────────────────────────────────────────
    // SAVE review: parse section-aware updateKey and dispatch to DAO
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Section-aware allowlist: which fields are editable in which section.
     * Key = sectionKey, Value = set of allowed frontend field names.
     */
    private static final Map<String, Set<String>> ALLOWED_REVIEW_FIELDS_BY_SECTION = Map.of(
            "professional_positioning", Set.of(
                    "professionalTitle", "valueLine", "professionalSummary",
                    "professionalAspirations", "coverLetter"
            ),
            "work_experience", Set.of(
                    "jobTitle", "companyName", "description", "bulletPoints"
            ),
            "courses", Set.of(
                    "courseName", "provider", "courseFocus"
            ),
            "projects", Set.of(
                    "projectName", "role", "description", "bulletPoints"
            ),
            "skills", Set.of(
                    "groupName", "skills"
            ),
            "personal_information", Set.of(
                    "location", "spokenLanguages", "willingnessToRelocate",
                    "willingnessForBusinessTrips", "citizenship", "dateOfBirth", "workFormats"
            )
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

    /**
     * Saves review edits. Parses updateKey format: "sectionKey:recordId:fieldName:adaptationCode[:groupIdx]"
     * For skills, the 5th part (groupIdx) is required.
     */
    public void saveReview(UUID requestId, UUID userId, String updateKey, String value) {
        // Verify ownership via request
        if (requestDao.findById(requestId, userId) == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }

        // Parse updateKey: sectionKey:recordId:fieldName:adaptationCode[:groupIdx]
        String[] parts = updateKey.split(":");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid updateKey format: " + updateKey);
        }

        String sectionKey = parts[0];
        String recordIdStr = parts[1];
        String fieldName = parts[2];
        String adaptationCode = parts[3];
        String groupIdx = (parts.length >= 5) ? parts[4] : null;

        // Validate section
        Set<String> allowedFields = ALLOWED_REVIEW_FIELDS_BY_SECTION.get(sectionKey);
        if (allowedFields == null) {
            throw new IllegalArgumentException("Unknown review section: " + sectionKey);
        }

        // Validate field
        if (!allowedFields.contains(fieldName)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is not editable in section '" + sectionKey + "'.");
        }
        if (FORBIDDEN_FIELD_PATTERNS.contains(fieldName)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' cannot be edited.");
        }

        // Dispatch to section-specific handler
        switch (sectionKey) {
            case "professional_positioning":
                saveResponseField(recordIdStr, fieldName, value);
                break;
            case "work_experience":
                if ("bulletPoints".equals(fieldName)) {
                    saveExperienceBullet(recordIdStr, adaptationCode, value);
                } else {
                    saveExperienceField(recordIdStr, fieldName, value);
                }
                break;
            case "courses":
                saveCourseField(recordIdStr, fieldName, value);
                break;
            case "projects":
                if ("bulletPoints".equals(fieldName)) {
                    saveProjectBullet(recordIdStr, adaptationCode, value);
                } else {
                    saveProjectField(recordIdStr, fieldName, value);
                }
                break;
            case "skills":
                saveSkillField(recordIdStr, fieldName, value, groupIdx, adaptationCode);
                break;
            case "personal_information":
                savePersonalField(recordIdStr, fieldName, value);
                break;
            default:
                throw new IllegalArgumentException("Unknown review section: " + sectionKey);
        }
    }

    // ─── Section-specific save handlers ────────────────────────────────────────

    private void saveResponseField(String responseIdStr, String fieldName, String value) {
        UUID responseId = UUID.fromString(responseIdStr);
        responseDao.updateResponseField(responseId, fieldName, value);
    }

    private void saveExperienceField(String experienceIdStr, String fieldName, String value) {
        UUID experienceId = UUID.fromString(experienceIdStr);
        responseDao.updateExperienceField(experienceId, fieldName, value);
    }

    private void saveExperienceBullet(String experienceIdStr, String bulletOrderStr, String value) {
        UUID experienceId = UUID.fromString(experienceIdStr);
        // Reject empty/whitespace-only bullet edits
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Bullet point text cannot be empty.");
        }
        int bulletOrder;
        try {
            bulletOrder = Integer.parseInt(bulletOrderStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bullet order: " + bulletOrderStr);
        }
        responseDao.updateExperienceBullet(experienceId, bulletOrder, value);
    }

    private void saveCourseField(String courseIdStr, String fieldName, String value) {
        UUID courseId = UUID.fromString(courseIdStr);
        responseDao.updateCourseField(courseId, fieldName, value);
    }

    private void saveProjectField(String projectIdStr, String fieldName, String value) {
        UUID projectId = UUID.fromString(projectIdStr);
        responseDao.updateProjectField(projectId, fieldName, value);
    }

    private void saveProjectBullet(String projectIdStr, String bulletOrderStr, String value) {
        UUID projectId = UUID.fromString(projectIdStr);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Bullet point text cannot be empty.");
        }
        int bulletOrder;
        try {
            bulletOrder = Integer.parseInt(bulletOrderStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bullet order: " + bulletOrderStr);
        }
        responseDao.updateProjectBullet(projectId, bulletOrder, value);
    }

    private void saveSkillField(String responseIdStr, String fieldName, String value,
                                 String groupIdxStr, String adaptationCode) {
        UUID responseId = UUID.fromString(responseIdStr);
        int groupIdx = Integer.parseInt(groupIdxStr);

        // Load current skills and determine the group
        List<GenerationResponseSkill> skills = responseDao.findSkillsByResponseId(responseId);

        // Find distinct groups in order
        List<String> groups = new ArrayList<>();
        for (GenerationResponseSkill s : skills) {
            if (!groups.contains(s.getSkillGroup())) {
                groups.add(s.getSkillGroup());
            }
        }

        if (groupIdx >= groups.size()) {
            throw new IllegalArgumentException("Skill group index " + groupIdx + " out of range.");
        }

        String oldGroupName = groups.get(groupIdx);

        if ("groupName".equals(fieldName)) {
            // Update group name for all skills in this group
            responseDao.updateSkillGroupName(responseId, oldGroupName, value);
        } else if ("skills".equals(fieldName)) {
            // Replace skills for this group: delete old + insert new
            // We need a connection for transaction
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                // Delete old skills for this group
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM generation_response_skill WHERE response_id = ? AND skill_group = ?")) {
                    stmt.setObject(1, responseId);
                    stmt.setString(2, oldGroupName);
                    stmt.executeUpdate();
                }
                // Insert new skills from CSV
                String[] skillNames = value.split(",");
                int order = 0;
                for (String skillName : skillNames) {
                    String trimmed = skillName.trim();
                    if (trimmed.isEmpty()) continue;
                    GenerationResponseSkill skill = new GenerationResponseSkill();
                    skill.setResponseId(responseId);
                    skill.setSkillGroup(oldGroupName);
                    skill.setSkillName(trimmed);
                    skill.setOrderInResume(order++);
                    responseDao.insertSkill(skill, conn);
                }
                conn.commit();
                log.debug("Updated skill group '{}' for response {} with {} skills",
                        oldGroupName, responseId, skillNames.length);
            } catch (SQLException e) {
                log.error("Error updating skills for response: {}", responseId, e);
                throw new RuntimeException("Failed to update skills.", e);
            }
        }
    }

    private void savePersonalField(String personalIdStr, String fieldName, String value) {
        // Load current personal info, update the specific field, save back
        UUID personalId = UUID.fromString(personalIdStr);

        // Need to find by personal ID, not response ID
        GenerationResponsePersonal personal;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM generation_response_personal WHERE id = ?")) {
            stmt.setObject(1, personalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Personal info record not found: " + personalId);
                personal = mapPersonalRow(rs);
            }
        } catch (SQLException e) {
            log.error("Error loading personal info: {}", personalId, e);
            throw new RuntimeException("Failed to load personal info.", e);
        }

        // Update the field (mapped from frontend name to setter)
        switch (fieldName) {
            case "location": personal.setLocation(value); break;
            case "spokenLanguages": personal.setSpokenLanguages(value); break;
            case "willingnessToRelocate": personal.setWillingnessToRelocate(value); break;
            case "willingnessForBusinessTrips": personal.setWillingnessForBusinessTrips(value); break;
            case "citizenship": personal.setCitizenship(value); break;
            case "dateOfBirth":
                try {
                    personal.setDateOfBirth(java.time.LocalDate.parse(value));
                } catch (java.time.format.DateTimeParseException e) {
                    log.warn("Invalid dateOfBirth format '{}' for personal id {} — skipping update", value, personalId);
                    return; // Skip this field update, don't crash the whole save
                }
                break;
            case "workFormats": personal.setWorkFormats(value); break;
            default: throw new IllegalArgumentException("Unknown personal info field: " + fieldName);
        }

        personalDao.update(personal);
    }

    private GenerationResponsePersonal mapPersonalRow(ResultSet rs) throws SQLException {
        GenerationResponsePersonal p = new GenerationResponsePersonal();
        p.setId((UUID) rs.getObject("id"));
        p.setResponseId((UUID) rs.getObject("response_id"));
        p.setLocation(rs.getString("location"));
        p.setSpokenLanguages(rs.getString("spoken_languages"));
        p.setWillingnessToRelocate(rs.getString("willingness_to_relocate"));
        p.setWillingnessForBusinessTrips(rs.getString("willingness_for_business_trips"));
        p.setCitizenship(rs.getString("citizenship"));
        p.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        p.setWorkFormats(rs.getString("work_formats"));
        p.setGpaGrade(rs.getString("gpa_grade"));
        p.setOrderInResume(rs.getInt("order_in_resume"));
        return p;
    }
}
