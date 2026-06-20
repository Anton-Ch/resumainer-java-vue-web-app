package com.resumainer.pdfspike.model;

public record FitLimits(
        int maxAttempts,
        double stepPercent,
        double page2DeltaLimitPercent,
        double bodyFontMinPx,
        double bodyFontDefaultPx,
        double bodyFontMaxPx,
        double lineHeightMin,
        double lineHeightDefault,
        double lineHeightMax,
        double sectionGapMinPx,
        double sectionGapDefaultPx,
        double sectionGapMaxPx,
        double itemGapMinPx,
        double itemGapDefaultPx,
        double itemGapMaxPx,
        double paragraphGapMinPx,
        double paragraphGapDefaultPx,
        double paragraphGapMaxPx,
        double bulletGapMinPx,
        double bulletGapDefaultPx,
        double bulletGapMaxPx
) {}
