package com.resumainer.dto.home;

/**
 * Canonical DTO for saved resume data on the Home page.
 * Used by both the paginated list ({@code GET /api/resumes})
 * and the latest-resume summary ({@code GET /api/user/home} → summary.lastResume).
 * <p>
 * {@code publicUrlLink} is a full absolute URL built from APP_PUBLIC_BASE_URL
 * or request origin fallback. Authenticated export URLs (pdfOpenUrl, pdfDownloadUrl,
 * htmlDownloadUrl) are canonical relative API paths under /api/generate/resumes/.
 * No raw filesystem paths are exposed.
 */
public class HomeSavedResumeDto {

    private long id;
    private String resumeTitle;
    private String vacancyTitle;
    private String companyName;
    private String languageCode;
    private String languageName;
    private String adaptationLevel;
    private String createdAt;
    private String publicUrlLink;
    private String pdfOpenUrl;
    private String pdfDownloadUrl;
    private String htmlDownloadUrl;
    private boolean pdfAvailable;
    private String pdfStatus;
    private String pdfMessage;
    private String coverLetter;

    public HomeSavedResumeDto() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getResumeTitle() { return resumeTitle; }
    public void setResumeTitle(String resumeTitle) { this.resumeTitle = resumeTitle; }

    public String getVacancyTitle() { return vacancyTitle; }
    public void setVacancyTitle(String vacancyTitle) { this.vacancyTitle = vacancyTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }

    public String getAdaptationLevel() { return adaptationLevel; }
    public void setAdaptationLevel(String adaptationLevel) { this.adaptationLevel = adaptationLevel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPublicUrlLink() { return publicUrlLink; }
    public void setPublicUrlLink(String publicUrlLink) { this.publicUrlLink = publicUrlLink; }

    public String getPdfOpenUrl() { return pdfOpenUrl; }
    public void setPdfOpenUrl(String pdfOpenUrl) { this.pdfOpenUrl = pdfOpenUrl; }

    public String getPdfDownloadUrl() { return pdfDownloadUrl; }
    public void setPdfDownloadUrl(String pdfDownloadUrl) { this.pdfDownloadUrl = pdfDownloadUrl; }

    public String getHtmlDownloadUrl() { return htmlDownloadUrl; }
    public void setHtmlDownloadUrl(String htmlDownloadUrl) { this.htmlDownloadUrl = htmlDownloadUrl; }

    public boolean isPdfAvailable() { return pdfAvailable; }
    public void setPdfAvailable(boolean pdfAvailable) { this.pdfAvailable = pdfAvailable; }

    public String getPdfStatus() { return pdfStatus; }
    public void setPdfStatus(String pdfStatus) { this.pdfStatus = pdfStatus; }

    public String getPdfMessage() { return pdfMessage; }
    public void setPdfMessage(String pdfMessage) { this.pdfMessage = pdfMessage; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}
