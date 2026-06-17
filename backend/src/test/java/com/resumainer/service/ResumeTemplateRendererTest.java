package com.resumainer.service;

import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.GenerationResponsePersonalDao;
import com.resumainer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeTemplateRendererTest {

    @Mock private GeneratedFileStorageService fileStorage;
    @Mock private GenerationResponsePersonalDao personalDao;

    private ResumeTemplateRenderer renderer;
    private GenerationResponseDao.ResponseBundle bundle;
    private Map<String, Object> contactData;

    @BeforeEach
    void setUp() {
        renderer = new ResumeTemplateRenderer(fileStorage, personalDao);

        ResumeGenerationResponse resp = new ResumeGenerationResponse();
        resp.setId(UUID.randomUUID());
        resp.setProfessionalTitle("Senior Java Developer");
        resp.setValueLine("Clean code advocate");
        resp.setProfessionalSummary("Experienced Java developer.");
        resp.setProfessionalAspirations("Seeking technical leadership.");

        bundle = new GenerationResponseDao.ResponseBundle();
        bundle.response = resp;
        bundle.experience = List.of();
        bundle.courses = List.of();
        bundle.projects = List.of();
        bundle.skills = List.of();

        contactData = new LinkedHashMap<>();
        contactData.put("fullName", "Alex Candidate");
        contactData.put("phone", "+7 777 000 00 00");
        contactData.put("resumeEmail", "alex@example.com");
        contactData.put("location", "Astana, Kazakhstan");  // contact fallback

        when(fileStorage.saveFile(anyString(), anyString(), anyString(), anyString())).thenReturn("/out/test.html");
        when(personalDao.findByResponseId(any())).thenReturn(null);
    }

    private String captureHtml() {
        ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
        verify(fileStorage).saveFile(anyString(), anyString(), anyString(), c.capture());
        return c.getValue();
    }

    // ══════════════════════════════════════════════════════════════════
    // BUG 1: Header location uses generated personal location
    // ══════════════════════════════════════════════════════════════════

    @Test
    void headerLocation_usesGeneratedPersonalOverContact() {
        // Generated EN personal location differs from contact
        GenerationResponsePersonal personal = new GenerationResponsePersonal();
        personal.setLocation("Astana, Kazakhstan");  // EN generated
        personal.setSpokenLanguages("English, Russian");
        when(personalDao.findByResponseId(any())).thenReturn(personal);

        // Contact location is different (Russian)
        contactData.put("location", "Астана, Казахстан");

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        // EN header must use generated EN location
        assertTrue(html.contains("Astana, Kazakhstan"),
                "Header must use generated personal location");
        // Must NOT use Russian contact fallback in header
        String header = extractHeader(html);
        assertNotNull(header);
        assertFalse(header.contains("Астана, Казахстан"),
                "Header must NOT use contact location when generated exists");
    }

    @Test
    void headerLocation_fallsBackToContactWhenGeneratedMissing() {
        // No personal data
        when(personalDao.findByResponseId(any())).thenReturn(null);

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        // Should fall back to contact location
        assertTrue(html.contains("Astana, Kazakhstan"),
                "Must fall back to contact location when generated missing");
    }

    @Test
    void headerLocation_russianUsesGeneratedRussianPersonalLocation() {
        GenerationResponsePersonal personal = new GenerationResponsePersonal();
        personal.setLocation("Астана, Казахстан");  // RU generated
        when(personalDao.findByResponseId(any())).thenReturn(personal);

        // Contact has EN location
        contactData.put("location", "Astana, Kazakhstan");

        renderer.renderAndSave(bundle, List.of(), contactData, "RU", "BALANCED", "test", "ABC");
        String html = captureHtml();

        String header = extractHeader(html);
        assertNotNull(header);
        assertTrue(header.contains("Астана, Казахстан"),
                "RU header must use generated RU location");
        assertFalse(header.contains("Astana, Kazakhstan"),
                "RU header must NOT use contact EN location when RU generated exists");
    }

    // ══════════════════════════════════════════════════════════════════
    // BUG 2: Work Experience split by isFirstPage
    // ══════════════════════════════════════════════════════════════════

    @Test
    void workExperience_splitByIsFirstPage_3primary7additional() {
        List<GenerationResponseExperience> exps = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            exps.add(makeExp("Role work-" + i, "Co", "Desc " + i,
                    LocalDate.of(2020,1,1), null, i <= 3));
        }
        bundle.experience = exps;
        // Need projects to trigger two-page template
        bundle.projects = List.of(makeProject("P", "D"));

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        String page1 = extractPage(html, "page-1");
        String page2 = extractPage(html, "page-2");

        assertNotNull(page1, "Must have page 1");
        assertNotNull(page2, "Must have page 2");

        // Page 1 must have work 1-3, not 4+
        assertTrue(page1.contains("Role work-1"), "Page1 must contain work-1");
        assertTrue(page1.contains("Role work-2"), "Page1 must contain work-2");
        assertTrue(page1.contains("Role work-3"), "Page1 must contain work-3");
        assertFalse(page1.contains("Role work-4"), "Page1 must NOT contain work-4");

        // Page 2 must have work 4-10
        assertTrue(page2.contains("Role work-4"), "Page2 must contain work-4");
        assertTrue(page2.contains("Role work-10"), "Page2 must contain work-10");
    }

    @Test
    void workExperience_allIsFirstPageTrue_allOnPage1() {
        List<GenerationResponseExperience> exps = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            exps.add(makeExp("Role w" + i, "Co", "D", LocalDate.of(2020,1,1), null, true));
        }
        bundle.experience = exps;
        bundle.projects = List.of(makeProject("P", "D"));

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        String page1 = extractPage(html, "page-1");

        // All 5 on page 1
        for (int i = 1; i <= 5; i++) {
            assertTrue(page1.contains("Role w" + i), "All first-page records on Page1");
        }
    }

    @Test
    void workExperience_noIsFirstPageTrue_fallsBackToLegacySplit() {
        // All isFirstPage=false → fallback: first 2 on page 1, rest on page 2
        List<GenerationResponseExperience> exps = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            exps.add(makeExp("Role w" + i, "Co", "D", LocalDate.of(2020,1,1), null, false));
        }
        bundle.experience = exps;
        bundle.projects = List.of(makeProject("P", "D"));

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        String page1 = extractPage(html, "page-1");
        String page2 = extractPage(html, "page-2");

        assertTrue(page1.contains("Role w1"), "Fallback: first 2 on page1");
        assertTrue(page1.contains("Role w2"));
        assertFalse(page1.contains("Role w3"));

        assertTrue(page2.contains("Role w3"));
        assertTrue(page2.contains("Role w5"));
    }

    @Test
    void workExperience_preservesInsertionOrder() {
        List<GenerationResponseExperience> exps = new ArrayList<>();
        exps.add(makeExp("Role A", "Co", "D", LocalDate.of(2020,1,1), null, true));
        exps.add(makeExp("Role B", "Co", "D", LocalDate.of(2020,1,1), null, false));
        exps.add(makeExp("Role C", "Co", "D", LocalDate.of(2020,1,1), null, true));
        exps.add(makeExp("Role D", "Co", "D", LocalDate.of(2020,1,1), null, false));
        bundle.experience = exps;
        bundle.projects = List.of(makeProject("P", "D"));

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        String page1 = extractPage(html, "page-1");

        // Order preserved: A before C on page 1
        int posA = page1.indexOf("Role A");
        int posC = page1.indexOf("Role C");
        assertTrue(posA < posC, "Role A must appear before Role C on Page 1");
    }

    @Test
    void workExperience_emptyList_noCrash() {
        bundle.experience = List.of();
        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertNotNull(html);
    }

    // ═══ Edge cases ══════════════════════════════════════════════════

    @Test
    void shouldUseTwoPage_whenCoursesExceed5() {
        List<GenerationResponseCourse> courses = new ArrayList<>();
        for (int i = 0; i < 7; i++) courses.add(makeCourse("Course " + i));
        bundle.courses = courses;

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertTrue(html.contains("page-2"), ">5 courses triggers two-page");
    }

    @Test
    void contactLine_omitsMissingFieldsGracefully() {
        contactData = new LinkedHashMap<>();
        contactData.put("fullName", "Alex");
        // No phone, no email, no location

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        // Should still render without crashing
        assertTrue(html.contains("Alex"), "Name still present");
        // Contact line div should be empty/absent since no data
        String header = extractHeader(html);
        assertNotNull(header);
    }

    @Test
    void workExperience_withLocation_includesLocation() {
        GenerationResponseExperience exp = makeExp("Dev", "Co", "Desc",
                LocalDate.of(2020,1,1), null, true);
        exp.setLocation("Remote");
        bundle.experience = List.of(exp);

        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertTrue(html.contains("Remote"), "Location should appear in job block");
    }

    @Test
    void workExperience_presentLabel_russian() {
        GenerationResponseExperience exp = makeExp("Dev", "Co", "Desc",
                LocalDate.of(2023,1,1), null, true);
        bundle.experience = List.of(exp);

        renderer.renderAndSave(bundle, List.of(), contactData, "RU", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertTrue(html.contains("Наст. время"), "Russian present label");
    }

    // ═══ Education colon format tests ════════════════════════════════

    @Test
    void education_englishUsesColonNotInPreposition() {
        Map<String, Object> edu = new LinkedHashMap<>();
        edu.put("institutionNameEn", "MIT");
        edu.put("degreeEn", "Bachelor");
        edu.put("fieldOfStudyEn", "Information Systems");
        renderer.renderAndSave(bundle, List.of(edu), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        assertTrue(html.contains("Bachelor: Information Systems"),
                "EN: colon between degree and field");
        assertFalse(html.contains("Bachelor in Information Systems"),
                "EN: must NOT use 'in' preposition");
    }

    @Test
    void education_russianUsesColonNotInPreposition() {
        Map<String, Object> edu = new LinkedHashMap<>();
        edu.put("institutionNameRu", "МГУ");
        edu.put("degreeRu", "Бакалавр");
        edu.put("fieldOfStudyRu", "Информационные системы");
        renderer.renderAndSave(bundle, List.of(edu), contactData, "RU", "BALANCED", "test", "ABC");
        String html = captureHtml();

        assertTrue(html.contains("Бакалавр: Информационные системы"),
                "RU: colon between degree and field");
        assertFalse(html.contains("Бакалавр in Информационные системы"),
                "RU: must NOT use 'in' preposition");
    }

    @Test
    void education_noInPrepositionAnywhere() {
        Map<String, Object> edu = new LinkedHashMap<>();
        edu.put("institutionNameEn", "MIT");
        edu.put("degreeEn", "Master");
        edu.put("fieldOfStudyEn", "Business Analytics");
        renderer.renderAndSave(bundle, List.of(edu), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();

        // Extract the education line content
        assertFalse(html.contains("Master in Business"),
                "Must not use 'in' preposition between degree and field");
    }

    // ══════════════════════════════════════════════════════════════════
    // Russian template tests
    // ══════════════════════════════════════════════════════════════════

    @Test
    void russian_usesRussianLabels() {
        bundle.projects = List.of(makeProject("P", "D"));
        bundle.experience = List.of(
                makeExp("Dev", "Co", "Desc", LocalDate.of(2020,1,1), null, true));
        bundle.courses = List.of(makeCourse("Java"));
        bundle.skills = List.of(makeSkill("Backend", "Java"));

        renderer.renderAndSave(bundle, List.of(), contactData, "RU", "BALANCED", "test", "ABC");
        String html = captureHtml();

        assertTrue(html.contains("Опыт работы"), "RU label for Work Experience");
        assertTrue(html.contains("Навыки"), "RU label for Skills");
        assertTrue(html.contains("Образование"), "RU label for Education");
        assertTrue(html.contains("Курсы и сертификаты"), "RU label for Courses");
        assertTrue(html.contains("Профессиональные цели"), "RU label for Aspirations");
        assertTrue(html.contains("Личная информация"), "RU label for Personal Info");
    }

    @Test
    void russian_pageNotesAreRussian() {
        bundle.projects = List.of(makeProject("P", "D"));
        renderer.renderAndSave(bundle, List.of(), contactData, "RU", "BALANCED", "test", "ABC");
        String html = captureHtml();

        assertTrue(html.contains("См. следующую страницу"));
        assertTrue(html.contains("См. предыдущую страницу"));
        assertFalse(html.contains("See the next page"), "No EN page notes in RU output");
    }

    // ══════════════════════════════════════════════════════════════════
    // Basic structure tests (preserved from before)
    // ══════════════════════════════════════════════════════════════════

    @Test
    void twoPage_hasFullStructure() {
        bundle.projects = List.of(makeProject("Test", "Desc"));
        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains("@page"));
        assertTrue(html.contains("page-1"));
        assertTrue(html.contains("page-2"));
        assertTrue(html.contains("<script>"));
    }

    @Test
    void noPlaceholderText() {
        bundle.projects = List.of(makeProject("P", "D"));
        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertFalse(html.contains("Candidate Name"));
        assertFalse(html.contains("Contact information"));
        assertFalse(html.contains("<!-- RESUME:"));
    }

    @Test
    void personalInfo_hasCompactSectionClass() {
        GenerationResponsePersonal p = new GenerationResponsePersonal();
        p.setLocation("Astana");
        when(personalDao.findByResponseId(any())).thenReturn(p);
        renderer.renderAndSave(bundle, List.of(), contactData, "EN", "BALANCED", "test", "ABC");
        String html = captureHtml();
        assertTrue(html.contains("compact-section"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════

    private String extractHeader(String html) {
        int start = html.indexOf("<div class=\"candidate-name\">");
        int end = html.indexOf("</section>", start);
        return (start >= 0 && end > start) ? html.substring(start, end) : null;
    }

    private String extractPage(String html, String pageClass) {
        int start = html.indexOf("class=\"resume-page " + pageClass + "\"");
        if (start < 0) start = html.indexOf("class=\"resume-page " + pageClass);
        if (start < 0) return null;
        // Find closing </main> for this page
        int mainStart = html.lastIndexOf("<main", start);
        int mainEnd = html.indexOf("</main>", start);
        if (mainEnd < 0) return null;
        return html.substring(start, mainEnd);
    }

    private GenerationResponseExperience makeExp(String title, String company, String desc,
                                                   LocalDate start, LocalDate end, boolean isFirstPage) {
        GenerationResponseExperience e = new GenerationResponseExperience();
        e.setJobTitle(title);
        e.setCompanyName(company);
        e.setDescription(desc);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setFirstPage(isFirstPage);
        return e;
    }

    private GenerationResponseProject makeProject(String name, String desc) {
        GenerationResponseProject p = new GenerationResponseProject();
        p.setProjectName(name);
        p.setDescription(desc);
        return p;
    }

    private GenerationResponseCourse makeCourse(String name) {
        GenerationResponseCourse c = new GenerationResponseCourse();
        c.setName(name);
        return c;
    }

    private GenerationResponseSkill makeSkill(String group, String name) {
        GenerationResponseSkill s = new GenerationResponseSkill();
        s.setSkillGroup(group);
        s.setSkillName(name);
        return s;
    }
}
