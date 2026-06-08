package com.resumainer.util;

import com.resumainer.model.UserHomeSummary.ProfileChecklist;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileReadinessCalculatorTest {

    @Test
    void isReady_allComplete_returnsTrue() {
        assertTrue(ProfileReadinessCalculator.isReady(true, true, true, true));
    }

    @Test
    void isReady_missingWorkExperience_returnsFalse() {
        assertFalse(ProfileReadinessCalculator.isReady(true, false, true, true));
    }

    @Test
    void isReady_missingEducation_returnsFalse() {
        assertFalse(ProfileReadinessCalculator.isReady(true, true, false, true));
    }

    @Test
    void isReady_missingContact_returnsFalse() {
        assertFalse(ProfileReadinessCalculator.isReady(false, true, true, true));
    }

    @Test
    void isReady_missingAdditionalInfo_returnsFalse() {
        assertFalse(ProfileReadinessCalculator.isReady(true, true, true, false));
    }

    @Test
    void isReady_allMissing_returnsFalse() {
        assertFalse(ProfileReadinessCalculator.isReady(false, false, false, false));
    }

    @Test
    void calculateChecklist_createsCorrectChecklist() {
        ProfileChecklist checklist = ProfileReadinessCalculator.calculateChecklist(true, false, true, true);

        assertTrue(checklist.isContactDetails());
        assertFalse(checklist.isWorkExperience());
        assertTrue(checklist.isEducation());
        assertTrue(checklist.isAdditionalInfo());
    }

    @Test
    void isReady_withNullChecklist_returnsFalse() {
        assertFalse(ProfileReadinessCalculator.isReady(null));
    }

    @Test
    void isReady_withCompleteChecklist_returnsTrue() {
        ProfileChecklist checklist = new ProfileChecklist(true, true, true, true);
        assertTrue(ProfileReadinessCalculator.isReady(checklist));
    }

    @Test
    void isReady_withChecklistMissingAdditional_returnsFalse() {
        ProfileChecklist checklist = new ProfileChecklist(true, true, true, false);
        assertFalse(ProfileReadinessCalculator.isReady(checklist));
    }
}
