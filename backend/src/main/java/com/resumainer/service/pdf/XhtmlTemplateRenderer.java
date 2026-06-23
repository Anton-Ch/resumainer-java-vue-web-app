package com.resumainer.service.pdf;

import com.resumainer.model.pdf.FitState;
import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Renders resume data into XHTML suitable for PDF generation via OpenHTMLToPDF.
 * Uses PDF-safe CSS only (no flexbox, no grid, no overflow:hidden).
 * Produces HTML and PDF parity: both use the same template with page navigation notes.
 *
 * Ported from spike V12.1 XhtmlTemplateRenderer. Adapted: spike ResumeData/PagePlan
 * types replaced with production ResumeRenderData/PagePlan. Work/project items
 * come pre-split from caller (based on PagePlan counts from budget resolver).
 */
public final class XhtmlTemplateRenderer {

    /** Render full multi-page HTML. */
    public String render(ResumeRenderData data, PagePlan plan, FitState fit) {
        StringBuilder html = new StringBuilder(24000);
        String lang = "RU".equalsIgnoreCase(data.getLanguageCode()) ? "ru" : "en";
        html.append("<!DOCTYPE html><html lang=\"").append(lang).append("\"><head><meta charset=\"utf-8\"/>");
        html.append("<title>Resume</title><style>").append(css(fit)).append("</style></head><body>");
        renderPage1(html, data, plan, fit);
        if (plan.getTargetPageCount() >= 2) renderPage2(html, data, plan, fit);
        if (plan.getTargetPageCount() >= 3) renderPage3(html, data, plan, fit);
        html.append("</body></html>");
        return html.toString();
    }

    /** Render single isolated page (for page-by-page PDF generation). */
    public String renderSinglePage(ResumeRenderData data, PagePlan plan, FitState fit, int pageNumber) {
        StringBuilder html = new StringBuilder(16000);
        String lang = "RU".equalsIgnoreCase(data.getLanguageCode()) ? "ru" : "en";
        html.append("<!DOCTYPE html><html lang=\"").append(lang).append("\"><head><meta charset=\"utf-8\"/>");
        html.append("<title>Resume Page ").append(pageNumber).append("</title><style>")
                .append(css(fit)).append("main.resume-page{page-break-after:auto;}").append("</style></head><body>");
        switch (pageNumber) {
            case 1: renderPage1(html, data, plan, fit); break;
            case 2: renderPage2(html, data, plan, fit); break;
            case 3: renderPage3(html, data, plan, fit); break;
            default: throw new IllegalArgumentException("Unsupported page number: " + pageNumber);
        }
        html.append("</body></html>");
        return html.toString();
    }

    // ── CSS (PDF-safe only, ported from spike) ──────────────────────

    private String css(FitState f) {
        return "@page{size:A4;margin:0;}"
                + "@font-face{font-family:'Inter';src:url('Inter-400.ttf');font-weight:400;}"
                + "@font-face{font-family:'Inter';src:url('Inter-600.ttf');font-weight:600;}"
                + "@font-face{font-family:'Inter';src:url('Inter-700.ttf');font-weight:700;}"
                + "@font-face{font-family:'Manrope';src:url('Manrope-700.ttf');font-weight:700;}"
                + "body{margin:0;background:#f5f6f8;color:#111;font-family:'Inter',Arial,Helvetica,sans-serif;}"
                + "main.resume-page{width:210mm;height:297mm;min-height:297mm;box-sizing:border-box;background:#fff;page-break-after:always;position:relative;}"
                + "main.resume-page:last-child{page-break-after:auto;}"
                + ".page-content{box-sizing:border-box;padding:10.5mm;font-size:" + px(f.getBodyFontPx()) + ";}"
                + ".has-next .page-content{padding-bottom:20mm;}.has-prev .page-content{padding-top:14mm;}"
                + ".page-1 .page-content{line-height:" + n(f.getPage1LineHeight()) + ";}"
                + ".page-2 .page-content{line-height:" + n(f.getPage2LineHeight()) + ";}"
                + ".page-3 .page-content{line-height:" + n(f.getPage3LineHeight()) + ";}"
                + ".page-1 section{margin:0 0 " + px(f.getPage1SectionGapPx()) + " 0;}"
                + ".page-2 section{margin:0 0 " + px(f.getPage2SectionGapPx()) + " 0;}"
                + ".page-3 section{margin:0 0 " + px(f.getPage3SectionGapPx()) + " 0;}"
                + ".candidate-name{font-family:'Manrope','Inter',Arial,sans-serif;font-size:" + px(f.getBodyFontPx() + 8) + ";font-weight:700;text-transform:uppercase;line-height:1;letter-spacing:.4px;margin:0 0 2px 0;}"
                + ".candidate-title{font-family:'Manrope','Inter',Arial,sans-serif;font-size:" + px(f.getBodyFontPx() + 4) + ";font-weight:700;color:#1f2937;margin:0 0 5px 0;}"
                + ".contact-line{font-size:" + px(Math.max(9.0, f.getBodyFontPx() - 1.5)) + ";color:#2b2b2b;margin:0 0 2px 0;}"
                + ".value-line{margin:6px 0 0 0;padding:5px 0;border-top:1px solid #1f2937;border-bottom:1px solid #1f2937;font-size:" + px(Math.max(10.0, f.getBodyFontPx() + 1.0)) + ";font-weight:700;text-transform:uppercase;letter-spacing:.2px;}"
                + ".section-title{font-family:'Manrope','Inter',Arial,sans-serif;font-size:" + px(f.getBodyFontPx() + 1.5) + ";font-weight:700;text-transform:uppercase;letter-spacing:.6px;margin:0 0 5px 0;padding-bottom:3px;border-bottom:1px solid #444;}"
                + "p{margin:0 0 " + px(f.getParagraphGapPx()) + " 0;}"
                + ".item-block{margin:0 0 " + px(f.getItemGapPx()) + " 0;page-break-inside:avoid;}"
                + ".item-title{font-size:" + px(f.getBodyFontPx() + 0.8) + ";font-weight:700;margin:0 0 1px 0;}"
                + ".item-subtitle{font-size:" + px(Math.max(9.0, f.getBodyFontPx() - 1.2)) + ";font-weight:600;color:#1f2937;margin:0 0 2px 0;}"
                + "ul{margin:2px 0 0 0;padding-left:17px;}li{margin:0 0 " + px(f.getBulletGapPx()) + " 0;padding-left:1px;}"
                + ".skill-group,.education-line,.course-line,.info-line{margin:0 0 " + px(Math.max(2.0, f.getParagraphGapPx())) + " 0;}"
                + ".label{font-weight:700;}"
                + ".page-note-top,.page-note-bottom{position:absolute;left:10.5mm;right:10.5mm;min-height:7mm;box-sizing:border-box;text-align:center;color:#666;font-size:" + px(Math.max(8.8, f.getBodyFontPx() - 2)) + ";font-weight:700;text-transform:uppercase;letter-spacing:.3px;padding:4px 0;border-color:#ccc;background:#fff;}"
                + ".page-note-top{top:4mm;border-bottom:1px solid #ccc;}"
                + ".page-note-bottom{bottom:4mm;border-top:1px solid #ccc;}";
    }

    // ── Page rendering ──────────────────────────────────────────────

    private void renderPage1(StringBuilder html, ResumeRenderData data, PagePlan plan, FitState fit) {
        boolean multiPage = plan.getTargetPageCount() > 1;
        html.append("<main class=\"resume-page page-1").append(multiPage ? " has-next" : "").append("\"><div class=\"page-content\">");
        renderHeader(html, data);
        section(html, label(data, "Professional Summary", "О себе"), "<p>" + esc(data.getProfessionalSummary()) + "</p>");
        renderWork(html, data, label(data, "Work Experience", "Опыт работы"), page1Work(data, plan));
        renderSkills(html, data);
        renderEducation(html, data);
        renderCourses(html, data);
        if (!multiPage) {
            renderAspirations(html, data);
            renderPersonal(html, data);
        }
        html.append("</div>");
        if (multiPage) html.append("<div class=\"page-note-bottom\">").append(esc(label(data, "See the next page", "См. следующую страницу"))).append("</div>");
        html.append("</main>");
    }

    private void renderPage2(StringBuilder html, ResumeRenderData data, PagePlan plan, FitState fit) {
        boolean hasNext = plan.getTargetPageCount() > 2;
        html.append("<main class=\"resume-page page-2 has-prev").append(hasNext ? " has-next" : "").append("\">");
        html.append("<div class=\"page-note-top\">").append(esc(label(data, "See the previous page", "См. предыдущую страницу"))).append("</div>");
        html.append("<div class=\"page-content\">");
        List<ResumeRenderData.RenderProjectItem> projects = page2Projects(data, plan);
        if (!projects.isEmpty()) renderProjects(html, data, projects);
        List<ResumeRenderData.RenderWorkItem> p2Work = page2Work(data, plan);
        if (!p2Work.isEmpty()) renderWork(html, data, label(data, "Additional Work Experience", "Дополнительный опыт работы"), p2Work);
        if (!hasNext) {
            renderAspirations(html, data);
            renderPersonal(html, data);
        }
        html.append("</div>");
        if (hasNext) html.append("<div class=\"page-note-bottom\">").append(esc(label(data, "See the next page", "См. следующую страницу"))).append("</div>");
        html.append("</main>");
    }

    private void renderPage3(StringBuilder html, ResumeRenderData data, PagePlan plan, FitState fit) {
        html.append("<main class=\"resume-page page-3 has-prev\"><div class=\"page-note-top\">")
                .append(esc(label(data, "See the previous page", "См. предыдущую страницу"))).append("</div><div class=\"page-content\">");
        renderAspirations(html, data);
        renderPersonal(html, data);
        html.append("</div></main>");
    }

    // ── Section renderers ───────────────────────────────────────────

    private void renderHeader(StringBuilder html, ResumeRenderData data) {
        html.append("<section><div class=\"candidate-name\">").append(esc(data.getFullName())).append("</div>");
        html.append("<div class=\"candidate-title\">").append(esc(data.getProfessionalTitle())).append("</div>");
        html.append("<div class=\"contact-line\">").append(join(data.getPhone(), data.getEmail(), data.getLocation(), data.getWhatsapp())).append("</div>");
        html.append("<div class=\"contact-line\">").append(join(data.getLinkedin(), data.getPortfolio(), data.getTelegram())).append("</div>");
        html.append("<div class=\"value-line\">").append(esc(data.getValueLine())).append("</div></section>");
    }

    private void renderWork(StringBuilder html, ResumeRenderData data, String title, List<ResumeRenderData.RenderWorkItem> items) {
        if (items == null || items.isEmpty()) return;
        StringBuilder body = new StringBuilder();
        for (ResumeRenderData.RenderWorkItem w : items) {
            body.append("<div class=\"item-block\"><div class=\"item-title\">").append(esc(w.getJobTitle())).append(" | ").append(esc(w.getCompanyName())).append("</div>");
            body.append("<div class=\"item-subtitle\">").append(join(w.getLocation(), w.getDateRange())).append("</div>");
            if (w.getDescription() != null && !w.getDescription().isBlank()) body.append("<p>").append(esc(w.getDescription())).append("</p>");
            if (w.getBulletPoints() != null && !w.getBulletPoints().isEmpty()) {
                body.append("<ul>");
                for (String b : w.getBulletPoints()) body.append("<li>").append(esc(b)).append("</li>");
                body.append("</ul>");
            }
            body.append("</div>");
        }
        section(html, title, body.toString());
    }

    private void renderSkills(StringBuilder html, ResumeRenderData data) {
        List<ResumeRenderData.RenderSkillGroup> skills = data.getSkills();
        if (skills == null || skills.isEmpty()) return;
        StringBuilder b = new StringBuilder();
        for (ResumeRenderData.RenderSkillGroup g : skills) {
            b.append("<div class=\"skill-group\"><span class=\"label\">").append(esc(g.getGroupName())).append(":</span> ")
                    .append(esc(String.join(", ", g.getSkills()))).append("</div>");
        }
        section(html, label(data, "Skills", "Навыки"), b.toString());
    }

    private void renderEducation(StringBuilder html, ResumeRenderData data) {
        List<String> edu = data.getEducation();
        if (edu == null || edu.isEmpty()) return;
        StringBuilder b = new StringBuilder();
        for (String e : edu) b.append("<div class=\"education-line\">").append(esc(e)).append("</div>");
        section(html, label(data, "Education", "Образование"), b.toString());
    }

    private void renderCourses(StringBuilder html, ResumeRenderData data) {
        List<ResumeRenderData.RenderCourseItem> courses = data.getCourses();
        if (courses == null || courses.isEmpty()) return;
        StringBuilder b = new StringBuilder();
        for (ResumeRenderData.RenderCourseItem c : courses) {
            String line = c.getName() + " | " + c.getProvider()
                    + (c.getCourseFocus() == null || c.getCourseFocus().isBlank() ? "" : " | " + c.getCourseFocus());
            b.append("<div class=\"course-line\">").append(esc(line)).append("</div>");
        }
        section(html, label(data, "Courses and Certifications", "Курсы и сертификаты"), b.toString());
    }

    private void renderProjects(StringBuilder html, ResumeRenderData data, List<ResumeRenderData.RenderProjectItem> projects) {
        if (projects == null || projects.isEmpty()) return;

        StringBuilder b = new StringBuilder();
        for (ResumeRenderData.RenderProjectItem p : projects) {
            b.append("<div class=\"item-block\"><div class=\"item-title\">")
                    .append(esc(p.getProjectName()))
                    .append(" | ")
                    .append(esc(p.getRole()))
                    .append("</div>");

            String subtitle = join(p.getDateRange());
            if (subtitle != null && !subtitle.isBlank()) {
                b.append("<div class=\"item-subtitle\">").append(subtitle).append("</div>");
            }

            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                b.append("<p>").append(esc(p.getDescription())).append("</p>");
            }

            if (p.getBulletPoints() != null && !p.getBulletPoints().isEmpty()) {
                b.append("<ul>");
                for (String item : p.getBulletPoints()) {
                    if (item != null && !item.isBlank()) {
                        b.append("<li>").append(esc(item)).append("</li>");
                    }
                }
                b.append("</ul>");
            }

            b.append("</div>");
        }
        section(html, label(data, "Projects and Volunteering", "Проекты и волонтёрство"), b.toString());
    }

    private void renderAspirations(StringBuilder html, ResumeRenderData data) {
        String aspirations = data.getProfessionalAspirations();
        if (aspirations == null || aspirations.isBlank()) return;
        section(html, label(data, "Professional Aspirations", "Профессиональные цели"), "<p>" + esc(aspirations) + "</p>");
    }

    private void renderPersonal(StringBuilder html, ResumeRenderData data) {
        StringBuilder body = new StringBuilder();
        appendIfPresent(body, data.getPersonalLine1());
        appendIfPresent(body, data.getPersonalLine2());
        appendIfPresent(body, data.getPersonalLine3());
        if (body.length() > 0) section(html, label(data, "Personal Information", "Персональная информация"), body.toString());
    }

    // ── Page splitting helpers (using PagePlan counts) ──────────────

    private List<ResumeRenderData.RenderWorkItem> page1Work(ResumeRenderData data, PagePlan plan) {
        List<ResumeRenderData.RenderWorkItem> all = data.getWorkExperience();
        if (all == null || all.isEmpty()) return List.of();
        int count = Math.min(plan.getPage1WorkCount(), all.size());
        return all.subList(0, count);
    }

    private List<ResumeRenderData.RenderWorkItem> page2Work(ResumeRenderData data, PagePlan plan) {
        List<ResumeRenderData.RenderWorkItem> all = data.getWorkExperience();
        if (all == null || all.isEmpty()) return List.of();
        int p1Count = Math.min(plan.getPage1WorkCount(), all.size());
        int p2Count = Math.min(plan.getPage2AdditionalWorkCount(), all.size() - p1Count);
        if (p2Count <= 0) return List.of();
        return all.subList(p1Count, p1Count + p2Count);
    }

    private List<ResumeRenderData.RenderProjectItem> page2Projects(ResumeRenderData data, PagePlan plan) {
        List<ResumeRenderData.RenderProjectItem> all = data.getProjects();
        if (all == null || all.isEmpty()) return List.of();
        int count = Math.min(plan.getPage2ProjectCount(), all.size());
        if (count <= 0) return List.of();
        return all.subList(0, count);
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private void section(StringBuilder html, String title, String body) {
        html.append("<section><div class=\"section-title\">").append(esc(title)).append("</div>").append(body).append("</section>");
    }

    private void appendIfPresent(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) sb.append("<div class=\"info-line\">").append(esc(value)).append("</div>");
    }

    private String label(ResumeRenderData data, String en, String ru) {
        return "RU".equalsIgnoreCase(data.getLanguageCode()) ? ru : en;
    }

    private String join(String... values) {
        StringJoiner j = new StringJoiner(" | ");
        for (String v : values) if (v != null && !v.isBlank()) j.add(esc(v));
        return j.toString();
    }

    private String px(double v) { return n(v) + "px"; }
    private String n(double v) { return String.format(Locale.ROOT, "%.2f", v); }

    /** HTML-escape user text before template insertion (FR-008-023-1).
     *  See also: {@link com.resumainer.util.HtmlEscapeUtil#escape(String)} */
    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
