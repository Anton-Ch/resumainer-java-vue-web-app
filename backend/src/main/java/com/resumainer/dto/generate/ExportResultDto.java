package com.resumainer.dto.generate;

import java.util.List;

/**
 * Export data returned after finalization.
 * htmlDownloadUrl is real in feat/007; pdfDownloadUrl, pdfOpenUrl, publicUrlLink
 * are placeholders until feat/008-pdf-conversion.
 */
public class ExportResultDto {

    private List<SavedResumeExportDto> resumes;

    public ExportResultDto() {}

    public List<SavedResumeExportDto> getResumes() { return resumes; }
    public void setResumes(List<SavedResumeExportDto> resumes) { this.resumes = resumes; }
}
