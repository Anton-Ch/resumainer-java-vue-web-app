package com.resumainer.dto.generate;

/**
 * Export DTO for one saved resume.
 * All URLs are fully functional (Phase 23+).
 * pdfAvailable indicates whether real PDF is generated.
 */
public class SavedResumeExportDto {

    private long savedResumeId;
    private String languageCode;       // EN, RU
    private String adaptationLevel;    // MINIMAL, BALANCED, MAXIMUM

    private String htmlDownloadUrl;    // GET /api/generate/resumes/{id}/html
    private String pdfDownloadUrl;     // GET /api/generate/resumes/{id}/pdf
    private String pdfOpenUrl;         // GET /api/generate/resumes/{id}/pdf?disposition=inline
    private String publicUrlLink;      // /{username}/{publicCode}

    private boolean pdfAvailable;      // true when pdf_status = READY
    private String pdfMessage;         // null when PDF is ready

    private String coverLetter;        // null if not generated

    public SavedResumeExportDto() {}

    public long getSavedResumeId() { return savedResumeId; }
    public void setSavedResumeId(long savedResumeId) { this.savedResumeId = savedResumeId; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public String getAdaptationLevel() { return adaptationLevel; }
    public void setAdaptationLevel(String adaptationLevel) { this.adaptationLevel = adaptationLevel; }

    public String getHtmlDownloadUrl() { return htmlDownloadUrl; }
    public void setHtmlDownloadUrl(String htmlDownloadUrl) { this.htmlDownloadUrl = htmlDownloadUrl; }

    public String getPdfDownloadUrl() { return pdfDownloadUrl; }
    public void setPdfDownloadUrl(String pdfDownloadUrl) { this.pdfDownloadUrl = pdfDownloadUrl; }

    public String getPdfOpenUrl() { return pdfOpenUrl; }
    public void setPdfOpenUrl(String pdfOpenUrl) { this.pdfOpenUrl = pdfOpenUrl; }

    public String getPublicUrlLink() { return publicUrlLink; }
    public void setPublicUrlLink(String publicUrlLink) { this.publicUrlLink = publicUrlLink; }

    public boolean isPdfAvailable() { return pdfAvailable; }
    public void setPdfAvailable(boolean pdfAvailable) { this.pdfAvailable = pdfAvailable; }

    public String getPdfMessage() { return pdfMessage; }
    public void setPdfMessage(String pdfMessage) { this.pdfMessage = pdfMessage; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}
