package com.resumainer.service;

import com.resumainer.dao.PromptConfigDao;
import com.resumainer.dao.ProfilePromptDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.model.ResumeGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Builder pattern — assembles the final AI prompt from modular DB-backed fragments.
 * Loads system prompt + language fragment + adaptation fragment + cover letter fragment
 * + profile payload, then builds the complete system and request prompts.
 */
@Service
public class ResumePromptBuilder {

    private static final Logger log = LoggerFactory.getLogger(ResumePromptBuilder.class);

    private final PromptConfigDao promptConfigDao;
    private final ProfilePromptDao profilePromptDao;
    private final GenerationRequestDao generationRequestDao;
    private final ResumeBudgetConfigService budgetConfigService;

    public ResumePromptBuilder(PromptConfigDao promptConfigDao,
                                ProfilePromptDao profilePromptDao,
                                GenerationRequestDao generationRequestDao,
                                ResumeBudgetConfigService budgetConfigService) {
        this.promptConfigDao = promptConfigDao;
        this.profilePromptDao = profilePromptDao;
        this.generationRequestDao = generationRequestDao;
        this.budgetConfigService = budgetConfigService;
    }

    /**
     * Build result containing rendered prompts and metadata for logging.
     */
    public static class PromptResult {
        public String systemPrompt;
        public String requestPrompt;
        public String promptHash;
        public UUID promptConfigId;
        public Map<String, Object> profilePayload;
        public String profilePayloadJson;
    }

    /**
     * Builds the complete prompt for a generation request.
     */
    public PromptResult build(UUID requestId, UUID userId) {
        ResumeGenerationRequest request = generationRequestDao.findById(requestId, userId);
        if (request == null) {
            throw new IllegalArgumentException("Generation request not found: " + requestId);
        }

        UUID promptConfigId = request.getPromptConfigId();
        if (promptConfigId == null) {
            promptConfigId = promptConfigDao.findActiveConfigId();
        }
        if (promptConfigId == null) {
            throw new IllegalStateException("No active prompt configuration found.");
        }

        // Load modular prompt fragments
        String systemPrompt = promptConfigDao.getSystemPrompt(promptConfigId);
        String languageFragment = promptConfigDao.getLanguagePrompt(promptConfigId, request.getLanguageMode());
        String adaptationFragment = promptConfigDao.getAdaptationPrompt(promptConfigId, request.getAdaptationSelection());
        String coverFragment = promptConfigDao.getCoverLetterPrompt(promptConfigId, request.isIncludeCoverLetter());

        // Load profile payload
        Map<String, Object> profilePayload = buildProfilePayload(userId);

        // Build the dynamic payload section for the prompt
        String profilePayloadJson = mapToJson(profilePayload);
        String contractJson = buildJsonContract(request.getLanguageMode(), request.getAdaptationSelection());

        // Assemble request prompt
        String requestPrompt = String.join("\n\n",
                "# Resume generation request",
                languageFragment,
                adaptationFragment,
                coverFragment,
                "# Dynamic payload",
                profilePayloadJson,
                "# Personal information rule",
                "Return personalInfo for every language/adaptation variant. "
                + "Education is not AI-generated: use bilingual profile education fields during template rendering. "
                + "For personalInfo.workFormats, use only the profile work formats from the dynamic payload. "
                + "Do not invent work formats. If no work formats are selected, return null.",
                "# Skills rule",
                "The profile contains a free-text skills field under additionalInfo.skills. "
                + "Convert this free-text into structured skills records for the output. "
                + "If the skills field contains phrases like 'SQL, BPMN, UML, Requirements Analysis', "
                + "group them by category when possible and return as: "
                + "[{\"skillGroup\": \"CategoryName\", \"skillName\": \"SkillName\"}]. "
                + "If grouping by category is not obvious from the source data, "
                + "use a single group like \"Professional Skills\" and list each skill as a separate record. "
                + "Return skills as a non-empty array. Do not omit the skills section.",
                "# Resume budget rules",
                buildBudgetSection(),
                "# Required response contract",
                contractJson,
                "Return JSON only. No markdown. No commentary."
        );

        // Generate prompt hash for reproducibility
        String promptHash = sha256(systemPrompt + "\n" + requestPrompt);

        PromptResult result = new PromptResult();
        result.systemPrompt = systemPrompt;
        result.requestPrompt = requestPrompt;
        result.promptHash = promptHash;
        result.promptConfigId = promptConfigId;
        result.profilePayload = profilePayload;
        result.profilePayloadJson = profilePayloadJson;
        return result;
    }

    private Map<String, Object> buildProfilePayload(UUID userId) {
        Map<String, Object> payload = new LinkedHashMap<>();

        // Contact details
        payload.put("contact", profilePromptDao.loadContact(userId));

        // Work experience (loaded from user profile via ProfilePromptDao)
        payload.put("workExperience", profilePromptDao.loadWorkExperience(userId));

        // Bilingual education
        payload.put("education", profilePromptDao.loadEducation(userId));

        // Courses (loaded from user profile via ProfilePromptDao)
        payload.put("courses", profilePromptDao.loadCourses(userId));

        // Projects (loaded from user profile via ProfilePromptDao)
        payload.put("projects", profilePromptDao.loadProjects(userId));

        // Additional info
        payload.put("additionalInfo", profilePromptDao.loadAdditionalInfo(userId));

        // Normalized work formats
        payload.put("workFormats", profilePromptDao.loadWorkFormats(userId));

        return payload;
    }

    // Package-private for test access — builds dynamic JSON contract based on request settings
    String buildJsonContract(String languageMode, String adaptationSelection) {
        // Build the expected JSON contract for the AI model.
        // Uses nested language→level structure so the model returns exactly
        // the requested shape. Single-language + single-level returns one branch.
        // The parser already supports both nested and flat structures, but
        // this contract demands the exact nested shape.
        String variantFields = "      \"professionalTitle\": \"string\",\n"
                + "      \"valueLine\": \"string\",\n"
                + "      \"professionalSummary\": \"string\",\n"
                + "      \"professionalAspirations\": \"string\",\n"
                + "      \"workExperience\": [{\n"
                + "        \"jobTitle\": \"string\",\n"
                + "        \"companyName\": \"string\",\n"
                + "        \"description\": \"string\",\n"
                + "        \"location\": \"string\",\n"
                + "        \"startDate\": \"YYYY-MM\",\n"
                + "        \"endDate\": \"YYYY-MM or null\",\n"
                + "        \"isFirstPage\": true\n"
                + "      }],\n"
                + "      \"courses\": [{\n"
                + "        \"name\": \"string\",\n"
                + "        \"provider\": \"string\",\n"
                + "        \"courseFocus\": \"string\"\n"
                + "      }],\n"
                + "      \"projects\": [{\n"
                + "        \"projectName\": \"string\",\n"
                + "        \"role\": \"string\",\n"
                + "        \"description\": \"string\",\n"
                + "        \"startDate\": \"YYYY-MM\"\n"
                + "      }],\n"
                + "      \"skills\": [{\n"
                + "        \"skillGroup\": \"string\",\n"
                + "        \"skillName\": \"string\"\n"
                + "      }],\n"
                + "      \"personalInfo\": {\n"
                + "        \"location\": \"string\",\n"
                + "        \"spokenLanguages\": \"string\",\n"
                + "        \"willingnessToRelocate\": \"string\",\n"
                + "        \"willingnessForBusinessTrips\": \"string\",\n"
                + "        \"citizenship\": \"string\",\n"
                + "        \"dateOfBirth\": \"YYYY-MM-DD\",\n"
                + "        \"workFormats\": [\"string\"]\n"
                + "      },\n"
                + "      \"coverLetter\": \"string or null\"\n"
                + "    }";

        // Determine which languages and levels to include
        List<String> languages = new ArrayList<>();
        switch (languageMode) {
            case "ENGLISH_ONLY": languages.add("en"); break;
            case "RUSSIAN_ONLY": languages.add("ru"); break;
            case "BILINGUAL":    languages.add("en"); languages.add("ru"); break;
        }

        List<String> levels = new ArrayList<>();
        switch (adaptationSelection) {
            case "MINIMAL":  levels.add("minimal"); break;
            case "BALANCED": levels.add("balanced"); break;
            case "MAXIMUM":  levels.add("maximum"); break;
            case "ALL":      levels.add("minimal"); levels.add("balanced"); levels.add("maximum"); break;
        }

        StringBuilder sb = new StringBuilder("{\n");
        for (int li = 0; li < languages.size(); li++) {
            String lang = languages.get(li);
            sb.append("  \"").append(lang).append("\": {\n");
            for (int vi = 0; vi < levels.size(); vi++) {
                String level = levels.get(vi);
                sb.append("    \"").append(level).append("\": {\n");
                sb.append(variantFields);
                sb.append("\n    }");
                if (vi < levels.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  }");
            if (li < languages.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Builds the # Resume budget rules section from active DB budget config.
     * Values come from resume_section_budget_rules, not hardcoded.
     * This enables configuration without Java code changes (BA requirement).
     */
    private String buildBudgetSection() {
        try {
            int groupsMin = budgetConfigService.getSkillsGroups();
            int groupsMax = budgetConfigService.getSkillsGroupsMax();
            int spgMin = budgetConfigService.getSkillsPerGroup();
            int spgMax = budgetConfigService.getSkillsPerGroupMax();
            int wpsMax = budgetConfigService.getWordsPerSkillMax();
            int maxCourses = budgetConfigService.getMaxCourses();
            int cfMin = budgetConfigService.getCourseFocusWordsMin();
            int cfMax = budgetConfigService.getCourseFocusWordsMax();
            int maxProj = budgetConfigService.getMaxProjects();
            int psMin = budgetConfigService.getProjectSentencesMin();
            int psMax = budgetConfigService.getProjectSentencesMax();

            return "Skills:\n"
                    + "- Create " + groupsMin + "\u2013" + groupsMax + " meaningful skill groups if source skills allow it.\n"
                    + "- Put " + spgMin + "\u2013" + spgMax + " skills per group where possible.\n"
                    + "- Keep each skill 1\u2013" + wpsMax + " words.\n"
                    + "\n"
                    + "Courses:\n"
                    + "- Include up to " + maxCourses + " courses.\n"
                    + "- For every included course, always provide courseFocus.\n"
                    + "- Keep courseFocus concise, ideally " + cfMin + "\u2013" + cfMax + " words.\n"
                    + "\n"
                    + "Projects:\n"
                    + "- Include up to " + maxProj + " projects.\n"
                    + "- Keep project descriptions " + psMin + "\u2013" + psMax + " sentences.\n";
        } catch (Exception e) {
            log.warn("Failed to load budget config for prompt: {}", e.getMessage());
            return "Skills:\n- Group skills by category when possible.\n- Return skills as a non-empty array.\n";
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 not available, skipping prompt hash");
            return null;
        }
    }

    private String mapToJson(Map<String, Object> map) {
        // Simple JSON serialization for the prompt payload
        StringBuilder sb = new StringBuilder("{\n");
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = "\"" + entry.getKey() + "\"";
            String value;
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) entry.getValue();
                value = mapToJson(nested);
            } else if (entry.getValue() instanceof List) {
                value = listToJson((List<?>) entry.getValue());
            } else if (entry.getValue() == null) {
                value = "null";
            } else if (entry.getValue() instanceof String) {
                value = "\"" + escapeJson((String) entry.getValue()) + "\"";
            } else {
                value = String.valueOf(entry.getValue());
            }
            entries.add("  " + key + ": " + value);
        }
        sb.append(String.join(",\n", entries));
        sb.append("\n}");
        return sb.toString();
    }

    private String listToJson(List<?> list) {
        if (list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        List<String> items = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) item;
                items.add(mapToJson(map));
            } else if (item instanceof String) {
                items.add("\"" + escapeJson((String) item) + "\"");
            } else {
                items.add(String.valueOf(item));
            }
        }
        sb.append("    ").append(String.join(",\n    ", items));
        sb.append("\n  ]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
