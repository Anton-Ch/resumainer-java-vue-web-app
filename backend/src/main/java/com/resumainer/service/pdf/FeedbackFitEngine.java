package com.resumainer.service.pdf;

import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.PdfFitLimits;
import com.resumainer.model.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Iterative feedback fitting engine for PDF page layout.
 * Uses round-robin shrink/grow on font, line-height, and gaps to fit content
 * within the target page count. Bounded by max_attempts from DB config.
 *
 * Ported from spike V12.1 FeedbackFitEngine. Adapted: spike FitLimits→PdfFitLimits,
 * FitState record→mutable class, FillTarget→PdfFillTarget, Path→File.
 */
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
    private final PdfFitLimits limits;

    public FeedbackFitEngine(XhtmlTemplateRenderer renderer, OpenHtmlPdfRenderer pdfRenderer,
                              PdfAnalyzer analyzer, PdfValidationService validator, PdfFitLimits limits) {
        this.renderer = renderer;
        this.pdfRenderer = pdfRenderer;
        this.analyzer = analyzer;
        this.validator = validator;
        this.blankPageCleaner = new PdfBlankPageCleaner(analyzer);
        this.limits = limits;
    }

    public FitResult fit(ResumeRenderData data, PagePlan plan, List<PdfFillTarget> targets,
                          File htmlFile, File pdfFile, File debugDir, boolean debugAttempts) {
        try {
            List<FitAttempt> allAttempts = new ArrayList<>();
            FitResult result = fitWithTargetPageCount(data, plan, targets, htmlFile, pdfFile, debugDir, debugAttempts, allAttempts);
            if (result.selectedAttempt() != null && result.selectedAttempt().valid()) return result;

            // 3-page fallback for 2-page plans that cannot fit
            if (plan.getTargetPageCount() == 2) {
                PagePlan fallback = new PagePlan();
                fallback.setTargetPageCount(3);
                fallback.setPage1WorkCount(plan.getPage1WorkCount());
                fallback.setPage2AdditionalWorkCount(plan.getPage2AdditionalWorkCount());
                fallback.setPage2ProjectCount(plan.getPage2ProjectCount());

                FitResult fbResult = fitWithTargetPageCount(data, fallback, targets, htmlFile, pdfFile,
                        new File(debugDir, "fallback-3-pages"), debugAttempts, allAttempts);
                if (fbResult.selectedAttempt() != null && better(fbResult.selectedAttempt(), result.selectedAttempt()))
                    return new FitResult(fbResult.selectedAttempt(), allAttempts);
            }
            return new FitResult(result.selectedAttempt(), allAttempts);
        } catch (Exception e) {
            log.error("PDF fitting failed completely", e);
            throw e;
        }
    }

    private FitResult fitWithTargetPageCount(ResumeRenderData data, PagePlan plan, List<PdfFillTarget> targets,
                                              File htmlFile, File pdfFile, File debugDir, boolean debugAttempts,
                                              List<FitAttempt> allAttempts) {
        File attemptsRoot = null;
        try {
            Files.createDirectories(htmlFile.getParentFile().toPath());
            Files.createDirectories(pdfFile.getParentFile().toPath());
            attemptsRoot = debugAttempts ? debugDir : Files.createTempDirectory("pdf-fit-attempts-").toFile();
            attemptsRoot.mkdirs();

            List<File> pagePdfFiles = new ArrayList<>();
            List<FitState> selectedPageStates = new ArrayList<>();
            FitState baseState = FitState.defaults(limits);

            // Select one effective target per planned page, with spike V12.1 adaptive rules.
            List<PdfFillTarget> pageTargets = effectiveTargets(data, plan, targets);

            for (int page = 1; page <= plan.getTargetPageCount(); page++) {
                FitState start = page == 1 ? baseState : copyFontFrom(baseState, FitState.defaults(limits));
                PdfFillTarget pageTarget = targetForIsolatedPage(pageTargets, page);
                FitAttempt pageAttempt = fitSinglePlannedPage(data, plan, page, pageTarget, start,
                        new File(attemptsRoot, "page-" + page), debugAttempts, allAttempts);
                selectedPageStates.add(pageAttempt.state());
                if (page == 1) baseState = pageAttempt.state();
                pagePdfFiles.add(new File(pageAttempt.pdfPath()));
                if (!pageAttempt.valid()) return new FitResult(pageAttempt, allAttempts);
            }

            FitState combinedState = mergeSelectedStates(selectedPageStates);
            String combinedHtml = renderer.render(data, plan, combinedState);
            String cssProblem = cssSafetyInspector.inspect(combinedHtml);
            if (!"OK".equals(cssProblem))
                throw new IllegalStateException("Unsafe CSS in combined HTML: " + cssProblem);
            Files.writeString(htmlFile.toPath(), combinedHtml, StandardCharsets.UTF_8);
            merger.merge(pagePdfFiles, pdfFile);

            PdfMetrics metrics = analyzer.analyze(pdfFile);
            String reason = validator.validate(metrics, plan.getTargetPageCount(), pageTargets,
                    contentExpectationBuilder.build(data, plan));
            FitAttempt finalAttempt = new FitAttempt(allAttempts.size() + 1, plan.getTargetPageCount(),
                    combinedState, metrics, "OK".equals(reason), reason,
                    htmlFile.getPath(), pdfFile.getPath());
            allAttempts.add(finalAttempt);
            return new FitResult(finalAttempt, allAttempts);
        } catch (Exception e) {
            log.error("Failed to fit and assemble PDF", e);
            throw new IllegalStateException("Failed to fit and assemble PDF", e);
        } finally {
            if (!debugAttempts && attemptsRoot != null) deleteRecursively(attemptsRoot);
        }
    }

    private FitAttempt fitSinglePlannedPage(ResumeRenderData data, PagePlan plan, int plannedPage,
                                             PdfFillTarget target, FitState initialState,
                                             File debugDir, boolean debugAttempts, List<FitAttempt> allAttempts) {
        FitState state = copyState(initialState);
        FitAttempt best = null;
        int maxAttempts = limits.getMaxAttempts();
        for (int i = 1; i <= maxAttempts; i++) {
            File htmlFile = new File(debugDir, String.format("attempt_%02d.html", i));
            File pdfFile = new File(debugDir, String.format("attempt_%02d.pdf", i));
            FitAttempt attempt = renderSingleAttempt(i, data, plan, plannedPage, target, state, htmlFile, pdfFile);
            allAttempts.add(attempt);
            best = chooseBetter(best, attempt, target);
            if (log.isDebugEnabled()) {
                log.debug("plannedPage={} attempt={} valid={} reason={} pages={}",
                        plannedPage, i, attempt.valid(), attempt.reason(),
                        attempt.metrics().actualPageCount());
            }
            if (attempt.valid()) return attempt;
            state = nextStateForPage(state, attempt, target, plannedPage, i);
        }
        return best;
    }

    private FitAttempt renderSingleAttempt(int attemptNumber, ResumeRenderData data, PagePlan plan,
                                            int plannedPage, PdfFillTarget target, FitState state,
                                            File htmlFile, File pdfFile) {
        try {
            Files.createDirectories(htmlFile.getParentFile().toPath());
            String html = renderer.renderSinglePage(data, plan, state, plannedPage);
            String cssProblem = cssSafetyInspector.inspect(html);
            if (!"OK".equals(cssProblem))
                throw new IllegalStateException("Unsafe CSS: " + cssProblem);
            Files.writeString(htmlFile.toPath(), html, StandardCharsets.UTF_8);
            pdfRenderer.render(html, pdfFile);
            int removed = blankPageCleaner.removeTrailingBlankPages(pdfFile);
            if (removed > 0) log.debug("removedTrailingBlankPages={} page={} attempt={}", removed, plannedPage, attemptNumber);
            PdfMetrics metrics = analyzer.analyze(pdfFile);
            String reason = validator.validate(metrics, 1,
                    target != null ? List.of(target) : List.of(),
                    contentExpectationBuilder.buildForPlannedPage(data, plan, plannedPage));
            return new FitAttempt(attemptNumber, 1, copyState(state), metrics,
                    "OK".equals(reason), "PAGE" + plannedPage + ":" + reason,
                    htmlFile.getPath(), pdfFile.getPath());
        } catch (Exception e) {
            throw new IllegalStateException("Fit attempt failed page=" + plannedPage + " attempt=" + attemptNumber, e);
        }
    }

    FitState nextStateForPage(FitState s, FitAttempt attempt, PdfFillTarget target, int page, int attemptIndex) {
        PdfMetrics metrics = attempt.metrics();
        String reason = attempt.reason() != null ? attempt.reason() : "";
        boolean missingOrClipped = reason.contains("MISSING_TEXTS") || reason.contains("TRAILING_BLANK_PAGE");
        boolean footerOverlap = reason.contains("FOOTER_OVERLAP");

        double maxFill = target != null && target.getMaxFill() != null ? target.getMaxFill().doubleValue() : 0.96;
        boolean overflow = missingOrClipped || footerOverlap || metrics.actualPageCount() > 1
                || metrics.fillRatios().getOrDefault(1, 0.0) > maxFill;

        double minFill = target != null ? target.getMinFill().doubleValue() : 0.80;
        boolean underfill = !missingOrClipped && !footerOverlap
                && metrics.fillRatios().getOrDefault(1, 0.0) < minFill;

        if (overflow) return shrinkRoundRobin(s, page, attemptIndex, page == 1);
        if (underfill) return growRoundRobin(s, page, attemptIndex, page == 1);
        return s;
    }

    double stepPercent() {
        return limits.getStepPercent() != null ? limits.getStepPercent().doubleValue() : 0.10;
    }

    /**
     * Select one effective fill target per planned page.
     * Applies spike V12.1 adaptive rules in code instead of depending on ambiguous DB duplicates.
     * Package-private for tests.
     */
    List<PdfFillTarget> effectiveTargets(ResumeRenderData data, PagePlan plan, List<PdfFillTarget> allTargets) {
        List<PdfFillTarget> selected = new ArrayList<>();
        int targetPageCount = plan.getTargetPageCount();
        for (int page = 1; page <= targetPageCount; page++) {
            PdfFillTarget target = selectConfiguredTarget(data, plan, allTargets, targetPageCount, page);
            if (target == null) {
                target = defaultTarget(targetPageCount, page);
            }
            selected.add(applySpikeAdaptiveTarget(data, plan, target));
        }
        return selected;
    }

    /**
     * Return the physical-page-1 target for isolated planned-page fitting.
     * A planned Page 2 rendered alone becomes a single-page PDF whose only physical page is 1.
     * Therefore the target page_number must be remapped to 1 during isolated fitting.
     * Package-private for tests.
     */
    PdfFillTarget targetForIsolatedPage(List<PdfFillTarget> targets, int plannedPageNumber) {
        PdfFillTarget original = null;
        if (targets != null) {
            for (PdfFillTarget candidate : targets) {
                if (candidate.getPageNumber() == plannedPageNumber) {
                    original = candidate;
                    break;
                }
            }
        }
        if (original == null) {
            original = defaultTarget(1, 1);
        }
        PdfFillTarget isolated = copyTarget(original);
        isolated.setTargetPageCount(1);
        isolated.setPageNumber(1);
        return isolated;
    }

    private PdfFillTarget selectConfiguredTarget(ResumeRenderData data, PagePlan plan, List<PdfFillTarget> allTargets,
                                                 int targetPageCount, int pageNumber) {
        if (allTargets == null || allTargets.isEmpty()) return null;
        String languageCode = data != null && data.getLanguageCode() != null
                ? data.getLanguageCode().trim().toUpperCase(Locale.ROOT)
                : null;
        int projectCount = pageNumber == 2 ? plan.getPage2ProjectCount() : 0;

        List<PdfFillTarget> candidates = new ArrayList<>();
        for (PdfFillTarget target : allTargets) {
            if (target.getTargetPageCount() != targetPageCount) continue;
            if (target.getPageNumber() != pageNumber) continue;
            if (!languageMatches(target, languageCode)) continue;
            if (!projectCountMatches(target, pageNumber, projectCount)) continue;
            candidates.add(target);
        }
        if (candidates.isEmpty()) return null;

        candidates.sort((a, b) -> {
            int lang = Boolean.compare(b.getLanguageCode() != null, a.getLanguageCode() != null);
            if (lang != 0) return lang;
            int projectSpecific = Boolean.compare(isProjectSpecific(b), isProjectSpecific(a));
            if (projectSpecific != 0) return projectSpecific;
            int priority = Integer.compare(b.getPriority(), a.getPriority());
            if (priority != 0) return priority;
            return Long.compare(a.getId(), b.getId());
        });
        return candidates.get(0);
    }

    private boolean languageMatches(PdfFillTarget target, String languageCode) {
        if (target.getLanguageCode() == null || target.getLanguageCode().isBlank()) return true;
        if (languageCode == null || languageCode.isBlank()) return false;
        return target.getLanguageCode().trim().equalsIgnoreCase(languageCode);
    }

    private boolean projectCountMatches(PdfFillTarget target, int pageNumber, int projectCount) {
        if (pageNumber != 2) return true;
        Integer min = target.getProjectCountMin();
        Integer max = target.getProjectCountMax();
        if (min != null && projectCount < min) return false;
        if (max != null && projectCount > max) return false;
        return true;
    }

    private boolean isProjectSpecific(PdfFillTarget target) {
        return target.getProjectCountMin() != null || target.getProjectCountMax() != null;
    }

    private PdfFillTarget applySpikeAdaptiveTarget(ResumeRenderData data, PagePlan plan, PdfFillTarget original) {
        PdfFillTarget target = copyTarget(original);
        BigDecimal min = target.getMinFill();
        BigDecimal max = target.getMaxFill();

        if (target.getPageNumber() == 1
                && plan.getTargetPageCount() == 1
                && data != null
                && "RU".equalsIgnoreCase(data.getLanguageCode())
                && max != null) {
            max = max.min(new BigDecimal("0.93"));
        }

        if (target.getPageNumber() == 2 && plan.getTargetPageCount() >= 2 && min != null) {
            int page2ProjectCount = plan.getPage2ProjectCount();
            if (page2ProjectCount == 0) {
                min = min.min(new BigDecimal("0.30"));
            } else if (page2ProjectCount == 1) {
                min = min.min(new BigDecimal("0.44"));
            }
        }

        target.setMinFill(min);
        target.setMaxFill(max);
        return target;
    }

    private PdfFillTarget defaultTarget(int targetPageCount, int pageNumber) {
        PdfFillTarget target = new PdfFillTarget();
        target.setTargetPageCount(targetPageCount);
        target.setPageNumber(pageNumber);
        target.setMinFill(pageNumber == 3 ? new BigDecimal("0.01") : new BigDecimal("0.80"));
        target.setMaxFill(new BigDecimal("0.96"));
        target.setPriority(0);
        return target;
    }

    private PdfFillTarget copyTarget(PdfFillTarget source) {
        PdfFillTarget copy = new PdfFillTarget();
        copy.setId(source.getId());
        copy.setFitLimitsId(source.getFitLimitsId());
        copy.setTargetPageCount(source.getTargetPageCount());
        copy.setPageNumber(source.getPageNumber());
        copy.setLanguageCode(source.getLanguageCode());
        copy.setProjectCountMin(source.getProjectCountMin());
        copy.setProjectCountMax(source.getProjectCountMax());
        copy.setMinFill(source.getMinFill());
        copy.setMaxFill(source.getMaxFill());
        copy.setPriority(source.getPriority());
        return copy;
    }

    /** Clamp page2/page3 line-height and section-gap delta from page1. Package-private for testing. */
    double clampDeltaFromPage1(double proposed, double page1Value, double globalMin, double globalMax) {
        double delta = limits.getPage2DeltaLimitPercent() != null
                ? limits.getPage2DeltaLimitPercent().doubleValue() : 0.65;
        double deltaMin = page1Value * (1.0 - delta);
        double deltaMax = page1Value * (1.0 + delta);
        return clamp(proposed, Math.max(globalMin, deltaMin), Math.min(globalMax, deltaMax));
    }

    private FitState shrinkRoundRobin(FitState s, int page, int attemptIndex, boolean allowFontChange) {
        int phase = (attemptIndex - 1) % (allowFontChange ? 4 : 3);
        double down = 1.0 - stepPercent();
        FitState result = copyState(s);
        if (phase == 0) {
            double v = pageSectionGap(s, page) * down;
            double clamped = page == 1
                    ? clamp(v, limits.getSectionGapMinPx().doubleValue(), limits.getSectionGapMaxPx().doubleValue())
                    : clampDeltaFromPage1(v, s.getPage1SectionGapPx(), limits.getSectionGapMinPx().doubleValue(), limits.getSectionGapMaxPx().doubleValue());
            setPageSectionGap(result, page, clamped);
        } else if (phase == 1) {
            applyContentGaps(result, down);
        } else if (phase == 2) {
            double v = pageLineHeight(s, page) * down;
            double clamped = page == 1
                    ? clamp(v, limits.getLineHeightMin().doubleValue(), limits.getLineHeightMax().doubleValue())
                    : clampDeltaFromPage1(v, s.getPage1LineHeight(), limits.getLineHeightMin().doubleValue(), limits.getLineHeightMax().doubleValue());
            setPageLineHeight(result, page, clamped);
        } else if (allowFontChange) {
            result.setBodyFontPx(clamp(s.getBodyFontPx() * down,
                    limits.getBodyFontMinPx().doubleValue(), limits.getBodyFontMaxPx().doubleValue()));
        }
        return result;
    }

    private FitState growRoundRobin(FitState s, int page, int attemptIndex, boolean allowFontChange) {
        int phase = (attemptIndex - 1) % (allowFontChange ? 4 : 3);
        double up = 1.0 + stepPercent();
        FitState result = copyState(s);
        if (allowFontChange && phase == 0) {
            result.setBodyFontPx(clamp(s.getBodyFontPx() * up,
                    limits.getBodyFontMinPx().doubleValue(), limits.getBodyFontMaxPx().doubleValue()));
        } else if ((!allowFontChange && phase == 0) || (allowFontChange && phase == 1)) {
            double v = pageLineHeight(s, page) * up;
            double clamped = page == 1
                    ? clamp(v, limits.getLineHeightMin().doubleValue(), limits.getLineHeightMax().doubleValue())
                    : clampDeltaFromPage1(v, s.getPage1LineHeight(), limits.getLineHeightMin().doubleValue(), limits.getLineHeightMax().doubleValue());
            setPageLineHeight(result, page, clamped);
        } else if ((!allowFontChange && phase == 1) || (allowFontChange && phase == 2)) {
            applyContentGaps(result, up);
        } else {
            double v = pageSectionGap(s, page) * up;
            double clamped = page == 1
                    ? clamp(v, limits.getSectionGapMinPx().doubleValue(), limits.getSectionGapMaxPx().doubleValue())
                    : clampDeltaFromPage1(v, s.getPage1SectionGapPx(), limits.getSectionGapMinPx().doubleValue(), limits.getSectionGapMaxPx().doubleValue());
            setPageSectionGap(result, page, clamped);
        }
        return result;
    }

    private FitAttempt chooseBetter(FitAttempt a, FitAttempt b, PdfFillTarget target) {
        if (a == null) return b;
        return score(b, target) < score(a, target) ? b : a;
    }

    private double score(FitAttempt a, PdfFillTarget target) {
        double sc = Math.abs(a.metrics().actualPageCount() - 1) * 100.0;
        for (var e : a.metrics().fillRatios().entrySet()) {
            if (e.getKey() > 1) sc += e.getValue() * 10.0;
        }
        double fill = a.metrics().fillRatios().getOrDefault(1, 0.0);
        double minFill = target != null ? target.getMinFill().doubleValue() : 0.80;
        double maxFill = target != null && target.getMaxFill() != null ? target.getMaxFill().doubleValue() : 0.96;
        if (fill < minFill) sc += minFill - fill;
        if (fill > maxFill) sc += fill - maxFill;
        String reason = a.reason() != null ? a.reason() : "";
        if (reason.contains("MISSING_TEXTS")) sc += 50.0;
        if (reason.contains("FOOTER_OVERLAP")) sc += 40.0;
        return sc;
    }

    private boolean better(FitAttempt a, FitAttempt b) {
        if (a == null) return false;
        if (b == null) return true;
        if (a.valid() != b.valid()) return a.valid();
        return a.metrics().actualPageCount() < b.metrics().actualPageCount();
    }

    private FitState mergeSelectedStates(List<FitState> states) {
        if (states.isEmpty()) return FitState.defaults(limits);
        FitState result = new FitState();
        result.setBodyFontPx(states.get(0).getBodyFontPx());
        result.setPage1LineHeight(states.get(0).getPage1LineHeight());
        result.setPage2LineHeight(states.size() > 1 ? states.get(1).getPage2LineHeight() : states.get(0).getPage2LineHeight());
        result.setPage3LineHeight(states.size() > 2 ? states.get(2).getPage3LineHeight() : (states.size() > 1 ? states.get(1).getPage3LineHeight() : states.get(0).getPage3LineHeight()));
        result.setPage1SectionGapPx(states.get(0).getPage1SectionGapPx());
        result.setPage2SectionGapPx(states.size() > 1 ? states.get(1).getPage2SectionGapPx() : states.get(0).getPage2SectionGapPx());
        result.setPage3SectionGapPx(states.size() > 2 ? states.get(2).getPage3SectionGapPx() : (states.size() > 1 ? states.get(1).getPage3SectionGapPx() : states.get(0).getPage3SectionGapPx()));
        result.setItemGapPx(states.stream().mapToDouble(FitState::getItemGapPx).min().orElse(0));
        result.setParagraphGapPx(states.stream().mapToDouble(FitState::getParagraphGapPx).min().orElse(0));
        result.setBulletGapPx(states.stream().mapToDouble(FitState::getBulletGapPx).min().orElse(0));
        return result;
    }

    private FitState copyFontFrom(FitState source, FitState target) {
        FitState result = copyState(target);
        result.setBodyFontPx(source.getBodyFontPx());
        return result;
    }

    private void applyContentGaps(FitState s, double multiplier) {
        s.setItemGapPx(clamp(s.getItemGapPx() * multiplier, limits.getItemGapMinPx().doubleValue(), limits.getItemGapMaxPx().doubleValue()));
        s.setParagraphGapPx(clamp(s.getParagraphGapPx() * multiplier, limits.getParagraphGapMinPx().doubleValue(), limits.getParagraphGapMaxPx().doubleValue()));
        s.setBulletGapPx(clamp(s.getBulletGapPx() * multiplier, limits.getBulletGapMinPx().doubleValue(), limits.getBulletGapMaxPx().doubleValue()));
    }

    private double pageLineHeight(FitState s, int page) {
        return page == 1 ? s.getPage1LineHeight() : page == 2 ? s.getPage2LineHeight() : s.getPage3LineHeight();
    }

    private double pageSectionGap(FitState s, int page) {
        return page == 1 ? s.getPage1SectionGapPx() : page == 2 ? s.getPage2SectionGapPx() : s.getPage3SectionGapPx();
    }

    private void setPageLineHeight(FitState s, int page, double v) {
        switch (page) {
            case 1: s.setPage1LineHeight(v); break;
            case 2: s.setPage2LineHeight(v); break;
            case 3: s.setPage3LineHeight(v); break;
        }
    }

    private void setPageSectionGap(FitState s, int page, double v) {
        switch (page) {
            case 1: s.setPage1SectionGapPx(v); break;
            case 2: s.setPage2SectionGapPx(v); break;
            case 3: s.setPage3SectionGapPx(v); break;
        }
    }

    private FitState copyState(FitState source) {
        FitState s = new FitState();
        s.setBodyFontPx(source.getBodyFontPx());
        s.setPage1LineHeight(source.getPage1LineHeight());
        s.setPage2LineHeight(source.getPage2LineHeight());
        s.setPage3LineHeight(source.getPage3LineHeight());
        s.setPage1SectionGapPx(source.getPage1SectionGapPx());
        s.setPage2SectionGapPx(source.getPage2SectionGapPx());
        s.setPage3SectionGapPx(source.getPage3SectionGapPx());
        s.setItemGapPx(source.getItemGapPx());
        s.setParagraphGapPx(source.getParagraphGapPx());
        s.setBulletGapPx(source.getBulletGapPx());
        return s;
    }

    private void deleteRecursively(File root) {
        try {
            if (root == null || !root.exists()) return;
            File[] files = root.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteRecursively(f);
                    f.delete();
                }
            }
            root.delete();
        } catch (Exception ignored) {}
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
