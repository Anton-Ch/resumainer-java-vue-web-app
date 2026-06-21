package com.resumainer.service.pdf;

import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.PdfFitLimits;
import com.resumainer.model.pdf.FitState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for FeedbackFitEngine Phase 20 parity with spike V12.1.
 * All tests call actual production methods (package-private), not copied helpers.
 */
class FeedbackFitEngineTest {

    private PdfFitLimits limits;
    private FeedbackFitEngine engine;

    @BeforeEach
    void setUp() {
        limits = createSpikeLimits();
        // Null collaborators are safe — helper methods don't use renderer/pdf/analyzer/validator
        engine = new FeedbackFitEngine(null, null, null, null, limits);
    }

    @Test
    void effectiveTargets_filtersByTargetPageCount() {
        List<PdfFillTarget> mixed = new ArrayList<>();
        mixed.add(makeTarget(1, 1, "0.80"));
        mixed.add(makeTarget(2, 1, "0.85"));
        mixed.add(makeTarget(2, 2, "0.50"));
        mixed.add(makeTarget(3, 1, "0.85"));

        List<PdfFillTarget> result = engine.effectiveTargets(2, mixed);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTargetPageCount() == 2));
    }

    @Test
    void targetForIsolatedPage_selectsCorrectPageNumber() {
        List<PdfFillTarget> page2Targets = List.of(
                makeTarget(2, 1, "0.85"),
                makeTarget(2, 2, "0.50"));

        PdfFillTarget p1 = engine.targetForIsolatedPage(page2Targets, 1);
        assertNotNull(p1);
        assertEquals(1, p1.getPageNumber());

        PdfFillTarget p2 = engine.targetForIsolatedPage(page2Targets, 2);
        assertNotNull(p2);
        assertEquals(2, p2.getPageNumber());
    }

    @Test
    void targetForIsolatedPage_returnsNullForMissingPage() {
        List<PdfFillTarget> targets = List.of(makeTarget(2, 1, "0.85"));
        assertNull(engine.targetForIsolatedPage(targets, 3));
    }

    @Test
    void clampDeltaFromPage1_clampsUpward() {
        double result = engine.clampDeltaFromPage1(30.0, 10.0, 1.0, 50.0);
        assertEquals(16.5, result, 0.01);
    }

    @Test
    void clampDeltaFromPage1_clampsDownward() {
        double result = engine.clampDeltaFromPage1(2.0, 10.0, 1.0, 50.0);
        assertEquals(3.5, result, 0.01);
    }

    @Test
    void clampDeltaFromPage1_respectsGlobalMin() {
        double result = engine.clampDeltaFromPage1(0.5, 10.0, 5.0, 50.0);
        assertEquals(5.0, result, 0.01);
    }

    @Test
    void clampDeltaFromPage1_respectsGlobalMax() {
        double result = engine.clampDeltaFromPage1(30.0, 10.0, 1.0, 15.0);
        assertEquals(15.0, result, 0.01);
    }

    @Test
    void stepPercent_readsFromLimits() {
        assertEquals(0.10, engine.stepPercent(), 0.001);
    }

    @Test
    void page2DeltaLimitPercent_isFraction() {
        assertEquals(0.65, limits.getPage2DeltaLimitPercent().doubleValue(), 0.001);
    }

    @Test
    void maxAttempts_isSpikeValue() {
        assertEquals(20, limits.getMaxAttempts());
    }

    @Test
    void fitStateDefaults_matchesSpikeValues() {
        FitState state = FitState.defaults(limits);
        assertEquals(12.5, state.getBodyFontPx(), 0.01);
        assertEquals(1.35, state.getPage1LineHeight(), 0.01);
        assertEquals(15.0, state.getPage1SectionGapPx(), 0.01);
        assertEquals(9.0, state.getItemGapPx(), 0.01);
        assertEquals(5.0, state.getParagraphGapPx(), 0.01);
        assertEquals(3.0, state.getBulletGapPx(), 0.01);
    }

    // ─── helpers ────────────────────────────────────────────────────

    private PdfFitLimits createSpikeLimits() {
        PdfFitLimits l = new PdfFitLimits();
        l.setBodyFontDefaultPx(bd("12.5")); l.setBodyFontMinPx(bd("9.0")); l.setBodyFontMaxPx(bd("16.0"));
        l.setLineHeightDefault(bd("1.35")); l.setLineHeightMin(bd("1.05")); l.setLineHeightMax(bd("1.75"));
        l.setSectionGapDefaultPx(bd("15.0")); l.setSectionGapMinPx(bd("2.4")); l.setSectionGapMaxPx(bd("50.0"));
        l.setItemGapDefaultPx(bd("9.0")); l.setItemGapMinPx(bd("2.4")); l.setItemGapMaxPx(bd("30.0"));
        l.setParagraphGapDefaultPx(bd("5.0")); l.setParagraphGapMinPx(bd("1.6")); l.setParagraphGapMaxPx(bd("24.0"));
        l.setBulletGapDefaultPx(bd("3.0")); l.setBulletGapMinPx(bd("0.8")); l.setBulletGapMaxPx(bd("18.0"));
        l.setMaxAttempts(20);
        l.setStepPercent(bd("0.10"));
        l.setPage2DeltaLimitPercent(bd("0.65"));
        return l;
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    private PdfFillTarget makeTarget(int targetPages, int pageNum, String minFill) {
        PdfFillTarget t = new PdfFillTarget();
        t.setTargetPageCount(targetPages);
        t.setPageNumber(pageNum);
        t.setMinFill(new BigDecimal(minFill));
        t.setMaxFill(new BigDecimal("0.96"));
        return t;
    }
}
