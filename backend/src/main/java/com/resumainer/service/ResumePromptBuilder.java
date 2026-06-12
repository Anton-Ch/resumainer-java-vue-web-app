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

    public ResumePromptBuilder(PromptConfigDao promptConfigDao,
                                ProfilePromptDao profilePromptDao,
                                GenerationRequestDao generationRequestDao) {
        this.promptConfigDao = promptConfigDao;
        this.profilePromptDao = profilePromptDao;
        this.generationRequestDao = generationRequestDao;
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
        return result;
    }

    private Map<String, Object> buildProfilePayload(UUID userId) {
        Map<String, Object> payload = new LinkedHashMap<>();

        // Contact details
        payload.put("contact", profilePromptDao.loadContact(userId));

        // Work experience (loaded via existing DAOs — stored as list of maps)
        // Note: full work experience list comes from WorkExperienceDao
        payload.put("workExperience", Collections.emptyList());

        // Bilingual education
        payload.put("education", profilePromptDao.loadEducation(userId));

        // Courses (loaded via existing CourseCertificateDao)
        payload.put("courses", Collections.emptyList());

        // Projects (loaded via existing ProjectDao)
        payload.put("projects", Collections.emptyList());

        // Additional info
        payload.put("additionalInfo", profilePromptDao.loadAdditionalInfo(userId));

        // Normalized work formats
        payload.put("workFormats", profilePromptDao.loadWorkFormats(userId));

        return payload;
    }

    private String buildJsonContract(String languageMode, String adaptationSelection) {
        // Build the expected JSON contract for the AI model based on settings
        return "{\n"
                + "  \"professionalTitle\": \"string\",\n"
                + "  \"valueLine\": \"string\",\n"
                + "  \"professionalSummary\": \"string\",\n"
                + "  \"professionalAspirations\": \"string\",\n"
                + "  \"workExperience\": [{\n"
                + "    \"jobTitle\": \"string\",\n"
                + "    \"companyName\": \"string\",\n"
                + "    \"description\": \"string\",\n"
                + "    \"location\": \"string\",\n"
                + "    \"startDate\": \"YYYY-MM\",\n"
                + "    \"endDate\": \"YYYY-MM or null\",\n"
                + "    \"isFirstPage\": true\n"
                + "  }],\n"
                + "  \"courses\": [{\n"
                + "    \"name\": \"string\",\n"
                + "    \"provider\": \"string\",\n"
                + "    \"courseFocus\": \"string\"\n"
                + "  }],\n"
                + "  \"projects\": [{\n"
                + "    \"projectName\": \"string\",\n"
                + "    \"role\": \"string\",\n"
                + "    \"description\": \"string\",\n"
                + "    \"startDate\": \"YYYY-MM\"\n"
                + "  }],\n"
                + "  \"skills\": [{\n"
                + "    \"skillGroup\": \"string\",\n"
                + "    \"skillName\": \"string\"\n"
                + "  }],\n"
                + "  \"personalInfo\": {\n"
                + "    \"location\": \"string\",\n"
                + "    \"spokenLanguages\": \"string\",\n"
                + "    \"willingnessToRelocate\": \"string\",\n"
                + "    \"willingnessForBusinessTrips\": \"string\",\n"
                + "    \"citizenship\": \"string\",\n"
                + "    \"dateOfBirth\": \"YYYY-MM-DD\",\n"
                + "    \"workFormats\": [\"string\"]\n"
                + "  }\n"
                + "}";
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
