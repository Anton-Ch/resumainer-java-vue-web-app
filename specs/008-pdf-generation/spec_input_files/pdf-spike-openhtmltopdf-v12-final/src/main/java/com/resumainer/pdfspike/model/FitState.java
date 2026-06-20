package com.resumainer.pdfspike.model;

import java.util.Locale;

public record FitState(
        double bodyFontPx,
        double page1LineHeight,
        double page2LineHeight,
        double page3LineHeight,
        double page1SectionGapPx,
        double page2SectionGapPx,
        double page3SectionGapPx,
        double itemGapPx,
        double paragraphGapPx,
        double bulletGapPx
) {
    public static FitState defaults(FitLimits l) {
        return new FitState(
                l.bodyFontDefaultPx(),
                l.lineHeightDefault(), l.lineHeightDefault(), l.lineHeightDefault(),
                l.sectionGapDefaultPx(), l.sectionGapDefaultPx(), l.sectionGapDefaultPx(),
                l.itemGapDefaultPx(), l.paragraphGapDefaultPx(), l.bulletGapDefaultPx());
    }

    public String label() {
        return String.format(Locale.ROOT, "font%.2f_lh%.2f_%.2f_gap%.1f_%.1f", bodyFontPx, page1LineHeight, page2LineHeight, page1SectionGapPx, page2SectionGapPx);
    }
}
