package com.resumainer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses structured JSON from AI output into normalized response data.
 * Supports single-language and bilingual responses, single and all adaptation levels.
 */
@Service
public class AiResponseParser {

    private static final Logger log = LoggerFactory.getLogger(AiResponseParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** One parsed response variant = one language + one adaptation level + its data. */
    public static class ParsedVariant {
        public long languageId;         // 1=EN, 2=RU (matches language table)
        public String languageCode;     // EN, RU
        public String adaptationLevel;  // MINIMAL, BALANCED, MAXIMUM

        public String professionalTitle;
        public String valueLine;
        public String professionalSummary;
        public String professionalAspirations;
        public String coverLetter;

        public List<ExperienceItem> experience = new ArrayList<>();
        public List<CourseItem> courses = new ArrayList<>();
        public List<ProjectItem> projects = new ArrayList<>();
        public List<SkillItem> skills = new ArrayList<>();
        public PersonalInfoItem personalInfo;
    }

    public static class ExperienceItem {
        public String sourceId;
        public String jobTitle;
        public String companyName;
        public String description;
        public String location;
        public String startDate;
        public String endDate;
        public boolean isFirstPage = true;
        public List<String> bulletPoints = new ArrayList<>();
    }

    public static class CourseItem {
        public String sourceId;
        public String name;
        public String provider;
        public String courseFocus;
    }

    public static class ProjectItem {
        public String sourceId;
        public String projectName;
        public String role;
        public String description;
        public String startDate;
        public String endDate;
        public List<String> bulletPoints = new ArrayList<>();
    }

    public static class SkillItem {
        public String skillGroup; public String skillName;
    }

    public static class PersonalInfoItem {
        public String location; public String spokenLanguages;
        public String willingnessToRelocate; public String willingnessForBusinessTrips;
        public String citizenship; public String dateOfBirth;
        public List<String> workFormats;
    }

    /**
     * Parses the AI JSON response into a list of variants based on request settings.
     *
     * @param rawJson              the raw JSON string from the AI provider
     * @param languageMode         ENGLISH_ONLY, RUSSIAN_ONLY, or BILINGUAL
     * @param adaptationSelection  MINIMAL, BALANCED, MAXIMUM, or ALL
     * @return list of parsed variants
     * @throws IllegalArgumentException if the JSON is invalid or missing required fields
     */
    public List<ParsedVariant> parse(String rawJson, String languageMode, String adaptationSelection) {
        try {
            JsonNode root = MAPPER.readTree(rawJson);

            // Determine which languages and levels to extract
            List<String> languages = getLanguages(languageMode);
            List<String> levels = getLevels(adaptationSelection);

            List<ParsedVariant> results = new ArrayList<>();

            for (String lang : languages) {
                boolean isSingleLang = languages.size() == 1;

                for (String level : levels) {
                    boolean isSingleLevel = levels.size() == 1;
                    JsonNode variantNode = findVariantNode(root, lang, level, isSingleLang, isSingleLevel);

                    if (variantNode == null) {
                        throw new IllegalArgumentException(
                                "Missing data for " + lang + " / " + level);
                    }

                    ParsedVariant variant = new ParsedVariant();
                    variant.languageCode = lang;
                    variant.languageId = lang.equals("EN") ? 1L : 2L;
                    variant.adaptationLevel = level;

                    // Top-level fields
                    variant.professionalTitle = getString(variantNode, "professionalTitle", "professional_title");
                    variant.valueLine = getString(variantNode, "valueLine", "value_line");
                    variant.professionalSummary = getString(variantNode, "professionalSummary", "professional_summary");
                    variant.professionalAspirations = getString(variantNode, "professionalAspirations", "professional_aspirations");
                    variant.coverLetter = getString(variantNode, "coverLetter", "cover_letter");

                    // Validate required
                    validateRequiredText(variant.professionalTitle, "professionalTitle", lang, level);
                    validateRequiredText(variant.valueLine, "valueLine", lang, level);
                    validateRequiredText(variant.professionalSummary, "professionalSummary", lang, level);
                    validateRequiredText(variant.professionalAspirations, "professionalAspirations", lang, level);

                    // Child sections
                    variant.experience = parseExperience(variantNode);
                    variant.courses = parseCourses(variantNode);
                    variant.projects = parseProjects(variantNode);
                    variant.skills = parseSkills(variantNode);
                    variant.personalInfo = parsePersonalInfo(variantNode);

                    validateRequiredSections(variant, lang, level);

                    results.add(variant);
                }
            }

            if (results.isEmpty()) {
                throw new IllegalArgumentException("AI response contains no recognizable data");
            }

            return results;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to parse AI response. The AI returned an unexpected format. Please try again.");
        }
    }

    // --- Private helpers ---

    private JsonNode findVariantNode(JsonNode root, String lang, String level,
                                      boolean isSingleLang, boolean isSingleLevel) {
        // Try language-rooted structure: { "en": { "minimal": {...} } }
        String langLower = lang.toLowerCase();
        JsonNode langNode = root.get(langLower);
        if (langNode == null) langNode = root.get(lang);

        if (langNode == null) {
            // Accept flat structure if only one language expected
            if (isSingleLang) langNode = root;
            else return null;
        }

        if (isSingleLevel && isSingleLang) {
            // Both single — langNode may BE the variant directly
            if (looksLikeVariant(langNode)) return langNode;
        }

        String levelLower = level.toLowerCase();
        JsonNode levelNode = langNode.get(levelLower);
        if (levelNode == null) levelNode = langNode.get(level);

        if (levelNode == null && isSingleLevel && looksLikeVariant(langNode)) {
            return langNode;
        }

        return levelNode;
    }

    private boolean looksLikeVariant(JsonNode node) {
        if (node == null || !node.isObject()) return false;
        // Check for any of the expected variant keys in camelCase or snake_case
        return node.has("professionalTitle") || node.has("professional_title")
            || node.has("professionalSummary") || node.has("professional_summary")
            || node.has("workExperience") || node.has("work_experience")
            || node.has("skills")
            || node.has("courses")
            || node.has("projects")
            || node.has("personalInfo") || node.has("personal_info");
    }

    private List<String> getLanguages(String languageMode) {
        List<String> list = new ArrayList<>();
        switch (languageMode) {
            case "ENGLISH_ONLY": list.add("EN"); break;
            case "RUSSIAN_ONLY": list.add("RU"); break;
            case "BILINGUAL":    list.add("EN"); list.add("RU"); break;
            default: throw new IllegalArgumentException("Unknown language mode: " + languageMode);
        }
        return list;
    }

    private List<String> getLevels(String adaptationSelection) {
        List<String> list = new ArrayList<>();
        switch (adaptationSelection) {
            case "MINIMAL":  list.add("MINIMAL"); break;
            case "BALANCED": list.add("BALANCED"); break;
            case "MAXIMUM":  list.add("MAXIMUM"); break;
            case "ALL":      list.add("MINIMAL"); list.add("BALANCED"); list.add("MAXIMUM"); break;
            default: throw new IllegalArgumentException("Unknown adaptation selection: " + adaptationSelection);
        }
        return list;
    }

    private void validateRequiredText(String value, String fieldName, String lang, String level) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required field in " + lang + "/" + level + ": " + fieldName);
        }
    }

    private void validateRequiredSections(ParsedVariant variant, String lang, String level) {
        if (variant.experience == null || variant.experience.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required section in " + lang + "/" + level + ": workExperience");
        }

        for (ExperienceItem item : variant.experience) {
            validateRequiredText(item.sourceId, "workExperience[].sourceId", lang, level);
            validateRequiredText(item.jobTitle, "workExperience[].jobTitle", lang, level);
            validateRequiredText(item.companyName, "workExperience[].companyName", lang, level);
            validateRequiredText(item.description, "workExperience[].description", lang, level);
        }

        if (variant.skills == null || variant.skills.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required section in " + lang + "/" + level + ": skills");
        }

        for (SkillItem item : variant.skills) {
            validateRequiredText(item.skillGroup, "skills[].skillGroup", lang, level);
            validateRequiredText(item.skillName, "skills[].skillName", lang, level);
        }

        // personalInfo is profile-owned data. The AI may echo it, but generation must not fail
        // if the model omits personalInfo or any of its fields. Profile completeness belongs to
        // pre-generation validation, not AI response parsing.
    }

    private List<ExperienceItem> parseExperience(JsonNode variant) {
        List<ExperienceItem> list = new ArrayList<>();
        JsonNode arr = variant.get("workExperience");
        if (arr == null) arr = variant.get("work_experience");
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                ExperienceItem e = new ExperienceItem();
                e.sourceId = getString(item, "sourceId", "source_id");
                e.jobTitle = getString(item, "jobTitle", "job_title");
                e.companyName = getString(item, "companyName", "company_name");
                e.description = getString(item, "description");
                e.location = getString(item, "location");
                e.startDate = getString(item, "startDate", "start_date");
                e.endDate = getString(item, "endDate", "end_date");
                e.isFirstPage = getBoolean(item, "isFirstPage", "is_first_page", true);
                e.bulletPoints = parseBulletPoints(item);
                if (e.jobTitle != null) list.add(e);
            }
        }
        return list;
    }

    private List<CourseItem> parseCourses(JsonNode variant) {
        List<CourseItem> list = new ArrayList<>();
        JsonNode arr = variant.get("courses");
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                CourseItem c = new CourseItem();
                c.sourceId = getString(item, "sourceId", "source_id");
                c.name = getString(item, "name");
                c.provider = getString(item, "provider");
                c.courseFocus = getString(item, "courseFocus", "course_focus");
                if (c.name != null) list.add(c);
            }
        }
        return list;
    }

    private List<ProjectItem> parseProjects(JsonNode variant) {
        List<ProjectItem> list = new ArrayList<>();
        JsonNode arr = variant.get("projects");
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                ProjectItem p = new ProjectItem();
                p.sourceId = getString(item, "sourceId", "source_id");
                p.projectName = getString(item, "projectName", "project_name");
                p.role = getString(item, "role");
                p.description = getString(item, "description");
                p.startDate = getString(item, "startDate", "start_date");
                p.endDate = getString(item, "endDate", "end_date");
                p.bulletPoints = parseBulletPoints(item);
                if (p.projectName != null) list.add(p);
            }
        }
        return list;
    }

    private List<SkillItem> parseSkills(JsonNode variant) {
        List<SkillItem> list = new ArrayList<>();
        JsonNode arr = variant.get("skills");
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                SkillItem s = new SkillItem();
                // Standard format: {"skillGroup": "..", "skillName": ".."}
                s.skillGroup = getString(item, "skillGroup", "skill_group");
                s.skillName = getString(item, "skillName", "skill_name");

                // Prototype-compatible format: {"groupName": "..", "skills": ["..", ".."]}
                // Convert each skill in the array into individual SkillItem rows.
                if (s.skillName == null) {
                    String groupName = getString(item, "groupName");
                    JsonNode skillsArr = item.get("skills");
                    if (groupName != null && skillsArr != null && skillsArr.isArray()) {
                        for (JsonNode skillNode : skillsArr) {
                            SkillItem expanded = new SkillItem();
                            expanded.skillGroup = groupName;
                            expanded.skillName = skillNode.isTextual() ? skillNode.asText() : null;
                            if (expanded.skillName != null) list.add(expanded);
                        }
                        continue; // already added expanded items
                    }
                }

                if (s.skillGroup != null && s.skillName != null) list.add(s);
            }
        }
        return list;
    }

    private PersonalInfoItem parsePersonalInfo(JsonNode variant) {
        JsonNode pi = variant.get("personalInfo");
        if (pi == null) pi = variant.get("personal_info");
        if (pi == null || !pi.isObject()) return null;

        PersonalInfoItem p = new PersonalInfoItem();
        p.location = getString(pi, "location");
        p.spokenLanguages = getString(pi, "spokenLanguages", "spoken_languages");
        p.willingnessToRelocate = getString(pi, "willingnessToRelocate", "willingness_to_relocate");
        p.willingnessForBusinessTrips = getString(pi, "willingnessForBusinessTrips", "willingness_for_business_trips");
        p.citizenship = getString(pi, "citizenship");
        p.dateOfBirth = getString(pi, "dateOfBirth", "date_of_birth");

        // Work formats — list of strings
        JsonNode wf = pi.get("workFormats");
        if (wf == null) wf = pi.get("work_formats");
        if (wf != null && wf.isArray()) {
            p.workFormats = new ArrayList<>();
            for (JsonNode item : wf) p.workFormats.add(item.asText());
        }
        return p;
    }

    private String getString(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode field = node.get(key);
            if (field != null && field.isTextual()) return field.asText();
        }
        return null;
    }

    private boolean getBoolean(JsonNode node, String key1, String key2, boolean defaultValue) {
        JsonNode field = node.get(key1);
        if (field == null) field = node.get(key2);
        if (field != null && field.isBoolean()) return field.asBoolean();
        return defaultValue;
    }

    private List<String> parseBulletPoints(JsonNode item) {
        List<String> bullets = new ArrayList<>();
        JsonNode arr = item.get("bulletPoints");
        if (arr == null) arr = item.get("bullet_points");
        if (arr != null && arr.isArray()) {
            for (JsonNode bullet : arr) {
                if (bullet.isTextual()) {
                    bullets.add(bullet.asText());
                }
            }
        }
        return bullets;
    }
}
