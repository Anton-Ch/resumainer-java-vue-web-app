package com.resumainer.service;

import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.ResumeTemplateDao;
import com.resumainer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Backend-owned HTML renderer. Fills one-page/two-page HTML templates
 * using simple string replacement. No template engine.
 * Sanitizes AI-generated text before insertion.
 */
@Service
public class ResumeTemplateRenderer {

    private static final Logger log = LoggerFactory.getLogger(ResumeTemplateRenderer.class);

    private final ResumeTemplateDao templateDao;
    private final GeneratedFileStorageService fileStorage;

    // Allowed HTML tags in AI-generated content
    private static final Set<String> ALLOWED_TAGS = Set.of("p", "strong", "em", "ul", "li", "br");

    public ResumeTemplateRenderer(ResumeTemplateDao templateDao,
                                   GeneratedFileStorageService fileStorage) {
        this.templateDao = templateDao;
        this.fileStorage = fileStorage;
    }

    /**
     * Renders a filled HTML for one response and saves it to disk.
     *
     * @param responseBundle the response with all child sections
     * @param profileEducation list of education records from profile (bilingual)
     * @param languageCode EN or RU
     * @param adaptationLevel MINIMAL, BALANCED, MAXIMUM
     * @param username the owner's username
     * @param publicCode public code for this resume
     * @return the relative path to the saved HTML file
     */
    public String renderAndSave(
            GenerationResponseDao.ResponseBundle responseBundle,
            List<Map<String, Object>> profileEducation,
            String languageCode, String adaptationLevel,
            String username, String publicCode) {

        // Build section fragments
        Map<String, String> fragments = buildFragments(responseBundle, profileEducation, languageCode);

        // Load template
        String templatePath = templateDao.findDefaultTemplatePath();
        if (templatePath == null) {
            throw new RuntimeException("No default resume template found.");
        }
        String template = fileStorage.readFile(templatePath);

        // Apply fragment replacements
        String result = template;
        for (Map.Entry<String, String> entry : fragments.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        // Fallback: if template still has {{RESUME_CONTENT}}, build full body
        if (result.contains("{{RESUME_CONTENT}}")) {
            result = result.replace("{{RESUME_CONTENT}}", buildFallbackBody(responseBundle, profileEducation, languageCode));
        }

        // Sanitize AI-generated text in the result
        result = sanitizeHtml(result);

        // Generate filename and save
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        String languageSuffix = languageCode.equals("EN") ? "en" : "ru";
        String filename = timestamp + "_" + languageSuffix + "_" + adaptationLevel.toLowerCase() + ".html";

        return fileStorage.saveFile(username, publicCode, filename, result);
    }

    // --- Fragment building ---

    private Map<String, String> buildFragments(
            GenerationResponseDao.ResponseBundle bundle,
            List<Map<String, Object>> profileEducation,
            String lang) {

        boolean isEn = "EN".equals(lang);
        ResumeGenerationResponse resp = bundle.response;
        Map<String, String> map = new LinkedHashMap<>();

        // Header
        String fullName = "Candidate Name"; // Would come from profile
        String title = resp.getProfessionalTitle() != null ? resp.getProfessionalTitle() : "";
        String valueLine = resp.getValueLine() != null ? resp.getValueLine() : "";

        map.put("<!-- RESUME:HEADER -->",
                "<h1>" + esc(fullName) + "</h1>"
                + "<p class=\"title\">" + esc(title) + "</p>"
                + (valueLine.isEmpty() ? "" : "<p class=\"value-line\">" + esc(valueLine) + "</p>"));

        // Professional Summary
        String summary = resp.getProfessionalSummary() != null ? resp.getProfessionalSummary() : "";
        map.put("<!-- RESUME:PROFESSIONAL_SUMMARY -->",
                "<h2>" + (isEn ? "Professional Summary" : "О себе") + "</h2>"
                + "<p>" + esc(summary) + "</p>");

        // Work Experience — primary (first 2) + additional (rest)
        List<GenerationResponseExperience> expList = bundle.experience;
        StringBuilder primaryExp = new StringBuilder();
        StringBuilder additionalExp = new StringBuilder();
        for (int i = 0; i < expList.size(); i++) {
            GenerationResponseExperience exp = expList.get(i);
            String html = buildExperienceHtml(exp);
            if (i < 2) {
                primaryExp.append(html);
            } else {
                additionalExp.append(html);
            }
        }
        map.put("<!-- RESUME:WORK_EXPERIENCE_PRIMARY -->",
                "<h2>" + (isEn ? "Work Experience" : "Опыт работы") + "</h2>"
                + (!primaryExp.isEmpty() ? primaryExp.toString() : "<p>No experience listed.</p>"));
        map.put("<!-- RESUME:WORK_EXPERIENCE_ADDITIONAL -->",
                (!additionalExp.isEmpty()
                        ? "<h3>" + (isEn ? "Additional Experience" : "Дополнительный опыт") + "</h3>"
                        + additionalExp.toString()
                        : ""));

        // Skills
        List<GenerationResponseSkill> skills = bundle.skills;
        Map<String, List<String>> grouped = skills.stream()
                .collect(Collectors.groupingBy(
                        GenerationResponseSkill::getSkillGroup,
                        LinkedHashMap::new,
                        Collectors.mapping(GenerationResponseSkill::getSkillName, Collectors.toList())));
        StringBuilder skillHtml = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            skillHtml.append("<p><strong>").append(esc(entry.getKey())).append(":</strong> ")
                    .append(entry.getValue().stream().map(this::esc).collect(Collectors.joining(", ")))
                    .append("</p>");
        }
        map.put("<!-- RESUME:SKILLS -->",
                "<h2>" + (isEn ? "Skills" : "Навыки") + "</h2>"
                + (skillHtml.isEmpty() ? "<p>No skills listed.</p>" : skillHtml.toString()));

        // Education — from profile (bilingual), NOT from AI
        String eduHtml = buildEducationHtml(profileEducation, isEn);
        map.put("<!-- RESUME:EDUCATION -->",
                "<h2>" + (isEn ? "Education" : "Образование") + "</h2>"
                + (eduHtml.isEmpty() ? "<p>No education listed.</p>" : eduHtml));

        // Courses
        List<GenerationResponseCourse> courses = bundle.courses;
        StringBuilder courseHtml = new StringBuilder();
        for (GenerationResponseCourse c : courses) {
            courseHtml.append("<p>")
                    .append(esc(c.getName()))
                    .append(c.getProvider() != null ? " — " + esc(c.getProvider()) : "")
                    .append("</p>");
        }
        map.put("<!-- RESUME:COURSES -->",
                "<h2>" + (isEn ? "Courses" : "Курсы") + "</h2>"
                + (courseHtml.isEmpty() ? "<p>No courses listed.</p>" : courseHtml.toString()));

        // Projects
        List<GenerationResponseProject> projects = bundle.projects;
        StringBuilder projHtml = new StringBuilder();
        for (GenerationResponseProject p : projects) {
            projHtml.append("<p><strong>").append(esc(p.getProjectName())).append("</strong>")
                    .append(p.getRole() != null ? " — " + esc(p.getRole()) : "")
                    .append("</p>")
                    .append(p.getDescription() != null ? "<p>" + esc(p.getDescription()) + "</p>" : "");
        }
        map.put("<!-- RESUME:PROJECTS -->",
                "<h2>" + (isEn ? "Projects" : "Проекты") + "</h2>"
                + (projHtml.isEmpty() ? "<p>No projects listed.</p>" : projHtml.toString()));

        // Professional Aspirations
        String aspirations = resp.getProfessionalAspirations() != null ? resp.getProfessionalAspirations() : "";
        map.put("<!-- RESUME:PROFESSIONAL_ASPIRATIONS -->",
                "<h2>" + (isEn ? "Professional Aspirations" : "Профессиональные цели") + "</h2>"
                + "<p>" + esc(aspirations) + "</p>");

        // Personal Information
        String personalHtml = buildPersonalInfoHtml(bundle, isEn);
        map.put("<!-- RESUME:PERSONAL_INFO -->",
                "<h2>" + (isEn ? "Personal Information" : "Личная информация") + "</h2>"
                + personalHtml);

        // Page notes
        map.put("<!-- RESUME:NOTE_NEXT -->", isEn ? "See the next page" : "См. следующую страницу");
        map.put("<!-- RESUME:NOTE_PREVIOUS -->", isEn ? "See the previous page" : "См. предыдущую страницу");

        return map;
    }

    // --- Fallback body (when template only has {{RESUME_CONTENT}}) ---

    private String buildFallbackBody(GenerationResponseDao.ResponseBundle bundle,
                                      List<Map<String, Object>> profileEducation,
                                      String lang) {
        boolean isEn = "EN".equals(lang);
        Map<String, String> fragments = buildFragments(bundle, profileEducation, lang);
        return "<div class=\"resume-content\">\n"
                + fragments.getOrDefault("<!-- RESUME:HEADER -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:PROFESSIONAL_SUMMARY -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:WORK_EXPERIENCE_PRIMARY -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:WORK_EXPERIENCE_ADDITIONAL -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:SKILLS -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:EDUCATION -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:COURSES -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:PROJECTS -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:PROFESSIONAL_ASPIRATIONS -->", "")
                + "\n" + fragments.getOrDefault("<!-- RESUME:PERSONAL_INFO -->", "")
                + "\n</div>";
    }

    // --- Helpers ---

    private String buildExperienceHtml(GenerationResponseExperience exp) {
        return "<div class=\"experience-item\">"
                + "<p><strong>" + esc(exp.getJobTitle()) + "</strong>"
                + " — " + esc(exp.getCompanyName()) + "</p>"
                + "<p class=\"date\">" + formatDate(exp.getStartDate()) + " – "
                + (exp.getEndDate() != null ? formatDate(exp.getEndDate()) : "Present") + "</p>"
                + "<p>" + esc(exp.getDescription()) + "</p>"
                + "</div>";
    }

    private String buildEducationHtml(List<Map<String, Object>> profileEducation, boolean isEn) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> edu : profileEducation) {
            String inst = isEn ? (String) edu.get("institutionNameEn") : (String) edu.get("institutionNameRu");
            String deg = isEn ? (String) edu.get("degreeEn") : (String) edu.get("degreeRu");
            String field = isEn ? (String) edu.get("fieldOfStudyEn") : (String) edu.get("fieldOfStudyRu");
            if (inst == null) continue;
            sb.append("<p><strong>").append(esc(inst)).append("</strong>")
                    .append(deg != null ? " — " + esc(deg) : "")
                    .append(field != null ? ", " + esc(field) : "")
                    .append("</p>");
        }
        return sb.toString();
    }

    private String buildPersonalInfoHtml(GenerationResponseDao.ResponseBundle bundle, boolean isEn) {
        // Try to load personal info from response_personal (would need personalDao here)
        // For MVP, return a placeholder or load from the response if available
        return "<p>" + (isEn ? "Personal information available in full resume." : "Личная информация доступна в полном резюме.") + "</p>";
    }

    private String sanitizeHtml(String html) {
        // Strip script and style tags
        html = html.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        html = html.replaceAll("(?i)<style[^>]*>.*?</style>", "");

        // Remove event handlers (onclick, onload, etc.)
        html = html.replaceAll("(?i)\\s+on\\w+\\s*=\\s*\"[^\"]*\"", "");
        html = html.replaceAll("(?i)\\s+on\\w+\\s*=\\s*'[^']*'", "");

        // Allow only specific tags: strip everything else
        // This is a simple approach — only keep allowed tags
        html = html.replaceAll("</?(?!(" + String.join("|", ALLOWED_TAGS) + ")\\b)[^>]*>", "");

        return html;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return "";
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        return date.format(fmt);
    }
}
