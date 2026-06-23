package com.resumainer.service.pdf;

import com.resumainer.model.pdf.FitState;
import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XhtmlTemplateRendererFooterSafeZoneTest {

    @Test
    void render_multiPageUsesExpandedBottomSafeZoneForFooterNote() {
        XhtmlTemplateRenderer renderer = new XhtmlTemplateRenderer();
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode("EN");
        data.setFullName("Vasya Pupkin");
        data.setProfessionalTitle("Senior Java Developer");
        data.setProfessionalSummary("Summary");

        PagePlan plan = new PagePlan();
        plan.setTargetPageCount(2);
        plan.setPage1WorkCount(0);
        plan.setPage2AdditionalWorkCount(0);
        plan.setPage2ProjectCount(0);

        FitState fit = new FitState();
        fit.setBodyFontPx(12.5);
        fit.setPage1LineHeight(1.22);
        fit.setPage2LineHeight(1.35);
        fit.setPage3LineHeight(1.35);
        fit.setPage1SectionGapPx(13.5);
        fit.setPage2SectionGapPx(15.0);
        fit.setPage3SectionGapPx(15.0);
        fit.setItemGapPx(8.1);
        fit.setParagraphGapPx(4.5);
        fit.setBulletGapPx(2.7);

        String html = renderer.render(data, plan, fit);

        assertTrue(html.contains(".has-next .page-content{padding-bottom:20mm;}"));
        assertTrue(html.contains("page-note-bottom"));
        assertTrue(html.contains("See the next page"));
    }
}
