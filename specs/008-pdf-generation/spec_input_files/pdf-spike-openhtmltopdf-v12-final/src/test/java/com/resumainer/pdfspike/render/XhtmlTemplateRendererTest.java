package com.resumainer.pdfspike.render;

import com.resumainer.pdfspike.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class XhtmlTemplateRendererTest {
    @Test void splitsContactIntoTwoLines() {
        String html = render(twoPagePlan(sample()));
        assertTrue(html.contains("+7 | e@test.com | Astana | +7wa"));
        assertTrue(html.contains("https://li | https://portfolio | @tg"));
    }

    @Test void rendersProjectsBeforeAdditionalWork() {
        String html = render(twoPagePlan(sample()));
        assertTrue(html.indexOf("Projects and Volunteering") < html.indexOf("Additional Work Experience"));
    }

    @Test void skipsCourseFocusWhenBlank() {
        String html = render(twoPagePlan(sample()));
        assertTrue(html.contains("Course A | Provider | focus words here"));
        assertTrue(html.contains("Course B | Provider</div>"));
    }


    @Test void rendersTwoPageNavigationNotes() {
        String html = render(twoPagePlan(sample()));
        assertTrue(html.contains("page-note-bottom"));
        assertTrue(html.contains("See the next page"));
        assertTrue(html.contains("page-note-top"));
        assertTrue(html.contains("See the previous page"));
    }

    @Test void usesFixedA4PageHeightForAbsoluteFooterNote() {
        String html = render(twoPagePlan(sample()));
        assertTrue(html.contains("height:297mm;min-height:297mm"));
    }

    private String render(PagePlan plan) {
        return new XhtmlTemplateRenderer().render(sample(), plan, new FitState(12.5,1.35,1.35,1.35,15,15,15,9,5,3));
    }

    private PagePlan twoPagePlan(ResumeData d) {
        EdgeCaseRule rule = new EdgeCaseRule(11,4,4,2,2,TemplateMode.TWO_PAGE,2,2,4,"test");
        return new PagePlan(rule,2,d.workExperience().subList(0,2),d.workExperience().subList(2,4),d.projects(),true);
    }

    private ResumeData sample() {
        List<WorkExperience> we = new ArrayList<>();
        for (int i = 0; i < 4; i++) we.add(new WorkExperience("role"+i,"company","loc","start","end","desc",List.of("bullet")));
        List<ProjectItem> ps = List.of(new ProjectItem("Project","Role","Remote","2026","2027","desc",List.of("bullet")));
        return new ResumeData(Language.EN,1,"Name","Title","+7","e@test.com","Astana","https://li","https://portfolio","@tg","+7wa","Value","Summary",we,List.of(new SkillGroup("Skill",List.of("A","B"))),List.of("Edu"),List.of(new CourseItem("Course A","Provider","focus words here"),new CourseItem("Course B","Provider","")),ps,"Aspirations","P1","P2","P3");
    }
}
