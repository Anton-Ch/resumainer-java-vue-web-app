package com.resumainer.model;

/**
 * Response DTO for a saved resume — used in paginated list results
 * and the User Home last-resume preview.
 * <p>
 * Maps to the {@code saved_resumes} table. Fields match the API contract
 * defined in {@code contracts/api-contracts.md}.
 */
public class SavedResume {

    private long id;
    private String resumeTitle;
    private String vacancy;
    private String company;
    private String language;
    private String adaptationLevel;
    private String createdAt;
    private String publicUrl;
    private String pdfUrl;
    private String coverLetter;

    public SavedResume() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResumeTitle() {
        return resumeTitle;
    }

    public void setResumeTitle(String resumeTitle) {
        this.resumeTitle = resumeTitle;
    }

    public String getVacancy() {
        return vacancy;
    }

    public void setVacancy(String vacancy) {
        this.vacancy = vacancy;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAdaptationLevel() {
        return adaptationLevel;
    }

    public void setAdaptationLevel(String adaptationLevel) {
        this.adaptationLevel = adaptationLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}
