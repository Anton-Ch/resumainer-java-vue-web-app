package com.resumainer.model;

/**
 * Response DTO for GET /api/user/home endpoint.
 * Aggregates profile readiness status, checklist state,
 * summary stats, and last resume preview.
 */
public class UserHomeSummary {

    private boolean profileReady;
    private ProfileChecklist profileChecklist;
    private Summary summary;
    private SavedResume lastResume;

    public UserHomeSummary() {
    }

    public boolean isProfileReady() {
        return profileReady;
    }

    public void setProfileReady(boolean profileReady) {
        this.profileReady = profileReady;
    }

    public ProfileChecklist getProfileChecklist() {
        return profileChecklist;
    }

    public void setProfileChecklist(ProfileChecklist profileChecklist) {
        this.profileChecklist = profileChecklist;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public SavedResume getLastResume() {
        return lastResume;
    }

    public void setLastResume(SavedResume lastResume) {
        this.lastResume = lastResume;
    }

    /**
     * Breakdown of profile section completeness.
     */
    public static class ProfileChecklist {
        private boolean contactDetails;
        private boolean workExperience;
        private boolean education;
        private boolean additionalInfo;

        public ProfileChecklist() {
        }

        public ProfileChecklist(boolean contactDetails, boolean workExperience, boolean education, boolean additionalInfo) {
            this.contactDetails = contactDetails;
            this.workExperience = workExperience;
            this.education = education;
            this.additionalInfo = additionalInfo;
        }

        public boolean isContactDetails() { return contactDetails; }
        public void setContactDetails(boolean contactDetails) { this.contactDetails = contactDetails; }
        public boolean isWorkExperience() { return workExperience; }
        public void setWorkExperience(boolean workExperience) { this.workExperience = workExperience; }
        public boolean isEducation() { return education; }
        public void setEducation(boolean education) { this.education = education; }
        public boolean isAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(boolean additionalInfo) { this.additionalInfo = additionalInfo; }
    }

    /**
     * Aggregate summary stats.
     */
    public static class Summary {
        private long savedResumesCount;
        private String profileStatus;
        private Long lastResumeId;

        public Summary() {
        }

        public Summary(long savedResumesCount, String profileStatus, Long lastResumeId) {
            this.savedResumesCount = savedResumesCount;
            this.profileStatus = profileStatus;
            this.lastResumeId = lastResumeId;
        }

        public long getSavedResumesCount() { return savedResumesCount; }
        public void setSavedResumesCount(long savedResumesCount) { this.savedResumesCount = savedResumesCount; }
        public String getProfileStatus() { return profileStatus; }
        public void setProfileStatus(String profileStatus) { this.profileStatus = profileStatus; }
        public Long getLastResumeId() { return lastResumeId; }
        public void setLastResumeId(Long lastResumeId) { this.lastResumeId = lastResumeId; }
    }
}
