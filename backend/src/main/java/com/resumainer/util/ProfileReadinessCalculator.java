package com.resumainer.util;

import com.resumainer.model.User;
import com.resumainer.model.UserHomeSummary.ProfileChecklist;

/**
 * Calculates profile readiness based on user data completeness.
 * <p>
 * Formula: {@code profileReady = contactComplete && hasWorkExperience && hasEducation}
 * <ul>
 *   <li>Contact complete: fullName + email + phone + location are non-null</li>
 *   <li>Work experience present: at least one complete non-deleted record</li>
 *   <li>Education present: at least one complete non-deleted record</li>
 * </ul>
 * Courses/certificates are not required for MVP readiness.
 */
public class ProfileReadinessCalculator {

    private ProfileReadinessCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Calculate profile readiness for a given user.
     *
     * @param contactComplete     whether contact details are complete
     * @param hasWorkExperience   whether at least one work experience exists
     * @param hasEducation        whether at least one education exists
     * @return a ProfileChecklist with all statuses
     */
    public static ProfileChecklist calculateChecklist(boolean contactComplete,
                                                       boolean hasWorkExperience,
                                                       boolean hasEducation) {
        return new ProfileChecklist(contactComplete, hasWorkExperience, hasEducation);
    }

    /**
     * Determine if the profile is ready for resume generation.
     */
    public static boolean isReady(ProfileChecklist checklist) {
        return checklist != null
                && checklist.isContactDetails()
                && checklist.isWorkExperience()
                && checklist.isEducation();
    }

    /**
     * Convenience method: compute readiness from raw flags.
     */
    public static boolean isReady(boolean contactComplete, boolean hasWorkExperience, boolean hasEducation) {
        return contactComplete && hasWorkExperience && hasEducation;
    }
}
