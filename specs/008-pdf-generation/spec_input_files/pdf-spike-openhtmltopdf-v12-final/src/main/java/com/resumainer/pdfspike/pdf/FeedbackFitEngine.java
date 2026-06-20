package com.resumainer.pdfspike.pdf;

import com.resumainer.pdfspike.model.*;
import com.resumainer.pdfspike.render.XhtmlTemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FeedbackFitEngine {
    private static final Logger log = LoggerFactory.getLogger(FeedbackFitEngine.class);
    private final XhtmlTemplateRenderer renderer;
    private final OpenHtmlPdfRenderer pdfRenderer;
    private final PdfAnalyzer analyzer;
    private final PdfValidationService validator;
    private final PdfPageMerger merger = new PdfPageMerger();
    private final ContentExpectationBuilder contentExpectationBuilder = new ContentExpectationBuilder();
    private final PdfBlankPageCleaner blankPageCleaner;
    private final CssSafetyInspector cssSafetyInspector = new CssSafetyInspector();
    private final FitLimits limits;

    public FeedbackFitEngine(XhtmlTemplateRenderer renderer, OpenHtmlPdfRenderer pdfRenderer, PdfAnalyzer analyzer, PdfValidationService validator, FitLimits limits) {
        this.renderer = renderer;
        this.pdfRenderer = pdfRenderer;
        this.analyzer = analyzer;
        this.validator = validator;
        this.blankPageCleaner = new PdfBlankPageCleaner(analyzer);
        this.limits = limits;
    }

    public FitResult fit(ResumeData data, PagePlan plan, Map<Integer, FillTarget> targets, Path htmlPath, Path pdfPath, Path debugDir, boolean debugAttempts) {
        List<FitAttempt> allAttempts = new ArrayList<>();
        FitResult result = fitWithTargetPageCount(data, plan, targets, htmlPath, pdfPath, debugDir, debugAttempts, allAttempts);
        if (result.selectedAttempt() != null && result.selectedAttempt().valid()) return result;

        if (plan.targetPageCount() == 2) {
            PagePlan fallback = new PagePlan(plan.rule(), 3, plan.page1Work(), plan.page2AdditionalWork(), plan.page2Projects(), true);
            Map<Integer, FillTarget> fallbackTargets = Map.of(
                    1, new FillTarget(3, 1, 0.85, 0.96, true),
                    2, new FillTarget(3, 2, 0.85, 0.96, true),
                    3, new FillTarget(3, 3, 0.01, 0.96, true)
            );
            FitResult fallbackResult = fitWithTargetPageCount(data, fallback, fallbackTargets, htmlPath, pdfPath, debugDir.resolve("fallback-3-pages"), debugAttempts, allAttempts);
            if (fallbackResult.selectedAttempt() != null && better(fallbackResult.selectedAttempt(), result.selectedAttempt())) return new FitResult(fallbackResult.selectedAttempt(), allAttempts);
        }
        return new FitResult(result.selectedAttempt(), allAttempts);
    }

    private FitResult fitWithTargetPageCount(ResumeData data, PagePlan plan, Map<Integer, FillTarget> targets, Path htmlPath, Path pdfPath, Path debugDir, boolean debugAttempts, List<FitAttempt> allAttempts) {
        Path attemptsRoot = null;
        try {
            Files.createDirectories(htmlPath.getParent());
            Files.createDirectories(pdfPath.getParent());
            attemptsRoot = debugAttempts ? debugDir : Files.createTempDirectory("pdf-spike-attempts-");
            Files.createDirectories(attemptsRoot);

            Map<Integer, FillTarget> effectiveTargets = effectiveTargets(data, plan, targets);
            List<Path> pagePdfPaths = new ArrayList<>();
            List<FitState> selectedPageStates = new ArrayList<>();
            FitState baseState = FitState.defaults(limits);

            for (int page = 1; page <= plan.targetPageCount(); page++) {
                FitState start = page == 1 ? baseState : copyFontFrom(baseState, FitState.defaults(limits));
                FitAttempt pageAttempt = fitSinglePlannedPage(data, plan, page, targetForIsolatedPage(effectiveTargets.get(page)), start, attemptsRoot.resolve("page-" + page), debugAttempts, allAttempts);
                selectedPageStates.add(pageAttempt.state());
                if (page == 1) baseState = pageAttempt.state();
                pagePdfPaths.add(pageAttempt.pdfPath());
                if (!pageAttempt.valid()) {
                    return new FitResult(pageAttempt, allAttempts);
                }
            }

            FitState combinedState = mergeSelectedStates(selectedPageStates);
            String combinedHtml = renderer.render(data, plan, combinedState);
            String cssProblem = cssSafetyInspector.inspect(combinedHtml);
            if (!"OK".equals(cssProblem)) throw new IllegalStateException("Unsafe/unsupported CSS detected in combined HTML: " + cssProblem);
            Files.writeString(htmlPath, combinedHtml, StandardCharsets.UTF_8);
            merger.merge(pagePdfPaths, pdfPath);

            PdfMetrics metrics = analyzer.analyze(pdfPath);
            String reason = validator.validate(metrics, plan.targetPageCount(), effectiveTargets, contentExpectationBuilder.build(data, plan));
            FitAttempt finalAttempt = new FitAttempt(allAttempts.size() + 1, plan.targetPageCount(), combinedState, metrics, "OK".equals(reason), reason, htmlPath, pdfPath);
            allAttempts.add(finalAttempt);
            return new FitResult(finalAttempt, allAttempts);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fit and assemble PDF", e);
        } finally {
            if (!debugAttempts && attemptsRoot != null) deleteRecursively(attemptsRoot);
        }
    }

    private FitAttempt fitSinglePlannedPage(ResumeData data, PagePlan plan, int plannedPage, FillTarget target, FitState initialState, Path debugDir, boolean debugAttempts, List<FitAttempt> allAttempts) {
        FitState state = initialState;
        FitAttempt best = null;
        for (int i = 1; i <= limits.maxAttempts(); i++) {
            Path html = debugDir.resolve("attempt_%02d.html".formatted(i));
            Path pdf = debugDir.resolve("attempt_%02d.pdf".formatted(i));
            FitAttempt attempt = renderSingleAttempt(i, data, plan, plannedPage, target, state, html, pdf);
            allAttempts.add(attempt);
            best = chooseBetter(best, attempt, target);
            log.debug("plannedPage={} attempt={} valid={} reason={} pages={} fill={} state={}", plannedPage, i, attempt.valid(), attempt.reason(), attempt.metrics().actualPageCount(), attempt.metrics().fillRatios(), state.label());
            if (attempt.valid()) return attempt;
            state = nextStateForPage(state, attempt, target, plannedPage, i);
        }
        return best;
    }

    private FitAttempt renderSingleAttempt(int attemptNumber, ResumeData data, PagePlan plan, int plannedPage, FillTarget target, FitState state, Path htmlPath, Path pdfPath) {
        try {
            Files.createDirectories(htmlPath.getParent());
            Files.createDirectories(pdfPath.getParent());
            String html = renderer.renderSinglePage(data, plan, state, plannedPage);
            String cssProblem = cssSafetyInspector.inspect(html);
            if (!"OK".equals(cssProblem)) throw new IllegalStateException("Unsafe/unsupported CSS detected: " + cssProblem);
            Files.writeString(htmlPath, html, StandardCharsets.UTF_8);
            pdfRenderer.render(html, pdfPath);
            int removedBlankPages = blankPageCleaner.removeTrailingBlankPages(pdfPath);
            if (removedBlankPages > 0) log.debug("removedTrailingBlankPages={} plannedPage={} attempt={} pdf={}", removedBlankPages, plannedPage, attemptNumber, pdfPath);
            PdfMetrics metrics = analyzer.analyze(pdfPath);
            String reason = validator.validate(metrics, 1, Map.of(1, target), contentExpectationBuilder.buildForPlannedPage(data, plan, plannedPage));
            return new FitAttempt(attemptNumber, 1, state, metrics, "OK".equals(reason), "PAGE" + plannedPage + ":" + reason, htmlPath, pdfPath);
        } catch (Exception e) {
            throw new IllegalStateException("Fit attempt failed plannedPage=" + plannedPage + " attempt=" + attemptNumber, e);
        }
    }


    private Map<Integer, FillTarget> effectiveTargets(ResumeData data, PagePlan plan, Map<Integer, FillTarget> targets) {
        Map<Integer, FillTarget> effective = new LinkedHashMap<>();
        for (Map.Entry<Integer, FillTarget> entry : targets.entrySet()) {
            int page = entry.getKey();
            FillTarget target = entry.getValue();
            double min = target.minFillRatio();
            Double max = target.maxFillRatio();

            if (page == 1 && plan.targetPageCount() == 1 && data.language() == Language.RU && max != null) {
                // Russian one-page resumes are more prone to bottom clipping because labels and values wrap more often.
                // Keep a slightly safer bottom margin; MISSING_TEXTS still remains the primary guard.
                max = Math.min(max, 0.93);
            }

            if (page == 2 && plan.targetPageCount() >= 2) {
                int page2ProjectCount = plan.page2Projects().size();
                if (page2ProjectCount == 0) {
                    min = Math.min(min, 0.30);
                } else if (page2ProjectCount == 1) {
                    min = Math.min(min, 0.44);
                }
            }

            effective.put(page, new FillTarget(target.pageCount(), target.pageNumber(), min, max, target.requiredNonEmpty()));
        }
        return effective;
    }

    private FillTarget targetForIsolatedPage(FillTarget original) {
        if (original == null) return new FillTarget(1, 1, 0.01, 0.96, true);
        return new FillTarget(1, 1, original.minFillRatio(), original.maxFillRatio(), original.requiredNonEmpty());
    }

    private FitState nextStateForPage(FitState s, FitAttempt attempt, FillTarget target, int page, int attemptIndex) {
        PdfMetrics metrics = attempt.metrics();
        boolean missingOrClippedText = attempt.reason().contains("MISSING_TEXTS") || attempt.reason().contains("TRAILING_BLANK_PAGE");
        boolean overflow = missingOrClippedText || metrics.actualPageCount() > 1 || metrics.fillRatios().getOrDefault(1, 0.0) > (target.maxFillRatio() == null ? 0.96 : target.maxFillRatio());
        boolean underfill = !missingOrClippedText && metrics.fillRatios().getOrDefault(1, 0.0) < target.minFillRatio();
        if (overflow) return shrinkRoundRobin(s, page, attemptIndex, page == 1);
        if (underfill) return growRoundRobin(s, page, attemptIndex, page == 1);
        return s;
    }

    private FitState shrinkRoundRobin(FitState s, int page, int attemptIndex, boolean allowFontChange) {
        int phase = (attemptIndex - 1) % (allowFontChange ? 4 : 3);
        double down = 1.0 - limits.stepPercent();
        if (phase == 0) return withPageSectionGap(s, page, clamp(pageSectionGap(s, page) * down, limits.sectionGapMinPx(), limits.sectionGapMaxPx()));
        if (phase == 1) return withContentGaps(s, down);
        if (phase == 2) return withPageLineHeight(s, page, clamp(pageLineHeight(s, page) * down, limits.lineHeightMin(), limits.lineHeightMax()));
        return withBody(s, clamp(s.bodyFontPx() * down, limits.bodyFontMinPx(), limits.bodyFontMaxPx()));
    }

    private FitState growRoundRobin(FitState s, int page, int attemptIndex, boolean allowFontChange) {
        int phase = (attemptIndex - 1) % (allowFontChange ? 4 : 3);
        double up = 1.0 + limits.stepPercent();
        if (allowFontChange && phase == 0) return withBody(s, clamp(s.bodyFontPx() * up, limits.bodyFontMinPx(), limits.bodyFontMaxPx()));
        if ((!allowFontChange && phase == 0) || (allowFontChange && phase == 1)) return withPageLineHeight(s, page, clamp(pageLineHeight(s, page) * up, limits.lineHeightMin(), limits.lineHeightMax()));
        if ((!allowFontChange && phase == 1) || (allowFontChange && phase == 2)) return withContentGaps(s, up);
        return withPageSectionGap(s, page, clamp(pageSectionGap(s, page) * up, limits.sectionGapMinPx(), limits.sectionGapMaxPx()));
    }

    private FitAttempt chooseBetter(FitAttempt a, FitAttempt b, FillTarget target) {
        if (a == null) return b;
        return score(b, target) < score(a, target) ? b : a;
    }

    private double score(FitAttempt a, FillTarget target) {
        double score = Math.abs(a.metrics().actualPageCount() - 1) * 100.0;
        for (var e : a.metrics().fillRatios().entrySet()) {
            if (e.getKey() > 1) score += e.getValue() * 10.0;
        }
        double fill = a.metrics().fillRatios().getOrDefault(1, 0.0);
        if (fill < target.minFillRatio()) score += target.minFillRatio() - fill;
        if (target.maxFillRatio() != null && fill > target.maxFillRatio()) score += fill - target.maxFillRatio();
        if (a.reason().contains("MISSING_TEXTS")) score += 50.0;
        return score;
    }

    private boolean better(FitAttempt a, FitAttempt b) {
        if (b == null) return true;
        if (a.valid() != b.valid()) return a.valid();
        return a.metrics().actualPageCount() < b.metrics().actualPageCount();
    }

    private FitState mergeSelectedStates(List<FitState> states) {
        FitState first = states.get(0);
        FitState second = states.size() > 1 ? states.get(1) : first;
        FitState third = states.size() > 2 ? states.get(2) : second;
        return new FitState(first.bodyFontPx(), first.page1LineHeight(), second.page2LineHeight(), third.page3LineHeight(),
                first.page1SectionGapPx(), second.page2SectionGapPx(), third.page3SectionGapPx(),
                min(states.stream().mapToDouble(FitState::itemGapPx).toArray()),
                min(states.stream().mapToDouble(FitState::paragraphGapPx).toArray()),
                min(states.stream().mapToDouble(FitState::bulletGapPx).toArray()));
    }

    private FitState copyFontFrom(FitState source, FitState target) {
        return new FitState(source.bodyFontPx(), target.page1LineHeight(), target.page2LineHeight(), target.page3LineHeight(), target.page1SectionGapPx(), target.page2SectionGapPx(), target.page3SectionGapPx(), target.itemGapPx(), target.paragraphGapPx(), target.bulletGapPx());
    }

    private FitState withBody(FitState s, double v) { return new FitState(v, s.page1LineHeight(), s.page2LineHeight(), s.page3LineHeight(), s.page1SectionGapPx(), s.page2SectionGapPx(), s.page3SectionGapPx(), s.itemGapPx(), s.paragraphGapPx(), s.bulletGapPx()); }
    private FitState withPageLineHeight(FitState s, int page, double v) {
        double adjusted = page > 1 ? clampDeltaFromPage1(v, s.page1LineHeight(), limits.lineHeightMin(), limits.lineHeightMax()) : v;
        return new FitState(s.bodyFontPx(), page == 1 ? adjusted : s.page1LineHeight(), page == 2 ? adjusted : s.page2LineHeight(), page == 3 ? adjusted : s.page3LineHeight(), s.page1SectionGapPx(), s.page2SectionGapPx(), s.page3SectionGapPx(), s.itemGapPx(), s.paragraphGapPx(), s.bulletGapPx());
    }
    private FitState withPageSectionGap(FitState s, int page, double v) {
        double adjusted = page > 1 ? clampDeltaFromPage1(v, s.page1SectionGapPx(), limits.sectionGapMinPx(), limits.sectionGapMaxPx()) : v;
        return new FitState(s.bodyFontPx(), s.page1LineHeight(), s.page2LineHeight(), s.page3LineHeight(), page == 1 ? adjusted : s.page1SectionGapPx(), page == 2 ? adjusted : s.page2SectionGapPx(), page == 3 ? adjusted : s.page3SectionGapPx(), s.itemGapPx(), s.paragraphGapPx(), s.bulletGapPx());
    }
    private FitState withContentGaps(FitState s, double multiplier) { return new FitState(s.bodyFontPx(), s.page1LineHeight(), s.page2LineHeight(), s.page3LineHeight(), s.page1SectionGapPx(), s.page2SectionGapPx(), s.page3SectionGapPx(), clamp(s.itemGapPx() * multiplier, limits.itemGapMinPx(), limits.itemGapMaxPx()), clamp(s.paragraphGapPx() * multiplier, limits.paragraphGapMinPx(), limits.paragraphGapMaxPx()), clamp(s.bulletGapPx() * multiplier, limits.bulletGapMinPx(), limits.bulletGapMaxPx())); }
    private double pageLineHeight(FitState s, int page) { return page == 1 ? s.page1LineHeight() : page == 2 ? s.page2LineHeight() : s.page3LineHeight(); }
    private double pageSectionGap(FitState s, int page) { return page == 1 ? s.page1SectionGapPx() : page == 2 ? s.page2SectionGapPx() : s.page3SectionGapPx(); }

    private double clampDeltaFromPage1(double proposed, double page1Value, double globalMin, double globalMax) {
        double delta = limits.page2DeltaLimitPercent();
        double deltaMin = page1Value * (1.0 - delta);
        double deltaMax = page1Value * (1.0 + delta);
        return clamp(proposed, Math.max(globalMin, deltaMin), Math.min(globalMax, deltaMax));
    }

    private void deleteRecursively(Path root) {
        try {
            if (root == null || !Files.exists(root)) return;
            try (var stream = Files.walk(root)) {
                stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
        } catch (Exception ignored) { }
    }

    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private double min(double[] values) { double m = Double.MAX_VALUE; for (double v : values) m = Math.min(m, v); return m; }
}
