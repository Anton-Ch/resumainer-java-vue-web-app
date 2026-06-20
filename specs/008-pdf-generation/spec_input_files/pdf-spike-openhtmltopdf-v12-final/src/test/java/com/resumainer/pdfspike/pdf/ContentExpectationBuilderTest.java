package com.resumainer.pdfspike.pdf;

import com.resumainer.pdfspike.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ContentExpectationBuilderTest {
    @Test void includesPersonalInformationLinesToCatchClipping() {
        ResumeData data = sample(Language.RU, "asp", "Местоположение: Астана", "Языки: Русский", "Формат работы: Гибрид");
        PagePlan plan = twoPagePlan(data);
        List<String> expected = new ContentExpectationBuilder().build(data, plan);
        assertTrue(expected.contains("Местоположение: Астана"));
        assertTrue(expected.contains("Языки: Русский"));
        assertTrue(expected.contains("Формат работы: Гибрид"));
    }

    @Test void onePagePlannedPageRequiresAspirationsAndNonBlankPersonalLines() {
        ResumeData data = sample(Language.RU, "asp", "Местоположение: Астана", "Языки: Русский", "Формат работы: Гибрид");
        PagePlan plan = onePagePlan(data);
        List<String> expected = new ContentExpectationBuilder().buildForPlannedPage(data, plan, 1);
        assertTrue(expected.contains("Профессиональные цели"));
        assertTrue(expected.contains("Персональная информация"));
        assertTrue(expected.contains("asp"));
        assertTrue(expected.contains("Формат работы: Гибрид"));
    }

    @Test void skipsBlankOptionalPersonalLines() {
        ResumeData data = sample(Language.EN, "asp", "Location: Astana", "", null);
        PagePlan plan = onePagePlan(data);
        List<String> expected = new ContentExpectationBuilder().buildForPlannedPage(data, plan, 1);
        assertTrue(expected.contains("Location: Astana"));
        assertFalse(expected.contains(""));
        assertFalse(expected.contains(null));
    }

    @Test void longAspirationsUseGenericGeneratedAnchorsInsteadOfFullHardcodedParagraph() {
        String aspirations = "I want to grow through complex systems, reliable delivery, product thinking, better automation, clear APIs, useful tools, team ownership, quality data, careful testing, and maintainable architecture.";
        ResumeData data = sample(Language.EN, aspirations, "Location: Astana", "Languages: English", "Work Format: Remote");
        PagePlan plan = onePagePlan(data);
        List<String> expected = new ContentExpectationBuilder().buildForPlannedPage(data, plan, 1);
        assertFalse(expected.contains(aspirations));
        assertTrue(expected.stream().anyMatch(e -> e.startsWith("I want to grow")));
        assertTrue(expected.stream().anyMatch(e -> e.contains("testing and maintainable architecture")));
    }

    private PagePlan twoPagePlan(ResumeData data) {
        EdgeCaseRule rule = new EdgeCaseRule(1,1,1,1,1,TemplateMode.TWO_PAGE,1,0,1,"test");
        return new PagePlan(rule,2,data.workExperience(),List.of(),data.projects(),true);
    }

    private PagePlan onePagePlan(ResumeData data) {
        EdgeCaseRule rule = new EdgeCaseRule(1,1,1,0,1,TemplateMode.ONE_PAGE,1,0,1,"test");
        return new PagePlan(rule,1,data.workExperience(),List.of(),List.of(),true);
    }

    private ResumeData sample(Language language, String aspirations, String personalLine1, String personalLine2, String personalLine3) {
        return new ResumeData(language,1,"Кандидат","Титул","p","e","l","li","po","tg","wa","v","summary",
                List.of(new WorkExperience("Роль","Компания","л","с","е","д",List.of())),
                List.of(),List.of(),List.of(new CourseItem("Курс","Провайдер","Фокус")),
                List.of(new ProjectItem("Проект","Роль","л","с","е","д",List.of())),
                aspirations,personalLine1,personalLine2,personalLine3);
    }
}
