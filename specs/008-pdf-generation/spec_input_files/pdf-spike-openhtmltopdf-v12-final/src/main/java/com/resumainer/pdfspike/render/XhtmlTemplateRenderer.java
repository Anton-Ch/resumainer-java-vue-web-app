package com.resumainer.pdfspike.render;

import com.resumainer.pdfspike.model.*;
import com.resumainer.pdfspike.util.Escape;

import java.util.List;
import java.util.StringJoiner;

public final class XhtmlTemplateRenderer {
    public String render(ResumeData data, PagePlan plan, FitState fit) {
        StringBuilder html = new StringBuilder(24000);
        html.append("<!DOCTYPE html><html lang=\"").append(data.language() == Language.RU ? "ru" : "en").append("\"><head><meta charset=\"utf-8\"/>");
        html.append("<title>ResumAIner PDF Spike V12</title><style>").append(css(fit)).append("</style></head><body>");
        renderPage1(html, data, plan, fit);
        if (plan.targetPageCount() >= 2) renderPage2(html, data, plan, fit);
        if (plan.targetPageCount() >= 3) renderPage3(html, data, plan, fit);
        html.append("</body></html>");
        return html.toString();
    }

    public String renderSinglePage(ResumeData data, PagePlan plan, FitState fit, int plannedPageNumber) {
        StringBuilder html = new StringBuilder(16000);
        html.append("<!DOCTYPE html><html lang=\"").append(data.language() == Language.RU ? "ru" : "en").append("\"><head><meta charset=\"utf-8\"/>");
        html.append("<title>ResumAIner PDF Spike V12 Page ").append(plannedPageNumber).append("</title><style>")
                .append(css(fit))
                .append("main.resume-page{page-break-after:auto;}")
                .append("</style></head><body>");
        if (plannedPageNumber == 1) renderPage1(html, data, plan, fit);
        else if (plannedPageNumber == 2) renderPage2(html, data, plan, fit);
        else if (plannedPageNumber == 3) renderPage3(html, data, plan, fit);
        else throw new IllegalArgumentException("Unsupported planned page number: " + plannedPageNumber);
        html.append("</body></html>");
        return html.toString();
    }

    private String css(FitState f) {
        return "@page{size:A4;margin:0;}" +
                "@font-face{font-family:'Inter';src:url('fonts/Inter-400.ttf');font-weight:400;}" +
                "@font-face{font-family:'Inter';src:url('fonts/Inter-600.ttf');font-weight:600;}" +
                "@font-face{font-family:'Inter';src:url('fonts/Inter-700.ttf');font-weight:700;}" +
                "@font-face{font-family:'Manrope';src:url('fonts/Manrope-700.ttf');font-weight:700;}" +
                "body{margin:0;background:#f5f6f8;color:#111;font-family:'Inter',Arial,Helvetica,sans-serif;}" +
                "main.resume-page{width:210mm;height:297mm;min-height:297mm;box-sizing:border-box;background:#fff;page-break-after:always;position:relative;}" +
                "main.resume-page:last-child{page-break-after:auto;}" +
                ".page-content{box-sizing:border-box;padding:10.5mm;font-size:" + px(f.bodyFontPx()) + ";}" +
                ".has-next .page-content{padding-bottom:14mm;}" +
                ".has-prev .page-content{padding-top:14mm;}" +
                ".page-1 .page-content{line-height:" + n(f.page1LineHeight()) + ";}" +
                ".page-2 .page-content{line-height:" + n(f.page2LineHeight()) + ";}" +
                ".page-3 .page-content{line-height:" + n(f.page3LineHeight()) + ";}" +
                ".page-1 section{margin:0 0 " + px(f.page1SectionGapPx()) + " 0;}" +
                ".page-2 section{margin:0 0 " + px(f.page2SectionGapPx()) + " 0;}" +
                ".page-3 section{margin:0 0 " + px(f.page3SectionGapPx()) + " 0;}" +
                ".candidate-name{font-family:'Manrope','Inter',Arial,sans-serif;font-size:" + px(f.bodyFontPx() + 8) + ";font-weight:700;text-transform:uppercase;line-height:1;letter-spacing:.4px;margin:0 0 2px 0;}" +
                ".candidate-title{font-family:'Manrope','Inter',Arial,sans-serif;font-size:" + px(f.bodyFontPx() + 4) + ";font-weight:700;color:#1f2937;margin:0 0 5px 0;}" +
                ".contact-line{font-size:" + px(Math.max(9.0, f.bodyFontPx() - 1.5)) + ";color:#2b2b2b;margin:0 0 2px 0;}" +
                ".value-line{margin:6px 0 0 0;padding:5px 0;border-top:1px solid #1f2937;border-bottom:1px solid #1f2937;font-size:" + px(Math.max(10.0, f.bodyFontPx() + 1.0)) + ";font-weight:700;text-transform:uppercase;letter-spacing:.2px;}" +
                ".section-title{font-family:'Manrope','Inter',Arial,sans-serif;font-size:" + px(f.bodyFontPx() + 1.5) + ";font-weight:700;text-transform:uppercase;letter-spacing:.6px;margin:0 0 5px 0;padding-bottom:3px;border-bottom:1px solid #444;}" +
                "p{margin:0 0 " + px(f.paragraphGapPx()) + " 0;}" +
                ".item-block{margin:0 0 " + px(f.itemGapPx()) + " 0;page-break-inside:avoid;}" +
                ".item-title{font-size:" + px(f.bodyFontPx() + 0.8) + ";font-weight:700;margin:0 0 1px 0;}" +
                ".item-subtitle{font-size:" + px(Math.max(9.0, f.bodyFontPx() - 1.2)) + ";font-weight:600;color:#1f2937;margin:0 0 2px 0;}" +
                "ul{margin:2px 0 0 0;padding-left:17px;}li{margin:0 0 " + px(f.bulletGapPx()) + " 0;padding-left:1px;}" +
                ".skill-group,.education-line,.course-line,.info-line{margin:0 0 " + px(Math.max(2.0, f.paragraphGapPx())) + " 0;}" +
                ".label{font-weight:700;}" +
                ".page-note-top,.page-note-bottom{position:absolute;left:10.5mm;right:10.5mm;min-height:7mm;box-sizing:border-box;text-align:center;color:#666;font-size:" + px(Math.max(8.8, f.bodyFontPx() - 2)) + ";font-weight:700;text-transform:uppercase;letter-spacing:.3px;padding:4px 0;border-color:#ccc;background:#fff;}" +
                ".page-note-top{top:4mm;border-bottom:1px solid #ccc;}" +
                ".page-note-bottom{bottom:4mm;border-top:1px solid #ccc;}";
    }

    private void renderPage1(StringBuilder html, ResumeData data, PagePlan plan, FitState fit) {
        html.append("<main class=\"resume-page page-1").append(plan.targetPageCount() > 1 ? " has-next" : "").append("\"><div class=\"page-content\">");
        renderHeader(html, data);
        section(html, label(data, "Professional Summary", "О себе"), "<p>" + Escape.html(data.summary()) + "</p>");
        renderWork(html, data, label(data, "Work Experience", "Опыт работы"), plan.page1Work());
        renderSkills(html, data);
        renderEducation(html, data);
        renderCourses(html, data);
        if (plan.targetPageCount() == 1) {
            renderAspirations(html, data);
            renderPersonal(html, data);
        }
        html.append("</div>");
        if (plan.targetPageCount() > 1) html.append("<div class=\"page-note-bottom\">").append(Escape.html(label(data, "See the next page", "См. следующую страницу"))).append("</div>");
        html.append("</main>");
    }

    private void renderPage2(StringBuilder html, ResumeData data, PagePlan plan, FitState fit) {
        html.append("<main class=\"resume-page page-2 has-prev").append(plan.targetPageCount() > 2 ? " has-next" : "").append("\"><div class=\"page-note-top\">")
                .append(Escape.html(label(data, "See the previous page", "См. предыдущую страницу"))).append("</div><div class=\"page-content\">");
        if (!plan.page2Projects().isEmpty()) renderProjects(html, data, plan.page2Projects());
        if (!plan.page2AdditionalWork().isEmpty()) renderWork(html, data, label(data, "Additional Work Experience", "Дополнительный опыт работы"), plan.page2AdditionalWork());
        if (plan.targetPageCount() <= 2) {
            renderAspirations(html, data);
            renderPersonal(html, data);
        }
        html.append("</div>");
        if (plan.targetPageCount() > 2) html.append("<div class=\"page-note-bottom\">").append(Escape.html(label(data, "See the next page", "См. следующую страницу"))).append("</div>");
        html.append("</main>");
    }

    private void renderPage3(StringBuilder html, ResumeData data, PagePlan plan, FitState fit) {
        html.append("<main class=\"resume-page page-3 has-prev\"><div class=\"page-note-top\">").append(Escape.html(label(data, "See the previous page", "См. предыдущую страницу"))).append("</div><div class=\"page-content\">");
        renderAspirations(html, data);
        renderPersonal(html, data);
        html.append("</div></main>");
    }

    private void renderHeader(StringBuilder html, ResumeData data) {
        html.append("<section><div class=\"candidate-name\">").append(Escape.html(data.fullName())).append("</div>");
        html.append("<div class=\"candidate-title\">").append(Escape.html(data.title())).append("</div>");
        html.append("<div class=\"contact-line\">").append(join(data.phone(), data.email(), data.location(), data.whatsapp())).append("</div>");
        html.append("<div class=\"contact-line\">").append(join(data.linkedin(), data.portfolio(), data.telegram())).append("</div>");
        html.append("<div class=\"value-line\">").append(Escape.html(data.valueLine())).append("</div></section>");
    }

    private void renderWork(StringBuilder html, ResumeData data, String title, List<WorkExperience> items) {
        if (items.isEmpty()) return;
        StringBuilder body = new StringBuilder();
        for (WorkExperience w : items) {
            body.append("<div class=\"item-block\"><div class=\"item-title\">").append(Escape.html(w.role())).append(" | ").append(Escape.html(w.company())).append("</div>");
            body.append("<div class=\"item-subtitle\">").append(join(w.location(), w.startDate() + " - " + w.endDate())).append("</div>");
            if (!w.description().isBlank()) body.append("<p>").append(Escape.html(w.description())).append("</p>");
            if (!w.bullets().isEmpty()) {
                body.append("<ul>");
                for (String b : w.bullets()) body.append("<li>").append(Escape.html(b)).append("</li>");
                body.append("</ul>");
            }
            body.append("</div>");
        }
        section(html, title, body.toString());
    }

    private void renderSkills(StringBuilder html, ResumeData data) {
        StringBuilder b = new StringBuilder();
        for (SkillGroup g : data.skills()) b.append("<div class=\"skill-group\"><span class=\"label\">").append(Escape.html(g.label())).append(":</span> ").append(Escape.html(String.join(", ", g.skills()))).append("</div>");
        section(html, label(data, "Skills", "Навыки"), b.toString());
    }

    private void renderEducation(StringBuilder html, ResumeData data) {
        StringBuilder b = new StringBuilder();
        for (String e : data.education()) b.append("<div class=\"education-line\">").append(Escape.html(e)).append("</div>");
        section(html, label(data, "Education", "Образование"), b.toString());
    }

    private void renderCourses(StringBuilder html, ResumeData data) {
        if (data.courses().isEmpty()) return;
        StringBuilder b = new StringBuilder();
        for (CourseItem c : data.courses()) {
            String line = c.name() + " | " + c.provider() + (c.focus() == null || c.focus().isBlank() ? "" : " | " + c.focus());
            b.append("<div class=\"course-line\">").append(Escape.html(line)).append("</div>");
        }
        section(html, label(data, "Courses and Certifications", "Курсы и сертификаты"), b.toString());
    }

    private void renderProjects(StringBuilder html, ResumeData data, List<ProjectItem> projects) {
        if (projects.isEmpty()) return;
        StringBuilder b = new StringBuilder();
        for (ProjectItem p : projects) {
            b.append("<div class=\"item-block\"><div class=\"item-title\">").append(Escape.html(p.title())).append(" | ").append(Escape.html(p.role())).append("</div>");
            b.append("<div class=\"item-subtitle\">").append(join(p.location(), p.startDate() + " - " + p.endDate())).append("</div>");
            b.append("<p>").append(Escape.html(p.description())).append("</p><ul>");
            for (String item : p.bullets()) b.append("<li>").append(Escape.html(item)).append("</li>");
            b.append("</ul></div>");
        }
        section(html, label(data, "Projects and Volunteering", "Проекты и волонтёрство"), b.toString());
    }

    private void renderAspirations(StringBuilder html, ResumeData data) {
        if (data.aspirations() == null || data.aspirations().isBlank()) return;
        section(html, label(data, "Professional Aspirations", "Профессиональные цели"), "<p>" + Escape.html(data.aspirations()) + "</p>");
    }

    private void renderPersonal(StringBuilder html, ResumeData data) {
        StringBuilder body = new StringBuilder();
        if (data.personalLine1() != null && !data.personalLine1().isBlank()) body.append("<div class=\"info-line\">").append(Escape.html(data.personalLine1())).append("</div>");
        if (data.personalLine2() != null && !data.personalLine2().isBlank()) body.append("<div class=\"info-line\">").append(Escape.html(data.personalLine2())).append("</div>");
        if (data.personalLine3() != null && !data.personalLine3().isBlank()) body.append("<div class=\"info-line\">").append(Escape.html(data.personalLine3())).append("</div>");
        if (body.length() > 0) section(html, label(data, "Personal Information", "Персональная информация"), body.toString());
    }

    private void section(StringBuilder html, String title, String body) { html.append("<section><div class=\"section-title\">").append(Escape.html(title)).append("</div>").append(body).append("</section>"); }
    private String label(ResumeData d, String en, String ru) { return d.language() == Language.RU ? ru : en; }
    private String join(String... values) { StringJoiner j = new StringJoiner(" | "); for (String v : values) if (v != null && !v.isBlank()) j.add(Escape.html(v)); return j.toString(); }
    private String px(double v) { return n(v) + "px"; }
    private String n(double v) { return String.format(java.util.Locale.ROOT, "%.2f", v); }
}
