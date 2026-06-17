package com.resumainer.service;

import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.GenerationResponsePersonalDao;
import com.resumainer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Backend-owned HTML renderer using prototype templates.
 * Loads template file from classpath, keeps ALL CSS/JS intact,
 * replaces only placeholder comments with data.
 * Template selection: one-page vs two-page based on content volume.
 */
@Service
public class ResumeTemplateRenderer {

    private static final Logger log = LoggerFactory.getLogger(ResumeTemplateRenderer.class);

    private static final String ONE_PAGE_EN = "templates/one_page_template_en.html";
    private static final String ONE_PAGE_RU = "templates/one_page_template_ru.html";
    private static final String TWO_PAGE_EN = "templates/two_page_template_en.html";
    private static final String TWO_PAGE_RU = "templates/two_page_template_ru.html";

    private final GeneratedFileStorageService fileStorage;
    private final GenerationResponsePersonalDao personalDao;

    public ResumeTemplateRenderer(GeneratedFileStorageService fileStorage,
                                   GenerationResponsePersonalDao personalDao) {
        this.fileStorage = fileStorage;
        this.personalDao = personalDao;
    }

    /**
     * Renders a filled HTML for one response and saves it to disk.
     *
     * @param contactData from ProfilePromptDao.loadContact(): fullName, phone, resumeEmail, location, professionalTitle
     */
    public String renderAndSave(
            GenerationResponseDao.ResponseBundle responseBundle,
            List<Map<String, Object>> profileEducation,
            Map<String, Object> contactData,
            String languageCode, String adaptationLevel,
            String username, String publicCode) {

        boolean isEn = "EN".equals(languageCode);
        boolean useTwoPage = shouldUseTwoPage(responseBundle);

        String templatePath = useTwoPage
                ? (isEn ? TWO_PAGE_EN : TWO_PAGE_RU)
                : (isEn ? ONE_PAGE_EN : ONE_PAGE_RU);
        String template = loadTemplate(templatePath);

        Map<String, String> fragments = buildFragments(responseBundle, profileEducation,
                contactData, isEn);

        String result = template;
        for (Map.Entry<String, String> entry : fragments.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        if (result.contains("<!-- RESUME:")) {
            log.warn("Unreplaced placeholders in template output");
        }

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        String langSuffix = isEn ? "en" : "ru";
        String filename = timestamp + "_" + langSuffix + "_" + adaptationLevel.toLowerCase() + ".html";

        return fileStorage.saveFile(username, publicCode, filename, result);
    }

    // ─── Template selection ──────────────────────────────────────────

    private boolean shouldUseTwoPage(GenerationResponseDao.ResponseBundle bundle) {
        if (bundle.projects != null && !bundle.projects.isEmpty()) return true;
        if (bundle.experience != null && bundle.experience.size() >= 3) return true;
        if (bundle.courses != null && bundle.courses.size() > 5) return true;
        return false;
    }

    /**
     * Splits work experience by isFirstPage flag.
     * Falls back to legacy first-2/rest split when no record is marked isFirstPage.
     */
    private WorkExperiencePageSplit splitWorkExperienceByPage(
            List<GenerationResponseExperience> experiences) {

        if (experiences == null || experiences.isEmpty()) {
            return new WorkExperiencePageSplit(List.of(), List.of());
        }

        List<GenerationResponseExperience> primary = experiences.stream()
                .filter(GenerationResponseExperience::isFirstPage)
                .toList();

        List<GenerationResponseExperience> additional = experiences.stream()
                .filter(exp -> !exp.isFirstPage())
                .toList();

        // Legacy fallback: if no record is marked first page, use positional split
        if (primary.isEmpty()) {
            List<GenerationResponseExperience> fbPrimary = experiences.size() <= 2
                    ? List.copyOf(experiences)
                    : List.copyOf(experiences.subList(0, 2));
            List<GenerationResponseExperience> fbAdditional = experiences.size() > 2
                    ? List.copyOf(experiences.subList(2, experiences.size()))
                    : List.of();
            return new WorkExperiencePageSplit(fbPrimary, fbAdditional);
        }

        return new WorkExperiencePageSplit(primary, additional);
    }

    private record WorkExperiencePageSplit(
            List<GenerationResponseExperience> primary,
            List<GenerationResponseExperience> additional
    ) {}

    // ─── Fragment building ───────────────────────────────────────────

    private Map<String, String> buildFragments(
            GenerationResponseDao.ResponseBundle bundle,
            List<Map<String, Object>> profileEducation,
            Map<String, Object> contactData,
            boolean isEn) {

        ResumeGenerationResponse resp = bundle.response;
        Map<String, String> map = new LinkedHashMap<>();

        // Load personal info once (used by header location and personal section)
        GenerationResponsePersonal personal = personalDao.findByResponseId(resp.getId());

        // --- HEADER with real contact data ---
        map.put("<!-- RESUME:HEADER -->", buildHeader(resp, contactData, personal, isEn));

        // --- Professional Summary ---
        map.put("<!-- RESUME:PROFESSIONAL_SUMMARY -->",
                buildSection(isEn ? "Professional Summary" : "О себе",
                        resp.getProfessionalSummary(), true));

        // --- Work Experience split by isFirstPage flag ---
        List<GenerationResponseExperience> expList = bundle.experience != null
                ? bundle.experience : List.of();
        WorkExperiencePageSplit weSplit = splitWorkExperienceByPage(expList);

        map.put("<!-- RESUME:WORK_EXPERIENCE_PRIMARY -->",
                buildWorkExperienceSection(weSplit.primary(), isEn));
        map.put("<!-- RESUME:WORK_EXPERIENCE_ADDITIONAL -->",
                weSplit.additional().isEmpty() ? "" : buildWorkExperienceSection(weSplit.additional(), isEn));

        // --- Skills ---
        map.put("<!-- RESUME:SKILLS -->", buildSkillsSection(bundle.skills, isEn));

        // --- Education ---
        map.put("<!-- RESUME:EDUCATION -->", buildEducationSection(profileEducation, isEn));

        // --- Courses ---
        map.put("<!-- RESUME:COURSES -->", buildCoursesSection(bundle.courses, isEn));

        // --- Projects ---
        map.put("<!-- RESUME:PROJECTS -->", buildProjectsSection(bundle.projects, isEn));

        // --- Aspirations ---
        map.put("<!-- RESUME:PROFESSIONAL_ASPIRATIONS -->",
                buildSection(isEn ? "Professional Aspirations" : "Профессиональные цели",
                        resp.getProfessionalAspirations(), false));

        // --- Personal Information with labels ---
        map.put("<!-- RESUME:PERSONAL_INFO -->", buildPersonalInfoSection(personal, isEn));

        // --- Page Two Header (uses contact name) ---
        String title = resp.getProfessionalTitle() != null ? resp.getProfessionalTitle() : "";
        String name = getCandidateName(contactData, isEn);
        map.put("<!-- RESUME:PAGE_TWO_HEADER -->",
                "<section>\n"
                + "  <div class=\"page-two-name\">" + esc(name) + "</div>\n"
                + "  <div class=\"page-two-title\">" + esc(title) + " | "
                + (isEn ? "Continued Resume" : "Продолжение резюме") + "</div>\n"
                + "</section>");

        // --- Page Notes ---
        map.put("<!-- RESUME:NOTE_NEXT -->", isEn ? "See the next page" : "См. следующую страницу");
        map.put("<!-- RESUME:NOTE_PREVIOUS -->", isEn ? "See the previous page" : "См. предыдущую страницу");

        return map;
    }

    // ─── Section builders ──────────────────────────────────────────

    private String getCandidateName(Map<String, Object> contact, boolean isEn) {
        if (contact == null) return isEn ? "Candidate" : "Кандидат";
        String name = (String) contact.getOrDefault("fullName",
                contact.getOrDefault("full_name", null));
        if (name == null || name.isBlank()) {
            return isEn ? "Candidate" : "Кандидат";
        }
        return name;
    }

    private String buildHeader(ResumeGenerationResponse resp,
                                Map<String, Object> contact,
                                GenerationResponsePersonal personal,
                                boolean isEn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");

        // Candidate name from profile contact
        String name = getCandidateName(contact, isEn);
        sb.append("  <div class=\"candidate-name\">").append(esc(name)).append("</div>\n");

        // Professional title from response
        String title = resp.getProfessionalTitle();
        if (title != null && !title.isBlank()) {
            sb.append("  <div class=\"candidate-title\">").append(esc(title)).append("</div>\n");
        }

        // Contact line from profile data + generated personal location
        String contactLine = buildContactLine(contact, personal, isEn);
        if (!contactLine.isBlank()) {
            sb.append("  <div class=\"contact-line\">").append(contactLine).append("</div>\n");
        }

        // Secondary contact line: LinkedIn, Portfolio, Telegram, WhatsApp
        String secondaryLine = buildSecondaryContactLine(contact, isEn);
        if (!secondaryLine.isBlank()) {
            sb.append("  <div class=\"contact-line\">").append(secondaryLine).append("</div>\n");
        }

        // Value line from response
        String valueLine = resp.getValueLine();
        if (valueLine != null && !valueLine.isBlank()) {
            sb.append("  <div class=\"value-line\">").append(esc(valueLine)).append("</div>\n");
        }
        sb.append("</section>");
        return sb.toString();
    }

    /**
     * Builds contact line. Phone/email from profile contact.
     * Location prefers generated personal location, falls back to contact location.
     */
    private String buildContactLine(Map<String, Object> contact,
                                     GenerationResponsePersonal personal,
                                     boolean isEn) {
        List<String> parts = new ArrayList<>();

        if (contact != null) {
            String phone = (String) contact.get("phone");
            if (phone != null && !phone.isBlank()) parts.add(esc(phone));

            String email = (String) contact.get("resumeEmail");
            if (email == null || email.isBlank()) email = (String) contact.get("email");
            if (email != null && !email.isBlank()) parts.add(esc(email));
        }

        // Location: prefer generated personal, fallback to contact
        String location = null;
        if (personal != null && personal.getLocation() != null
                && !personal.getLocation().isBlank()) {
            location = personal.getLocation();
        } else if (contact != null) {
            location = (String) contact.get("location");
        }

        if (location != null && !location.isBlank()) {
            parts.add(esc(location));
        }

        return String.join(" | ", parts);
    }

    /**
     * Builds secondary contact line: LinkedIn, Portfolio, Telegram, WhatsApp.
     * Only includes fields with non-blank values. Labels are bilingual where needed.
     */
    private String buildSecondaryContactLine(Map<String, Object> contact, boolean isEn) {
        if (contact == null) return "";

        List<String> parts = new ArrayList<>();

        String linkedin = getString(contact, "linkedinUrl", "linkedin_url");
        if (linkedin != null && !linkedin.isBlank()) {
            parts.add("LinkedIn: " + esc(linkedin));
        }

        String portfolio = getString(contact, "portfolioUrl", "portfolio_url");
        if (portfolio != null && !portfolio.isBlank()) {
            String label = isEn ? "Portfolio" : "Портфолио";
            parts.add(label + ": " + esc(portfolio));
        }

        String telegram = getString(contact, "telegram");
        if (telegram != null && !telegram.isBlank()) {
            parts.add("Telegram: " + esc(telegram));
        }

        String whatsapp = getString(contact, "whatsapp");
        if (whatsapp != null && !whatsapp.isBlank()) {
            parts.add("WhatsApp: " + esc(whatsapp));
        }

        return String.join(" | ", parts);
    }

    /**
     * Returns the first non-null, non-blank string value from a map for a set of keys.
     * Supports both camelCase and snake_case key variants.
     */
    private String getString(Map<String, Object> map, String... keys) {
        if (map == null) return null;
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private String buildSection(String title, String content, boolean isSummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");
        sb.append("  <div class=\"section-title\">").append(esc(title)).append("</div>\n");
        if (content != null && !content.isBlank()) {
            if (isSummary) {
                sb.append("  <p class=\"summary-text\">").append(sanitizeAiText(content)).append("</p>\n");
            } else {
                sb.append("  <p>").append(sanitizeAiText(content)).append("</p>\n");
            }
        }
        sb.append("</section>");
        return sb.toString();
    }

    private String buildWorkExperienceSection(List<GenerationResponseExperience> experiences, boolean isEn) {
        if (experiences == null || experiences.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");
        sb.append("  <div class=\"section-title\">")
                .append(isEn ? "Work Experience" : "Опыт работы")
                .append("</div>\n");

        for (GenerationResponseExperience exp : experiences) {
            sb.append("  <div class=\"job-block\">\n");
            sb.append("    <div class=\"item-heading\">");
            sb.append(esc(exp.getJobTitle()));

            String company = exp.getCompanyName();
            if (company != null && !company.isBlank()) {
                sb.append(" | ").append(esc(company));
            }
            String loc = exp.getLocation();
            if (loc != null && !loc.isBlank()) {
                sb.append(" – ").append(esc(loc));
            }
            sb.append(" | <span class=\"date\">");
            sb.append(formatDate(exp.getStartDate()));
            sb.append(" – ");
            sb.append(exp.getEndDate() != null ? formatDate(exp.getEndDate()) : (isEn ? "Present" : "Наст. время"));
            sb.append("</span></div>\n");

            // Description always wrapped in <p>
            String desc = exp.getDescription();
            if (desc != null && !desc.isBlank()) {
                sb.append("    <p>").append(sanitizeAiText(desc)).append("</p>\n");
            }
            sb.append("  </div>\n");
        }
        sb.append("</section>");
        return sb.toString();
    }

    private String buildSkillsSection(List<GenerationResponseSkill> skills, boolean isEn) {
        if (skills == null || skills.isEmpty()) {
            return buildSection(isEn ? "Skills" : "Навыки",
                    isEn ? "No skills listed." : "Навыки не указаны.", false);
        }

        Map<String, List<String>> grouped = skills.stream()
                .collect(Collectors.groupingBy(
                        GenerationResponseSkill::getSkillGroup,
                        LinkedHashMap::new,
                        Collectors.mapping(GenerationResponseSkill::getSkillName, Collectors.toList())));

        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");
        sb.append("  <div class=\"section-title\">").append(isEn ? "Skills" : "Навыки").append("</div>\n");

        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            sb.append("  <div class=\"skill-group\">");
            sb.append("<span class=\"skill-label\">").append(esc(entry.getKey())).append(":</span> ");
            sb.append(entry.getValue().stream().map(this::esc).collect(Collectors.joining(", ")));
            sb.append("</div>\n");
        }
        sb.append("</section>");
        return sb.toString();
    }

    private String buildEducationSection(List<Map<String, Object>> profileEducation, boolean isEn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");
        sb.append("  <div class=\"section-title\">").append(isEn ? "Education" : "Образование").append("</div>\n");

        if (profileEducation == null || profileEducation.isEmpty()) {
            sb.append("  <div class=\"education-line\">")
                    .append(isEn ? "No education listed." : "Образование не указано.")
                    .append("</div>\n");
        } else {
            for (Map<String, Object> edu : profileEducation) {
                String inst = isEn ? (String) edu.get("institutionNameEn") : (String) edu.get("institutionNameRu");
                String deg = isEn ? (String) edu.get("degreeEn") : (String) edu.get("degreeRu");
                String field = isEn ? (String) edu.get("fieldOfStudyEn") : (String) edu.get("fieldOfStudyRu");
                if (inst == null || inst.isBlank()) continue;

                sb.append("  <div class=\"education-line\"><strong>");
                String safeDeg = deg != null && !deg.isBlank() ? deg : (isEn ? "Degree" : "Степень");
                String safeField = field != null && !field.isBlank() ? field : (isEn ? "Field" : "Специальность");
                sb.append(esc(safeDeg)).append(": ").append(esc(safeField));
                sb.append("</strong> | ").append(esc(inst));
                sb.append("</div>\n");
            }
        }
        sb.append("</section>");
        return sb.toString();
    }

    private String buildCoursesSection(List<GenerationResponseCourse> courses, boolean isEn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");
        sb.append("  <div class=\"section-title\">")
                .append(isEn ? "Courses and Certifications" : "Курсы и сертификаты")
                .append("</div>\n");

        if (courses == null || courses.isEmpty()) {
            sb.append("  <div class=\"course-line\">")
                    .append(isEn ? "No courses listed." : "Курсы не указаны.")
                    .append("</div>\n");
        } else {
            for (GenerationResponseCourse c : courses) {
                sb.append("  <div class=\"course-line\"><strong>").append(esc(c.getName())).append("</strong>");
                String provider = c.getProvider();
                if (provider != null && !provider.isBlank()) {
                    sb.append(" – ").append(esc(provider));
                }
                String focus = c.getCourseFocus();
                if (focus != null && !focus.isBlank()) {
                    sb.append(" | ").append(esc(focus));
                }
                sb.append("</div>\n");
            }
        }
        sb.append("</section>");
        return sb.toString();
    }

    private String buildProjectsSection(List<GenerationResponseProject> projects, boolean isEn) {
        if (projects == null || projects.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<section>\n");
        sb.append("  <div class=\"section-title\">")
                .append(isEn ? "Projects and Volunteering" : "Проекты и волонтёрство")
                .append("</div>\n");

        for (GenerationResponseProject p : projects) {
            sb.append("  <div class=\"project-block\">\n");
            sb.append("    <div class=\"item-heading\">").append(esc(p.getProjectName()));
            String role = p.getRole();
            if (role != null && !role.isBlank()) {
                sb.append(" – ").append(esc(role));
            }
            sb.append("</div>\n");
            String desc = p.getDescription();
            if (desc != null && !desc.isBlank()) {
                sb.append("    <p>").append(sanitizeAiText(desc)).append("</p>\n");
            }
            sb.append("  </div>\n");
        }
        sb.append("</section>");
        return sb.toString();
    }

    private String buildPersonalInfoSection(GenerationResponsePersonal personal, boolean isEn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section class=\"compact-section\">\n");
        sb.append("  <div class=\"section-title\">")
                .append(isEn ? "Personal Information" : "Личная информация")
                .append("</div>\n");

        if (personal == null) {
            sb.append("  <div class=\"compact-info\"><span>")
                    .append(isEn ? "Personal information available in full resume."
                            : "Личная информация доступна в полном резюме.")
                    .append("</span></div>\n");
        } else {
            sb.append("  <div class=\"compact-info\">");
            appendLabeled(sb, isEn ? "Location" : "Местоположение", personal.getLocation());
            appendLabeled(sb, isEn ? "Languages" : "Языки", personal.getSpokenLanguages());
            appendLabeled(sb, isEn ? "Relocation" : "Переезд", personal.getWillingnessToRelocate());
            appendLabeled(sb, isEn ? "Business trips" : "Командировки",
                    personal.getWillingnessForBusinessTrips());
            appendLabeled(sb, isEn ? "Citizenship" : "Гражданство", personal.getCitizenship());
            if (personal.getWorkFormats() != null && !personal.getWorkFormats().isBlank()) {
                appendLabeled(sb, isEn ? "Work format" : "Формат работы", personal.getWorkFormats());
            }
            if (personal.getDateOfBirth() != null) {
                appendLabeled(sb, isEn ? "Date of birth" : "Дата рождения",
                        personal.getDateOfBirth().toString());
            }
            sb.append("</div>\n");
        }
        sb.append("</section>");
        return sb.toString();
    }

    private void appendLabeled(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) return;
        sb.append("<span><strong>").append(esc(label)).append(":</strong> ")
                .append(esc(value)).append("</span> ");
    }

    // ─── Template loading ──────────────────────────────────────────

    private String loadTemplate(String classpathResource) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathResource);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load template: {}", classpathResource, e);
            throw new RuntimeException("Failed to load resume template: " + classpathResource, e);
        }
    }

    // ─── Sanitization ──────────────────────────────────────────────

    private String sanitizeAiText(String html) {
        if (html == null) return "";
        String result = html;
        result = result.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        result = result.replaceAll("(?is)<style[^>]*>.*?</style>", "");
        result = result.replaceAll("(?i)\\s+on\\w+\\s*=\\s*\"[^\"]*\"", "");
        result = result.replaceAll("(?i)\\s+on\\w+\\s*=\\s*'[^']*'", "");
        return result.trim();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
