package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests DB-backed Work Experience distribution logic.
 *
 * The resolver must not hardcode a single max job count. It must select
 * the matching resume_work_experience_distribution_rules row by priority
 * and calculate actual target counts from profile counts.
 */
@ExtendWith(MockitoExtension.class)
class WorkExperienceBudgetResolverTest {

    @Mock
    private ResumeBudgetConfigService budgetConfigService;

    private WorkExperienceBudgetResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new WorkExperienceBudgetResolver(budgetConfigService);
        // lenient because validation tests such as totalJobs=0 fail before resolver reads DB rules.
        lenient().when(budgetConfigService.getWorkExperienceDistributionRules()).thenReturn(defaultRules());
    }

    @Test
    void resolve_oneJobNoProjects_returnsEc001OnePage() {
        assertBudget(resolver.resolve(1, 2, 0), "EC-001", "one_page", 1, 0, 1, 0, 1);
    }

    @Test
    void resolve_twoJobsNoProjects_returnsEc002OnePage() {
        assertBudget(resolver.resolve(2, 2, 0), "EC-002", "one_page", 2, 0, 2, 0, 2);
    }

    @Test
    void resolve_threeJobsNoProjects_returnsEc003OnePage() {
        assertBudget(resolver.resolve(3, 2, 0), "EC-003", "one_page", 3, 0, 3, 0, 3);
    }

    @Test
    void resolve_oneJobWithOneProject_returnsEc004TwoPageWithoutAdditionalWorkExperience() {
        assertBudget(resolver.resolve(1, 2, 1), "EC-004", "two_page", 1, 0, 1, 0, 1);
    }

    @Test
    void resolve_twoJobsWithOneProject_returnsEc005TwoPageWithoutAdditionalWorkExperience() {
        assertBudget(resolver.resolve(2, 2, 1), "EC-005", "two_page", 2, 0, 2, 0, 2);
    }

    @Test
    void resolve_threeJobsWithOneProject_returnsEc006TwoPageWithoutAdditionalWorkExperience() {
        assertBudget(resolver.resolve(3, 2, 1), "EC-006", "two_page", 3, 0, 3, 0, 3);
    }

    @Test
    void resolve_oneJobWithTwoOrMoreProjects_returnsEc007TwoPageWithoutAdditionalWorkExperience() {
        assertBudget(resolver.resolve(1, 2, 2), "EC-007", "two_page", 1, 0, 1, 0, 1);
    }

    @Test
    void resolve_twoJobsWithTwoOrMoreProjects_returnsEc008TwoPageWithoutAdditionalWorkExperience() {
        assertBudget(resolver.resolve(2, 2, 3), "EC-008", "two_page", 2, 0, 2, 0, 2);
    }

    @Test
    void resolve_threeJobsWithTwoOrMoreProjects_returnsEc009TwoPageWithoutAdditionalWorkExperience() {
        assertBudget(resolver.resolve(3, 2, 4), "EC-009", "two_page", 3, 0, 3, 0, 3);
    }

    @Test
    void resolve_fourJobsNoProjectsWithCourses_returnsEc010TwoPageTwoAndTwo() {
        assertBudget(resolver.resolve(4, 6, 0), "EC-010", "two_page", 2, 2, 2, 2, 4);
    }

    @Test
    void resolve_fourJobsWithProjects_returnsEc011TwoPageTwoAndTwo() {
        assertBudget(resolver.resolve(4, 6, 1), "EC-011", "two_page", 2, 2, 2, 2, 4);
    }

    @Test
    void resolve_fiveJobsNoProjectsWithCourses_returnsEc012TwoPageThreeAndTwo() {
        assertBudget(resolver.resolve(5, 6, 0), "EC-012", "two_page", 3, 2, 3, 2, 5);
    }

    @Test
    void resolve_fiveJobsWithProjects_returnsEc013TwoPageThreeAndTwo() {
        assertBudget(resolver.resolve(5, 6, 1), "EC-013", "two_page", 3, 2, 3, 2, 5);
    }

    @Test
    void resolve_sixJobsNoProjects_returnsEc014TwoPageThreeAndThree() {
        assertBudget(resolver.resolve(6, 6, 0), "EC-014", "two_page", 3, 3, 3, 3, 6);
    }

    @Test
    void resolve_sixJobsWithProjects_returnsEc015TwoPageThreeAndThree() {
        assertBudget(resolver.resolve(6, 6, 2), "EC-015", "two_page", 3, 3, 3, 3, 6);
    }

    @Test
    void resolve_sixteenJobsWithCoursesAndProjects_returnsEc016TwoPageThreeAndSeven() {
        assertBudget(resolver.resolve(16, 26, 11), "EC-016", "two_page", 3, 7, 3, 7, 10);
    }

    @Test
    void resolve_fourJobsWithoutCoursesAndProjects_prefersEc017CourseFreeOnePageExpansion() {
        WorkExperienceBudgetResolver.WorkExperienceBudget budget = resolver.resolve(4, 0, 0);

        assertEquals("EC-017", budget.caseKey,
                "EC-017 has higher priority than EC-010 and must be selected first");
        assertEquals("one_page", budget.templateMode);
        assertEquals(5, budget.page1Capacity,
                "DB rule capacity is 5, but actual selected jobs must not exceed totalJobs");
        assertEquals(0, budget.page2Capacity);
        assertEquals(4, budget.targetPage1Jobs,
                "With only 4 source jobs, actual Page 1 target is 4, not artificial 5");
        assertEquals(0, budget.targetPage2Jobs);
        assertEquals(4, budget.maxTotalJobs);
    }

    @Test
    void resolve_fiveJobsWithoutCoursesAndProjects_prefersEc017CourseFreeOnePageExpansion() {
        assertBudget(resolver.resolve(5, 0, 0), "EC-017", "one_page", 5, 0, 5, 0, 5);
    }

    @Test
    void resolve_zeroJobs_throwsClearError() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(0, 0, 0));

        assertTrue(ex.getMessage().contains("totalJobs must be positive"));
    }

    private void assertBudget(
            WorkExperienceBudgetResolver.WorkExperienceBudget budget,
            String caseKey,
            String templateMode,
            int page1Capacity,
            int page2Capacity,
            int targetPage1Jobs,
            int targetPage2Jobs,
            int maxTotalJobs
    ) {
        assertEquals(caseKey, budget.caseKey);
        assertEquals(templateMode, budget.templateMode);
        assertEquals(page1Capacity, budget.page1Capacity);
        assertEquals(page2Capacity, budget.page2Capacity);
        assertEquals(targetPage1Jobs, budget.targetPage1Jobs);
        assertEquals(targetPage2Jobs, budget.targetPage2Jobs);
        assertEquals(maxTotalJobs, budget.maxTotalJobs);
    }

    private List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> defaultRules() {
        return List.of(
                rule("EC-017", 4, 5, 0, 0, true,  "one_page", 5, 0, null, 5),

                rule("EC-001", 1, 1, 0, 0, false, "one_page", 1, 0, null, 10),
                rule("EC-002", 2, 2, 0, 0, false, "one_page", 2, 0, null, 10),
                rule("EC-003", 3, 3, 0, 0, false, "one_page", 3, 0, null, 10),

                rule("EC-004", 1, 1, 1, 1, false, "two_page", 1, 0, 0, 15),
                rule("EC-005", 2, 2, 1, 1, false, "two_page", 2, 0, 0, 15),
                rule("EC-006", 3, 3, 1, 1, false, "two_page", 3, 0, 0, 15),
                rule("EC-007", 1, 1, 2, null, false, "two_page", 1, 0, 0, 15),
                rule("EC-008", 2, 2, 2, null, false, "two_page", 2, 0, 0, 15),
                rule("EC-009", 3, 3, 2, null, false, "two_page", 3, 0, 0, 15),

                rule("EC-010", 4, 4, 0, 0, false, "two_page", 2, 2, 2, 20),
                rule("EC-011", 4, 4, 1, null, false, "two_page", 2, 2, 2, 20),
                rule("EC-012", 5, 5, 0, 0, false, "two_page", 3, 2, 2, 20),
                rule("EC-013", 5, 5, 1, null, false, "two_page", 3, 2, 2, 20),

                rule("EC-014", 6, 6, 0, 0, false, "two_page", 3, 3, 3, 25),
                rule("EC-015", 6, 6, 1, null, false, "two_page", 3, 3, 3, 25),

                rule("EC-016", 7, 99, 0, null, false, "two_page", 3, 7, 7, 30)
        );
    }

    private ResumeBudgetConfigDao.WorkExperienceDistributionRule rule(
            String caseKey,
            int minTotalJobs,
            int maxTotalJobs,
            int minProjects,
            Integer maxProjects,
            boolean requireNoCourses,
            String templateMode,
            int page1Jobs,
            int page2Jobs,
            Integer page2MaxAdditionalJobs,
            int priority
    ) {
        return new ResumeBudgetConfigDao.WorkExperienceDistributionRule(
                caseKey,
                minTotalJobs,
                maxTotalJobs,
                minProjects,
                maxProjects,
                requireNoCourses,
                templateMode,
                page1Jobs,
                page2Jobs,
                page2MaxAdditionalJobs,
                priority
        );
    }
}
