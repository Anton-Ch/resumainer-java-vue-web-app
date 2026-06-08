package com.resumainer.util;

import com.resumainer.model.UserHomeSummary.ProfileChecklist;

/**
 * Calculates profile readiness based on user data completeness.
 * <p>
 * Formula: {@code profileReady = contactComplete && hasWorkExperience && hasEducation && hasAdditionalInfo}
 * <ul>
 *   <li>Contact complete: fullName + email + phone + location are non-null</li>
 *   <li>Work experience present: at least one complete non-deleted record</li>
 *   <li>Education present: at least one complete non-deleted record</li>
 *   <li>Additional info present: dateOfBirth + citizenship are filled (required fields)</li>
 * </ul>
 * Courses/certificates are not required for MVP readiness.
 */
public class ProfileReadinessCalculator {

    private ProfileReadinessCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Calculate profile readiness for a given user.
     */
    public static ProfileChecklist calculateChecklist(boolean contactComplete,
                                                       boolean hasWorkExperience,
                                                       boolean hasEducation,
                                                       boolean hasAdditionalInfo) {
        return new ProfileChecklist(contactComplete, hasWorkExperience, hasEducation, hasAdditionalInfo);
    }

    /**
     * Determine if the profile is ready for resume generation.
     */
    public static boolean isReady(ProfileChecklist checklist) {
        return checklist != null
                && checklist.isContactDetails()
                && checklist.isWorkExperience()
                && checklist.isEducation()
                && checklist.isAdditionalInfo();
    }

    /**
     * Convenience method: compute readiness from raw flags.
     */
    public static boolean isReady(boolean contactComplete, boolean hasWorkExperience,
                                   boolean hasEducation, boolean hasAdditionalInfo) {
        return contactComplete && hasWorkExperience && hasEducation && hasAdditionalInfo;
    }
}
