package com.resumainer.model.pdf;

import com.resumainer.model.PdfFitLimits;

/**
 * Mutable fit state used by the feedback fitting engine.
 * Starts from maximum values and shrinks/grows during fitting iterations.
 * Ported from spike V12.1 FitState record, converted to mutable class.
 */
public class FitState {

    private double bodyFontPx;
    private double page1LineHeight;
    private double page2LineHeight;
    private double page3LineHeight;
    private double page1SectionGapPx;
    private double page2SectionGapPx;
    private double page3SectionGapPx;
    private double itemGapPx;
    private double paragraphGapPx;
    private double bulletGapPx;

    public FitState() {}

    /** Start fitting from spike V12.1 default values (from config). */
    public static FitState defaults(PdfFitLimits limits) {
        FitState s = new FitState();
        s.bodyFontPx = d(limits.getBodyFontDefaultPx(), 12.5);
        s.page1LineHeight = d(limits.getLineHeightDefault(), 1.35);
        s.page2LineHeight = d(limits.getLineHeightDefault(), 1.35);
        s.page3LineHeight = d(limits.getLineHeightDefault(), 1.35);
        s.page1SectionGapPx = d(limits.getSectionGapDefaultPx(), 15.0);
        s.page2SectionGapPx = d(limits.getSectionGapDefaultPx(), 15.0);
        s.page3SectionGapPx = d(limits.getSectionGapDefaultPx(), 15.0);
        s.itemGapPx = d(limits.getItemGapDefaultPx(), 9.0);
        s.paragraphGapPx = d(limits.getParagraphGapDefaultPx(), 5.0);
        s.bulletGapPx = d(limits.getBulletGapDefaultPx(), 3.0);
        return s;
    }

    private static double d(java.math.BigDecimal v, double fallback) {
        return v != null ? v.doubleValue() : fallback;
    }

    public double getBodyFontPx() { return bodyFontPx; }
    public void setBodyFontPx(double bodyFontPx) { this.bodyFontPx = bodyFontPx; }
    public double getPage1LineHeight() { return page1LineHeight; }
    public void setPage1LineHeight(double page1LineHeight) { this.page1LineHeight = page1LineHeight; }
    public double getPage2LineHeight() { return page2LineHeight; }
    public void setPage2LineHeight(double page2LineHeight) { this.page2LineHeight = page2LineHeight; }
    public double getPage3LineHeight() { return page3LineHeight; }
    public void setPage3LineHeight(double page3LineHeight) { this.page3LineHeight = page3LineHeight; }
    public double getPage1SectionGapPx() { return page1SectionGapPx; }
    public void setPage1SectionGapPx(double page1SectionGapPx) { this.page1SectionGapPx = page1SectionGapPx; }
    public double getPage2SectionGapPx() { return page2SectionGapPx; }
    public void setPage2SectionGapPx(double page2SectionGapPx) { this.page2SectionGapPx = page2SectionGapPx; }
    public double getPage3SectionGapPx() { return page3SectionGapPx; }
    public void setPage3SectionGapPx(double page3SectionGapPx) { this.page3SectionGapPx = page3SectionGapPx; }
    public double getItemGapPx() { return itemGapPx; }
    public void setItemGapPx(double itemGapPx) { this.itemGapPx = itemGapPx; }
    public double getParagraphGapPx() { return paragraphGapPx; }
    public void setParagraphGapPx(double paragraphGapPx) { this.paragraphGapPx = paragraphGapPx; }
    public double getBulletGapPx() { return bulletGapPx; }
    public void setBulletGapPx(double bulletGapPx) { this.bulletGapPx = bulletGapPx; }
}
