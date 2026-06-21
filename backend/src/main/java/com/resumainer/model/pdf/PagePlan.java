package com.resumainer.model.pdf;

import java.util.List;

/**
 * Page allocation plan: which content goes to which page.
 * Built from production budget resolver, not spike edge_case_rule.
 * Ported from spike V12.1 PagePlan record, adapted for production.
 */
public class PagePlan {

    private int targetPageCount;
    private int page1WorkCount;
    private int page2AdditionalWorkCount;
    private int page2ProjectCount;
    private boolean page2HasProjectsFirst;

    public PagePlan() {}

    public int getTargetPageCount() { return targetPageCount; }
    public void setTargetPageCount(int targetPageCount) { this.targetPageCount = targetPageCount; }
    public int getPage1WorkCount() { return page1WorkCount; }
    public void setPage1WorkCount(int page1WorkCount) { this.page1WorkCount = page1WorkCount; }
    public int getPage2AdditionalWorkCount() { return page2AdditionalWorkCount; }
    public void setPage2AdditionalWorkCount(int page2AdditionalWorkCount) { this.page2AdditionalWorkCount = page2AdditionalWorkCount; }
    public int getPage2ProjectCount() { return page2ProjectCount; }
    public void setPage2ProjectCount(int page2ProjectCount) { this.page2ProjectCount = page2ProjectCount; }
    public boolean isPage2HasProjectsFirst() { return page2HasProjectsFirst; }
    public void setPage2HasProjectsFirst(boolean page2HasProjectsFirst) { this.page2HasProjectsFirst = page2HasProjectsFirst; }
}
