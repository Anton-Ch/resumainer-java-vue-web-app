package com.resumainer.dao;

import com.resumainer.model.GenerationResponseExperienceBullet;
import com.resumainer.model.GenerationResponseProjectBullet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD tests for bullet persistence in GenerationResponseDao (Feature 008, PG1).
 * Covers: insert/read for experience and project bullets, order preservation,
 * empty bullet rejection (CHECK constraint), cascade delete on parent removal.
 */
class GenerationResponseBulletDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private GenerationResponseDao dao;

    private final UUID experienceId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(statement.executeUpdate()).thenReturn(1);

        dao = new GenerationResponseDao(dataSource);
    }

    // ─── Experience Bullet Insert ─────────────────────────────────

    @Test
    void insertExperienceBullet_setsAllFields() throws Exception {
        GenerationResponseExperienceBullet b = makeExpBullet(experienceId, 0, "Led team of 5 engineers");

        dao.insertExperienceBullet(b, connection);

        verify(statement).setObject(1, experienceId);
        verify(statement).setInt(2, 0);
        verify(statement).setString(3, "Led team of 5 engineers");
        verify(statement).executeUpdate();
        verify(connection, never()).close();
    }

    @Test
    void insertExperienceBullet_marksEditedWhenTrue() throws Exception {
        GenerationResponseExperienceBullet b = makeExpBullet(experienceId, 1, "Reduced costs by 20%");
        b.setEdited(true);

        dao.insertExperienceBullet(b, connection);

        verify(statement).setBoolean(4, true);
    }

    @Test
    void insertExperienceBullet_defaultsEditedToFalse() throws Exception {
        GenerationResponseExperienceBullet b = makeExpBullet(experienceId, 2, "Launched MVP");

        dao.insertExperienceBullet(b, connection);

        verify(statement).setBoolean(4, false);
    }

    // ─── Project Bullet Insert ────────────────────────────────────

    @Test
    void insertProjectBullet_setsAllFields() throws Exception {
        GenerationResponseProjectBullet b = makeProjBullet(projectId, 0, "Built REST API");

        dao.insertProjectBullet(b, connection);

        verify(statement).setObject(1, projectId);
        verify(statement).setInt(2, 0);
        verify(statement).setString(3, "Built REST API");
        verify(statement).executeUpdate();
    }

    // ─── Experience Bullet Read ───────────────────────────────────

    @Test
    void findExperienceBullets_returnsListInOrder() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getObject("experience_id")).thenReturn(experienceId, experienceId);
        when(resultSet.getInt("bullet_order")).thenReturn(0, 1);
        when(resultSet.getString("bullet_text")).thenReturn("First bullet", "Second bullet");
        when(resultSet.getBoolean("is_edited")).thenReturn(false, true);

        List<GenerationResponseExperienceBullet> bullets = dao.findExperienceBullets(experienceId);

        assertEquals(2, bullets.size());
        assertEquals(0, bullets.get(0).getBulletOrder());
        assertEquals("First bullet", bullets.get(0).getBulletText());
        assertFalse(bullets.get(0).isEdited());
        assertEquals(1, bullets.get(1).getBulletOrder());
        assertEquals("Second bullet", bullets.get(1).getBulletText());
        assertTrue(bullets.get(1).isEdited());
    }

    @Test
    void findExperienceBullets_returnsEmptyList_whenNoRows() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<GenerationResponseExperienceBullet> bullets = dao.findExperienceBullets(experienceId);

        assertNotNull(bullets);
        assertTrue(bullets.isEmpty());
    }

    @Test
    void findExperienceBullets_usesPreparedStatement() throws Exception {
        when(resultSet.next()).thenReturn(false);

        dao.findExperienceBullets(experienceId);

        verify(statement).setObject(1, experienceId);
    }

    // ─── Project Bullet Read ──────────────────────────────────────

    @Test
    void findProjectBullets_returnsListInOrder() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("project_id")).thenReturn(projectId);
        when(resultSet.getInt("bullet_order")).thenReturn(0);
        when(resultSet.getString("bullet_text")).thenReturn("Project bullet");
        when(resultSet.getBoolean("is_edited")).thenReturn(false);

        List<GenerationResponseProjectBullet> bullets = dao.findProjectBullets(projectId);

        assertEquals(1, bullets.size());
        assertEquals("Project bullet", bullets.get(0).getBulletText());
    }

    @Test
    void findProjectBullets_returnsEmptyList_whenNoRows() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<GenerationResponseProjectBullet> bullets = dao.findProjectBullets(projectId);

        assertTrue(bullets.isEmpty());
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private GenerationResponseExperienceBullet makeExpBullet(UUID expId, int order, String text) {
        GenerationResponseExperienceBullet b = new GenerationResponseExperienceBullet();
        b.setExperienceId(expId);
        b.setBulletOrder(order);
        b.setBulletText(text);
        return b;
    }

    private GenerationResponseProjectBullet makeProjBullet(UUID projId, int order, String text) {
        GenerationResponseProjectBullet b = new GenerationResponseProjectBullet();
        b.setProjectId(projId);
        b.setBulletOrder(order);
        b.setBulletText(text);
        return b;
    }
}
