package com.resumainer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates parsed AI response variants using request/profile/budget context that AiResponseParser does not know.
 *
 * AiResponseParser validates pure JSON structure and context-free mandatory fields.
 * AiResponseValidator validates business rules that depend on the generation request,
 * profile payload, or active resume budget configuration.
 */
@Service
public class AiResponseValidator {

    private static final Logger log = LoggerFactory.getLogger(AiResponseValidator.class);

    private final ResumeBudgetConfigService budgetConfigService;
    private final WorkExperienceBudgetResolver workExperienceBudgetResolver;

    public AiResponseValidator(ResumeBudgetConfigService budgetConfigService) {
        this.budgetConfigService = budgetConfigService;
        this.workExperienceBudgetResolver = new WorkExperienceBudgetResolver(budgetConfigService);
    }

    /**
     * Validates parsed variants before they are persisted.
     *
     * Important design rule:
     * The validator does NOT require AI to return every source record from the profile.
     * Resume budget/relevance rules may select only a subset of work experience, courses, or projects.
     * The validator checks that returned items are valid, traceable back to source profile records,
     * and do not exceed active budget limits.
     *
     * @param variants           parsed AI response variants
     * @param includeCoverLetter whether the user requested cover letter generation
     * @param profilePayload     profile payload used to build the AI prompt
     * @throws IllegalArgumentException when the parsed AI response violates request/profile/budget rules
     */
    public void validate(List<AiResponseParser.ParsedVariant> variants,
                         boolean includeCoverLetter,
                         Map<String, Object> profilePayload) {
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("AI response validation failed: no parsed variants");
        }

        for (AiResponseParser.ParsedVariant variant : variants) {
            validateCoverLetter(variant, includeCoverLetter);
            validateSkillsBudget(variant);
            validateWorkExperienceSourceIds(variant, profilePayload);
            validateWorkExperienceBudget(variant, profilePayload);
            validateCourses(variant, profilePayload);
            validateProjects(variant, profilePayload);
            validateBulletPoints(variant);
        }
    }

    private void validateCoverLetter(AiResponseParser.ParsedVariant variant, boolean includeCoverLetter) {
        String context = variantContext(variant);

        if (includeCoverLetter && isBlank(variant.coverLetter)) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context + ": coverLetter is required");
        }

        if (!includeCoverLetter && !isBlank(variant.coverLetter)) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context + ": coverLetter must be null or blank when disabled");
        }
    }

    private void validateSkillsBudget(AiResponseParser.ParsedVariant variant) {
        String context = variantContext(variant);

        if (variant.skills == null || variant.skills.isEmpty()) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context + ": skills are required");
        }

        int minGroups = budgetConfigService.getSkillsGroups();
        int maxGroups = budgetConfigService.getSkillsGroupsMax();
        int minSkillsPerGroup = budgetConfigService.getSkillsPerGroup();
        int maxSkillsPerGroup = budgetConfigService.getSkillsPerGroupMax();

        Map<String, Integer> skillsByGroup = new LinkedHashMap<>();
        for (AiResponseParser.SkillItem skill : variant.skills) {
            validateRequiredText(skill.skillGroup, "skills[].skillGroup", context);
            validateRequiredText(skill.skillName, "skills[].skillName", context);

            String groupKey = skill.skillGroup.trim();
            skillsByGroup.put(groupKey, skillsByGroup.getOrDefault(groupKey, 0) + 1);
        }

        int groupCount = skillsByGroup.size();
        if (groupCount < minGroups) {
            log.warn("AI response budget warning in {}: skill group count is below budget minimum: {} < {}",
                    context, groupCount, minGroups);
        }
        if (groupCount > maxGroups) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": skill group count exceeds budget maximum: " + groupCount + " > " + maxGroups);
        }

        for (Map.Entry<String, Integer> entry : skillsByGroup.entrySet()) {
            String groupName = entry.getKey();
            int count = entry.getValue();
            if (count < minSkillsPerGroup) {
                log.warn("AI response budget warning in {}: skill group '{}' has too few skills: {} < {}",
                        context, groupName, count, minSkillsPerGroup);
            }
            if (count > maxSkillsPerGroup) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": skill group '" + groupName + "' has too many skills: "
                                + count + " > " + maxSkillsPerGroup);
            }
        }
    }

    private void validateWorkExperienceSourceIds(AiResponseParser.ParsedVariant variant,
                                                 Map<String, Object> profilePayload) {
        if (!hasSection(profilePayload, "workExperience")) return;

        Set<String> allowedIds = extractSourceIds(profilePayload, "workExperience");
        String context = variantContext(variant);

        if (allowedIds.isEmpty()) {
            if (variant.experience != null && !variant.experience.isEmpty()) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": workExperience returned but profile has no source workExperience records");
            }
            return;
        }

        Set<String> seen = new HashSet<>();
        for (AiResponseParser.ExperienceItem item : variant.experience) {
            validateRequiredText(item.sourceId, "workExperience[].sourceId", context);
            if (!allowedIds.contains(item.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": unknown workExperience sourceId: " + item.sourceId);
            }
            if (!seen.add(item.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": duplicate workExperience sourceId: " + item.sourceId);
            }
        }
    }

    private void validateWorkExperienceBudget(AiResponseParser.ParsedVariant variant,
                                              Map<String, Object> profilePayload) {
        if (!hasSection(profilePayload, "workExperience")) return;

        int totalProfileJobs = sectionSize(profilePayload, "workExperience");
        if (totalProfileJobs <= 0) return;

        int totalProfileCourses = sectionSize(profilePayload, "courses");
        int totalProfileProjects = sectionSize(profilePayload, "projects");

        WorkExperienceBudgetResolver.WorkExperienceBudget budget =
                workExperienceBudgetResolver.resolve(totalProfileJobs, totalProfileCourses, totalProfileProjects);

        String context = variantContext(variant);
        List<AiResponseParser.ExperienceItem> experience = variant.experience == null
                ? List.of()
                : variant.experience;

        int returnedCount = experience.size();
        if (returnedCount > budget.maxTotalJobs) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": workExperience count exceeds resolved budget maximum: "
                            + returnedCount + " > " + budget.maxTotalJobs
                            + " (" + budget.caseKey + ")");
        }

        int page1Count = 0;
        int page2Count = 0;
        for (AiResponseParser.ExperienceItem item : experience) {
            if (item.isFirstPage) {
                page1Count++;
            } else {
                page2Count++;
            }
        }

        String currentSourceId = findCurrentWorkExperienceSourceId(profilePayload);
        if (!isBlank(currentSourceId)) {
            boolean currentIncluded = false;
            for (AiResponseParser.ExperienceItem item : experience) {
                if (currentSourceId.equals(item.sourceId)) {
                    currentIncluded = true;
                    break;
                }
            }

            if (!currentIncluded) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": current workExperience sourceId must be included: " + currentSourceId);
            }

            AiResponseParser.ExperienceItem first = experience.isEmpty() ? null : experience.get(0);
            if (first == null || !currentSourceId.equals(first.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": current workExperience sourceId must be the first selected record: "
                                + currentSourceId);
            }

            if (!first.isFirstPage) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": current workExperience sourceId must have isFirstPage=true: "
                                + currentSourceId);
            }
        }

        if (page1Count > budget.targetPage1Jobs) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": Page 1 workExperience count exceeds resolved budget: "
                            + page1Count + " > " + budget.targetPage1Jobs
                            + " (" + budget.caseKey + ")");
        }

        if (page2Count > budget.targetPage2Jobs) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": Page 2 workExperience count exceeds resolved budget: "
                            + page2Count + " > " + budget.targetPage2Jobs
                            + " (" + budget.caseKey + ")");
        }
    }

    private void validateCourses(AiResponseParser.ParsedVariant variant,
                                 Map<String, Object> profilePayload) {
        if (!hasSection(profilePayload, "courses")) return;

        Set<String> allowedIds = extractSourceIds(profilePayload, "courses");
        String context = variantContext(variant);

        if (allowedIds.isEmpty()) {
            if (variant.courses != null && !variant.courses.isEmpty()) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": courses returned but profile has no source course records");
            }
            return;
        }

        if (variant.courses == null || variant.courses.isEmpty()) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": courses are required because profile has courses");
        }

        int maxCourses = budgetConfigService.getMaxCourses();
        if (variant.courses.size() > maxCourses) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": course count exceeds budget maximum: "
                            + variant.courses.size() + " > " + maxCourses);
        }

        Set<String> seen = new HashSet<>();
        for (AiResponseParser.CourseItem item : variant.courses) {
            validateRequiredText(item.sourceId, "courses[].sourceId", context);
            validateRequiredText(item.name, "courses[].name", context);
            validateRequiredText(item.provider, "courses[].provider", context);
            validateRequiredText(item.courseFocus, "courses[].courseFocus", context);

            if (!allowedIds.contains(item.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": unknown course sourceId: " + item.sourceId);
            }
            if (!seen.add(item.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": duplicate course sourceId: " + item.sourceId);
            }
        }
    }

    private void validateProjects(AiResponseParser.ParsedVariant variant,
                                  Map<String, Object> profilePayload) {
        if (!hasSection(profilePayload, "projects")) return;

        Set<String> allowedIds = extractSourceIds(profilePayload, "projects");
        String context = variantContext(variant);

        if (allowedIds.isEmpty()) {
            if (variant.projects != null && !variant.projects.isEmpty()) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": projects returned but profile has no source project records");
            }
            return;
        }

        if (variant.projects == null || variant.projects.isEmpty()) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": projects are required because profile has projects");
        }

        int maxProjects = budgetConfigService.getMaxProjects();
        if (variant.projects.size() > maxProjects) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context
                            + ": project count exceeds budget maximum: "
                            + variant.projects.size() + " > " + maxProjects);
        }

        Set<String> seen = new HashSet<>();
        for (AiResponseParser.ProjectItem item : variant.projects) {
            validateRequiredText(item.sourceId, "projects[].sourceId", context);
            validateRequiredText(item.projectName, "projects[].projectName", context);
            validateRequiredText(item.role, "projects[].role", context);
            validateRequiredText(item.description, "projects[].description", context);

            if (!allowedIds.contains(item.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": unknown project sourceId: " + item.sourceId);
            }
            if (!seen.add(item.sourceId)) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": duplicate project sourceId: " + item.sourceId);
            }
        }
    }

    private void validateBulletPoints(AiResponseParser.ParsedVariant variant) {
        String context = variantContext(variant);

        // Validate work experience bullets
        if (variant.experience != null) {
            for (int i = 0; i < variant.experience.size(); i++) {
                AiResponseParser.ExperienceItem exp = variant.experience.get(i);
                validateBullets(exp.bulletPoints, "workExperience[" + i + "]", context);
            }
        }

        // Validate project bullets
        if (variant.projects != null) {
            for (int i = 0; i < variant.projects.size(); i++) {
                AiResponseParser.ProjectItem proj = variant.projects.get(i);
                validateBullets(proj.bulletPoints, "projects[" + i + "]", context);
            }
        }
    }

    private void validateBullets(List<String> bullets, String parentPath, String context) {
        if (bullets == null || bullets.isEmpty()) return; // bullets are optional in MVP
        for (int j = 0; j < bullets.size(); j++) {
            String bullet = bullets.get(j);
            if (bullet == null || bullet.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": " + parentPath + ".bulletPoints[" + j + "] bullet point is empty");
            }
            if (bullet.length() > 250) {
                throw new IllegalArgumentException(
                        "AI response validation failed in " + context
                                + ": " + parentPath + ".bulletPoints[" + j + "] bullet point exceeds maximum length: "
                                + bullet.length() + " > 250");
            }
        }
    }

    private int sectionSize(Map<String, Object> profilePayload, String sectionName) {
        if (profilePayload == null) return 0;
        Object rawSection = profilePayload.get(sectionName);
        if (!(rawSection instanceof List<?> sectionItems)) return 0;
        return sectionItems.size();
    }

    private String findCurrentWorkExperienceSourceId(Map<String, Object> profilePayload) {
        if (profilePayload == null) return null;
        Object rawSection = profilePayload.get("workExperience");
        if (!(rawSection instanceof List<?> sectionItems)) return null;

        for (Object rawItem : sectionItems) {
            if (!(rawItem instanceof Map<?, ?> map)) continue;

            Object isCurrent = map.get("isCurrent");
            if (!isTruthy(isCurrent)) continue;

            Object id = map.get("id");
            if (id != null && !String.valueOf(id).isBlank()) {
                return String.valueOf(id);
            }
        }

        return null;
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) return false;
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private boolean hasSection(Map<String, Object> profilePayload, String sectionName) {
        return profilePayload != null && profilePayload.containsKey(sectionName);
    }

    private Set<String> extractSourceIds(Map<String, Object> profilePayload, String sectionName) {
        Set<String> ids = new HashSet<>();
        if (profilePayload == null) return ids;

        Object rawSection = profilePayload.get(sectionName);
        if (!(rawSection instanceof List<?> sectionItems)) return ids;

        for (Object rawItem : sectionItems) {
            if (!(rawItem instanceof Map<?, ?> map)) continue;
            Object id = map.get("id");
            if (id != null && !String.valueOf(id).isBlank()) {
                ids.add(String.valueOf(id));
            }
        }

        return ids;
    }

    private void validateRequiredText(String value, String fieldName, String context) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(
                    "AI response validation failed in " + context + ": " + fieldName + " is required");
        }
    }

    private String variantContext(AiResponseParser.ParsedVariant variant) {
        if (variant == null) {
            return "unknown/unknown";
        }
        String lang = isBlank(variant.languageCode) ? "unknown" : variant.languageCode;
        String level = isBlank(variant.adaptationLevel) ? "unknown" : variant.adaptationLevel;
        return lang + "/" + level;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
