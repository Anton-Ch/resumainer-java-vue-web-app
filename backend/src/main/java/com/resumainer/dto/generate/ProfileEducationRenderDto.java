package com.resumainer.dto.generate;

/**
 * Lightweight Education DTO for template rendering.
 * Selects the correct language variant (EN or RU) based on response language.
 */
public class ProfileEducationRenderDto {

    private String institutionName;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
    private String gpaGrade;

    public ProfileEducationRenderDto() {}

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getFieldOfStudy() { return fieldOfStudy; }
    public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getGpaGrade() { return gpaGrade; }
    public void setGpaGrade(String gpaGrade) { this.gpaGrade = gpaGrade; }
}
