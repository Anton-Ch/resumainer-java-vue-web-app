package com.resumainer.dto.generate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Grouped review data for the frontend.
 * Organized by language, then by section, then by record/field and adaptation level.
 */
public class GenerationReviewDto {

    private UUID requestId;
    private List<LanguageReviewGroup> languages;

    public GenerationReviewDto() {}

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }

    public List<LanguageReviewGroup> getLanguages() { return languages; }
    public void setLanguages(List<LanguageReviewGroup> languages) { this.languages = languages; }

    // --- Nested classes ---

    public static class LanguageReviewGroup {
        private long languageId;
        private String languageCode;   // EN, RU
        private List<SectionReviewGroup> sections;

        public long getLanguageId() { return languageId; }
        public void setLanguageId(long languageId) { this.languageId = languageId; }
        public String getLanguageCode() { return languageCode; }
        public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
        public List<SectionReviewGroup> getSections() { return sections; }
        public void setSections(List<SectionReviewGroup> sections) { this.sections = sections; }
    }

    public static class SectionReviewGroup {
        private String sectionKey;   // professional_summary, work_experience, skills, etc.
        private String sectionLabel; // Display name
        private List<RecordReviewGroup> records;

        public String getSectionKey() { return sectionKey; }
        public void setSectionKey(String sectionKey) { this.sectionKey = sectionKey; }
        public String getSectionLabel() { return sectionLabel; }
        public void setSectionLabel(String sectionLabel) { this.sectionLabel = sectionLabel; }
        public List<RecordReviewGroup> getRecords() { return records; }
        public void setRecords(List<RecordReviewGroup> records) { this.records = records; }
    }

    public static class RecordReviewGroup {
        private UUID recordId;
        private int orderInResume;
        private Map<String, List<AdaptationVariant>> fieldVariants;

        public UUID getRecordId() { return recordId; }
        public void setRecordId(UUID recordId) { this.recordId = recordId; }
        public int getOrderInResume() { return orderInResume; }
        public void setOrderInResume(int orderInResume) { this.orderInResume = orderInResume; }
        public Map<String, List<AdaptationVariant>> getFieldVariants() { return fieldVariants; }
        public void setFieldVariants(Map<String, List<AdaptationVariant>> fieldVariants) { this.fieldVariants = fieldVariants; }
    }

    public static class AdaptationVariant {
        private UUID responseId;
        private long adaptationLevelId;
        private String adaptationCode;    // MINIMAL, BALANCED, MAXIMUM
        private String value;
        private String updateKey;         // Opaque key for saveReview: "sectionKey:recordId:fieldName:adaptationCode"

        public UUID getResponseId() { return responseId; }
        public void setResponseId(UUID responseId) { this.responseId = responseId; }

        public long getAdaptationLevelId() { return adaptationLevelId; }
        public void setAdaptationLevelId(long adaptationLevelId) { this.adaptationLevelId = adaptationLevelId; }
        public String getAdaptationCode() { return adaptationCode; }
        public void setAdaptationCode(String adaptationCode) { this.adaptationCode = adaptationCode; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getUpdateKey() { return updateKey; }
        public void setUpdateKey(String updateKey) { this.updateKey = updateKey; }
    }
}
