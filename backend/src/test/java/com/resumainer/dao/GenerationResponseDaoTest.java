package com.resumainer.dao;

import com.resumainer.model.GenerationResponseCourse;
import com.resumainer.model.GenerationResponseExperience;
import com.resumainer.model.GenerationResponseProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * DAO tests for generated response child-section inserts.
 *
 * These tests protect source_id persistence for AI-generated repeatable sections.
 */
class GenerationResponseDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private GenerationResponseDao dao;

    private final UUID responseId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new GenerationResponseDao(dataSource);
    }

    @Test
    void insertExperience_persistsSourceId() throws Exception {
        GenerationResponseExperience exp = new GenerationResponseExperience();
        exp.setResponseId(responseId);
        exp.setSourceId("work-5");
        exp.setJobTitle("Business Analyst");
        exp.setCompanyName("Bobrosoft");
        exp.setDescription("Gathered requirements.");
        exp.setLocation("Astana");
        exp.setFirstPage(true);
        exp.setStartDate(LocalDate.of(2025, 5, 1));
        exp.setEndDate(null);
        exp.setOrderInResume(0);

        dao.insertExperience(exp, connection);

        verify(connection).prepareStatement(contains("source_id"));
        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "work-5");
        verify(statement).setString(3, "Business Analyst");
        verify(statement).setString(4, "Bobrosoft");
        verify(statement).setString(5, "Gathered requirements.");
        verify(statement).setString(6, "Astana");
        verify(statement).setBoolean(7, true);
        verify(statement).setDate(8, Date.valueOf(LocalDate.of(2025, 5, 1)));
        verify(statement).setNull(9, java.sql.Types.DATE);
        verify(statement).setInt(10, 0);
        verify(statement).executeUpdate();
    }

    @Test
    void insertCourse_persistsSourceId() throws Exception {
        GenerationResponseCourse course = new GenerationResponseCourse();
        course.setResponseId(responseId);
        course.setSourceId("course-5");
        course.setName("Microsoft Business Analysis");
        course.setProvider("Coursera");
        course.setFirstPage(true);
        course.setCourseFocus("Business analysis");
        course.setOrderInResume(1);

        dao.insertCourse(course, connection);

        verify(connection).prepareStatement(contains("source_id"));
        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "course-5");
        verify(statement).setString(3, "Microsoft Business Analysis");
        verify(statement).setString(4, "Coursera");
        verify(statement).setBoolean(5, true);
        verify(statement).setString(6, "Business analysis");
        verify(statement).setInt(7, 1);
        verify(statement).executeUpdate();
    }

    @Test
    void insertProject_persistsSourceId() throws Exception {
        GenerationResponseProject project = new GenerationResponseProject();
        project.setResponseId(responseId);
        project.setSourceId("project-2");
        project.setProjectName("Reporting Optimization");
        project.setRole("Developer");
        project.setDescription("Optimized reporting workflow.");
        project.setLocation("Ust-Kamenogorsk");
        project.setStartDate(LocalDate.of(2026, 5, 1));
        project.setEndDate(null);
        project.setOrderInResume(2);

        dao.insertProject(project, connection);

        verify(connection).prepareStatement(contains("source_id"));
        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "project-2");
        verify(statement).setString(3, "Reporting Optimization");
        verify(statement).setString(4, "Developer");
        verify(statement).setString(5, "Optimized reporting workflow.");
        verify(statement).setString(6, "Ust-Kamenogorsk");
        verify(statement).setDate(7, Date.valueOf(LocalDate.of(2026, 5, 1)));
        verify(statement).setNull(8, java.sql.Types.DATE);
        verify(statement).setInt(9, 2);
        verify(statement).executeUpdate();
    }
}
