package com.resumainer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PDF render fitting limits — bounds for the feedback fitting engine.
 * Maps to 'resume_pdf_fit_limits' table.
 */
public class PdfFitLimits {

    private long id;
    private String configKey;
    private boolean active;
    private BigDecimal bodyFontMinPx;
    private BigDecimal bodyFontDefaultPx;
    private BigDecimal bodyFontMaxPx;
    private BigDecimal lineHeightMin;
    private BigDecimal lineHeightDefault;
    private BigDecimal lineHeightMax;
    private BigDecimal sectionGapMinPx;
    private BigDecimal sectionGapDefaultPx;
    private BigDecimal sectionGapMaxPx;
    private BigDecimal itemGapMinPx;
    private BigDecimal itemGapDefaultPx;
    private BigDecimal itemGapMaxPx;
    private BigDecimal paragraphGapMinPx;
    private BigDecimal paragraphGapDefaultPx;
    private BigDecimal paragraphGapMaxPx;
    private BigDecimal bulletGapMinPx;
    private BigDecimal bulletGapDefaultPx;
    private BigDecimal bulletGapMaxPx;
    private int maxAttempts;
    private BigDecimal stepPercent;
    private BigDecimal page2DeltaLimitPercent;
    private LocalDateTime createdAt;

    public PdfFitLimits() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public BigDecimal getBodyFontMinPx() { return bodyFontMinPx; }
    public void setBodyFontMinPx(BigDecimal bodyFontMinPx) { this.bodyFontMinPx = bodyFontMinPx; }
    public BigDecimal getBodyFontDefaultPx() { return bodyFontDefaultPx; }
    public void setBodyFontDefaultPx(BigDecimal v) { this.bodyFontDefaultPx = v; }
    public BigDecimal getBodyFontMaxPx() { return bodyFontMaxPx; }
    public void setBodyFontMaxPx(BigDecimal bodyFontMaxPx) { this.bodyFontMaxPx = bodyFontMaxPx; }
    public BigDecimal getLineHeightMin() { return lineHeightMin; }
    public void setLineHeightMin(BigDecimal lineHeightMin) { this.lineHeightMin = lineHeightMin; }
    public BigDecimal getLineHeightDefault() { return lineHeightDefault; }
    public void setLineHeightDefault(BigDecimal v) { this.lineHeightDefault = v; }
    public BigDecimal getLineHeightMax() { return lineHeightMax; }
    public void setLineHeightMax(BigDecimal lineHeightMax) { this.lineHeightMax = lineHeightMax; }
    public BigDecimal getSectionGapMinPx() { return sectionGapMinPx; }
    public void setSectionGapMinPx(BigDecimal sectionGapMinPx) { this.sectionGapMinPx = sectionGapMinPx; }
    public BigDecimal getSectionGapDefaultPx() { return sectionGapDefaultPx; }
    public void setSectionGapDefaultPx(BigDecimal v) { this.sectionGapDefaultPx = v; }
    public BigDecimal getSectionGapMaxPx() { return sectionGapMaxPx; }
    public void setSectionGapMaxPx(BigDecimal sectionGapMaxPx) { this.sectionGapMaxPx = sectionGapMaxPx; }
    public BigDecimal getItemGapMinPx() { return itemGapMinPx; }
    public void setItemGapMinPx(BigDecimal itemGapMinPx) { this.itemGapMinPx = itemGapMinPx; }
    public BigDecimal getItemGapDefaultPx() { return itemGapDefaultPx; }
    public void setItemGapDefaultPx(BigDecimal v) { this.itemGapDefaultPx = v; }
    public BigDecimal getItemGapMaxPx() { return itemGapMaxPx; }
    public void setItemGapMaxPx(BigDecimal itemGapMaxPx) { this.itemGapMaxPx = itemGapMaxPx; }
    public BigDecimal getParagraphGapMinPx() { return paragraphGapMinPx; }
    public void setParagraphGapMinPx(BigDecimal paragraphGapMinPx) { this.paragraphGapMinPx = paragraphGapMinPx; }
    public BigDecimal getParagraphGapDefaultPx() { return paragraphGapDefaultPx; }
    public void setParagraphGapDefaultPx(BigDecimal v) { this.paragraphGapDefaultPx = v; }
    public BigDecimal getParagraphGapMaxPx() { return paragraphGapMaxPx; }
    public void setParagraphGapMaxPx(BigDecimal paragraphGapMaxPx) { this.paragraphGapMaxPx = paragraphGapMaxPx; }
    public BigDecimal getBulletGapMinPx() { return bulletGapMinPx; }
    public void setBulletGapMinPx(BigDecimal bulletGapMinPx) { this.bulletGapMinPx = bulletGapMinPx; }
    public BigDecimal getBulletGapDefaultPx() { return bulletGapDefaultPx; }
    public void setBulletGapDefaultPx(BigDecimal v) { this.bulletGapDefaultPx = v; }
    public BigDecimal getBulletGapMaxPx() { return bulletGapMaxPx; }
    public void setBulletGapMaxPx(BigDecimal bulletGapMaxPx) { this.bulletGapMaxPx = bulletGapMaxPx; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public BigDecimal getStepPercent() { return stepPercent; }
    public void setStepPercent(BigDecimal v) { this.stepPercent = v; }
    public BigDecimal getPage2DeltaLimitPercent() { return page2DeltaLimitPercent; }
    public void setPage2DeltaLimitPercent(BigDecimal page2DeltaLimitPercent) { this.page2DeltaLimitPercent = page2DeltaLimitPercent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
