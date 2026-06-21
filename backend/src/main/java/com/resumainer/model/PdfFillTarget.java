package com.resumainer.model;

import java.math.BigDecimal;

/**
 * PDF page fill target — per-page minimum/maximum fill ratio.
 * Maps to 'resume_pdf_fill_targets' table.
 */
public class PdfFillTarget {

    private long id;
    private long fitLimitsId;
    private int targetPageCount;
    private int pageNumber;
    private String languageCode;
    private Integer projectCountMin;
    private Integer projectCountMax;
    private BigDecimal minFill;
    private BigDecimal maxFill;
    private int priority;

    public PdfFillTarget() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getFitLimitsId() { return fitLimitsId; }
    public void setFitLimitsId(long fitLimitsId) { this.fitLimitsId = fitLimitsId; }
    public int getTargetPageCount() { return targetPageCount; }
    public void setTargetPageCount(int targetPageCount) { this.targetPageCount = targetPageCount; }
    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public Integer getProjectCountMin() { return projectCountMin; }
    public void setProjectCountMin(Integer projectCountMin) { this.projectCountMin = projectCountMin; }
    public Integer getProjectCountMax() { return projectCountMax; }
    public void setProjectCountMax(Integer projectCountMax) { this.projectCountMax = projectCountMax; }
    public BigDecimal getMinFill() { return minFill; }
    public void setMinFill(BigDecimal minFill) { this.minFill = minFill; }
    public BigDecimal getMaxFill() { return maxFill; }
    public void setMaxFill(BigDecimal maxFill) { this.maxFill = maxFill; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
