package com.resumainer.dto.generate;

import java.util.List;

/**
 * Export data returned after finalization (Phase 23+).
 */
public class ExportResultDto {

    private List<SavedResumeExportDto> resumes;

    public ExportResultDto() {}

    public List<SavedResumeExportDto> getResumes() { return resumes; }
    public void setResumes(List<SavedResumeExportDto> resumes) { this.resumes = resumes; }
}
