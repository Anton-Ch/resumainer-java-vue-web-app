package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao;
import com.resumainer.dao.ResumeBudgetConfigDao.BudgetConfig;
import com.resumainer.dao.ResumeBudgetConfigDao.SectionBudget;
import com.resumainer.dao.ResumeBudgetConfigDao.WorkExperienceDistributionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ResumeBudgetConfigService.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class ResumeBudgetConfigServiceTest {

    @Mock
    private ResumeBudgetConfigDao dao;

    private ResumeBudgetConfigService service;

    @BeforeEach
    void setUp() {
        service = new ResumeBudgetConfigService(dao);
        when(dao.loadActiveConfig()).thenReturn(new BudgetConfig(1L, "Test Budget", 1));
    }

    @Test
    void loadsActiveBudgetConfig() {
        BudgetConfig config = service.getActiveBudgetConfig();
        assertNotNull(config);
        assertEquals(1L, config.id);
        assertEquals("Test Budget", config.name);
        assertEquals(1, config.versionNo);
    }

    @Test
    void throwsWhenNoActiveConfig() {
        when(dao.loadActiveConfig()).thenReturn(null);
        assertThrows(IllegalStateException.class, () ->
            service.getActiveBudgetConfig());
    }

    @Test
    void loadsSkillsGroupsBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "groups"))
                .thenReturn(new SectionBudget(4, 5));

        assertEquals(4, service.getSkillsGroups());
        assertEquals(5, service.getSkillsGroupsMax());
    }

    @Test
    void loadsSkillsPerGroupBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "skills_per_group"))
                .thenReturn(new SectionBudget(5, 7));

        assertEquals(5, service.getSkillsPerGroup());
        assertEquals(7, service.getSkillsPerGroupMax());
    }

    @Test
    void loadsWordsPerSkillBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "words_per_skill"))
                .thenReturn(new SectionBudget(10, 20));

        assertEquals(10, service.getWordsPerSkill());
        assertEquals(20, service.getWordsPerSkillMax());
    }

    @Test
    void loadsCoursesBudget() {
        when(dao.loadSectionBudget(1L, "courses", "medium", "max_courses"))
                .thenReturn(new SectionBudget(0, 7));
        when(dao.loadSectionBudget(1L, "courses", "medium", "focus_words_per_course"))
                .thenReturn(new SectionBudget(1, 3));

        assertEquals(7, service.getMaxCourses());
        assertEquals(1, service.getCourseFocusWordsMin());
        assertEquals(3, service.getCourseFocusWordsMax());
    }

    @Test
    void loadsProjectsBudget() {
        when(dao.loadSectionBudget(1L, "projects", "light", "max_projects"))
                .thenReturn(new SectionBudget(0, 4));
        when(dao.loadSectionBudget(1L, "projects", "light", "sentences_per_project"))
                .thenReturn(new SectionBudget(2, 3));
        when(dao.loadSectionBudget(1L, "projects", "light", "bullet_points_per_project"))
                .thenReturn(new SectionBudget(2, 4));

        assertEquals(4, service.getMaxProjects());
        assertEquals(2, service.getProjectSentencesMin());
        assertEquals(3, service.getProjectSentencesMax());
    }

    // ─── Edge cases: null SectionBudget ────────────────────────────

    @Test
    void returnsZeroMin_whenNoSectionBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "groups")).thenReturn(null);

        assertEquals(0, service.getSkillsGroups());
    }

    @Test
    void returnsZeroMax_whenNoSectionBudget() {
        when(dao.loadSectionBudget(1L, "skills", "light", "groups")).thenReturn(null);

        assertEquals(0, service.getSkillsGroupsMax());
    }

    @Test
    void returnsZeroMin_whenNullMinValue() {
        when(dao.loadSectionBudget(1L, "skills", "light", "groups"))
                .thenReturn(new SectionBudget(null, 5));

        assertEquals(0, service.getSkillsGroups());
    }

    @Test
    void returnsZeroMax_whenNullMaxValue() {
        when(dao.loadSectionBudget(1L, "skills", "light", "groups"))
                .thenReturn(new SectionBudget(4, null));

        assertEquals(0, service.getSkillsGroupsMax());
    }

    @Test
    void throwsWhenNoActiveConfig_inAnyBudgetMethod() {
        when(dao.loadActiveConfig()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> service.getSkillsGroups());
        assertThrows(IllegalStateException.class, () -> service.getSkillsGroupsMax());
        assertThrows(IllegalStateException.class, () -> service.getMaxCourses());
        assertThrows(IllegalStateException.class, () -> service.getMaxProjects());
    }

    // ─── Work Experience Distribution Rules ───────────────────────

    @Test
    void loadsWorkExperienceDistributionRules() {
        WorkExperienceDistributionRule rule = new WorkExperienceDistributionRule(
                "default", 0, 10, 0, 3,
                false, "standard", 2, 3, 5, 100);
        when(dao.loadWorkExperienceDistributionRules(1L)).thenReturn(List.of(rule));

        List<WorkExperienceDistributionRule> rules = service.getWorkExperienceDistributionRules();

        assertEquals(1, rules.size());
        assertEquals("default", rules.get(0).caseKey);
        verify(dao).loadWorkExperienceDistributionRules(1L);
    }

    @Test
    void loadWorkExperienceDistributionRules_throwsWhenNoActiveConfig() {
        when(dao.loadActiveConfig()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> service.getWorkExperienceDistributionRules());
    }
}
