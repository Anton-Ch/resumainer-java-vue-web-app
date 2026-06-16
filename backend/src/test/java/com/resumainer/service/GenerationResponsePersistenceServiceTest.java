package com.resumainer.service;

import com.resumainer.dao.AiUsageLogDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.GenerationResponsePersonalDao;
import com.resumainer.model.GenerationResponseCourse;
import com.resumainer.model.GenerationResponseExperience;
import com.resumainer.model.GenerationResponseProject;
import com.resumainer.model.ResumeGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service tests for persisting parsed AI response variants.
 *
 * These tests protect sourceId transfer from AiResponseParser parsed items
 * into generated response model objects before DAO insertion.
 */
class GenerationResponsePersistenceServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private GenerationResponseDao responseDao;
    private GenerationResponsePersonalDao personalDao;
    private GenerationRequestDao requestDao;
    private AiUsageLogDao usageLogDao;
    private GenerationResponsePersistenceService service;

    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID responseId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        responseDao = mock(GenerationResponseDao.class);
        personalDao = mock(GenerationResponsePersonalDao.class);
        requestDao = mock(GenerationRequestDao.class);
        usageLogDao = mock(AiUsageLogDao.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(responseDao.insertResponse(any(ResumeGenerationResponse.class), same(connection)))
                .thenAnswer(invocation -> {
                    ResumeGenerationResponse response = invocation.getArgument(0);
                    response.setId(responseId);
                    return response;
                });

        service = new GenerationResponsePersistenceService(
                dataSource,
                responseDao,
                personalDao,
                requestDao,
                usageLogDao
        );
    }

    @Test
    void persistResponses_passesSourceIdToGeneratedSectionModels() throws Exception {
        AiResponseParser.ParsedVariant variant = new AiResponseParser.ParsedVariant();
        variant.languageCode = "EN";
        variant.adaptationLevel = "BALANCED";
        variant.professionalTitle = "Business Analyst";
        variant.professionalSummary = "Summary";
        variant.professionalAspirations = "Aspirations";

        AiResponseParser.ExperienceItem experience = new AiResponseParser.ExperienceItem();
        experience.sourceId = "work-5";
        experience.jobTitle = "Business Analyst";
        experience.companyName = "Bobrosoft";
        experience.description = "Gathered requirements.";
        experience.location = "Astana";
        experience.startDate = "2025-05";
        experience.endDate = null;
        variant.experience.add(experience);

        AiResponseParser.CourseItem course = new AiResponseParser.CourseItem();
        course.sourceId = "course-5";
        course.name = "Microsoft Business Analysis";
        course.provider = "Coursera";
        course.courseFocus = "Business analysis";
        variant.courses.add(course);

        AiResponseParser.ProjectItem project = new AiResponseParser.ProjectItem();
        project.sourceId = "project-2";
        project.projectName = "Reporting Optimization";
        project.role = "Developer";
        project.description = "Optimized reporting workflow.";
        project.startDate = "2026-05";
        project.endDate = null;
        variant.projects.add(project);

        service.persistResponses(requestId, userId, List.of(variant));

        ArgumentCaptor<GenerationResponseExperience> experienceCaptor =
                ArgumentCaptor.forClass(GenerationResponseExperience.class);
        ArgumentCaptor<GenerationResponseCourse> courseCaptor =
                ArgumentCaptor.forClass(GenerationResponseCourse.class);
        ArgumentCaptor<GenerationResponseProject> projectCaptor =
                ArgumentCaptor.forClass(GenerationResponseProject.class);

        verify(responseDao).insertExperience(experienceCaptor.capture(), same(connection));
        verify(responseDao).insertCourse(courseCaptor.capture(), same(connection));
        verify(responseDao).insertProject(projectCaptor.capture(), same(connection));

        assertEquals("work-5", experienceCaptor.getValue().getSourceId());
        assertEquals("course-5", courseCaptor.getValue().getSourceId());
        assertEquals("project-2", projectCaptor.getValue().getSourceId());

        verify(connection).commit();
        verify(requestDao).updateStatus(requestId, userId, "completed", null, true);
    }
}
