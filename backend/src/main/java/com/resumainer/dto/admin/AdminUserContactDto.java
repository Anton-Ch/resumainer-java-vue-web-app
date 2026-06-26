package com.resumainer.dto.admin;

/**
 * Read-only contacts section for admin user details.
 * Maps to contact_detail table. resumeEmail comes from contact_detail.resume_email.
 */
public class AdminUserContactDto {

    private String fullName;
    private String professionalTitle;
    private String phone;
    private String resumeEmail;
    private String location;
    private String linkedinUrl;
    private String portfolioUrl;
    private String telegram;
    private String whatsapp;

    public AdminUserContactDto() {
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getResumeEmail() { return resumeEmail; }
    public void setResumeEmail(String resumeEmail) { this.resumeEmail = resumeEmail; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public String getTelegram() { return telegram; }
    public void setTelegram(String telegram) { this.telegram = telegram; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
}
