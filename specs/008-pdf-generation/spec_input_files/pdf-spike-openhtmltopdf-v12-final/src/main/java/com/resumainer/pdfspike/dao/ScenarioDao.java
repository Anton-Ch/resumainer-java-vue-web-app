package com.resumainer.pdfspike.dao;

import com.resumainer.pdfspike.db.SimpleConnectionPool;
import com.resumainer.pdfspike.model.*;
import com.resumainer.pdfspike.budget.EdgeCaseRuleProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SPIKE ONLY - DO NOT PORT AS PRODUCTION DAO.
 * Reads edge_case_rule/mock_candidate/mock_scenario tables used only by the standalone PDF spike.
 * In the capstone app, replace this with real profile/generated-response/saved-resume data access.
 */
public final class ScenarioDao implements EdgeCaseRuleProvider {
    private final SimpleConnectionPool pool;
    public ScenarioDao(SimpleConnectionPool pool) { this.pool = pool; }

    public List<Scenario> loadEdgeScenarios() {
        List<Scenario> list = new ArrayList<>();
        try (Connection c = pool.getConnection(); var st = c.createStatement(); var rs = st.executeQuery("SELECT * FROM mock_scenario ORDER BY ec_number, language")) {
            while (rs.next()) list.add(new Scenario(rs.getString("scenario_key"), rs.getInt("ec_number"), Language.valueOf(rs.getString("language")), rs.getInt("expected_pages")));
        } catch (SQLException e) { throw new IllegalStateException("Failed to load scenarios", e); }
        return list;
    }

    @Override
    public EdgeCaseRule loadRule(int ec) {
        try (Connection c = pool.getConnection(); var ps = c.prepareStatement("SELECT * FROM edge_case_rule WHERE ec_number=?")) {
            ps.setInt(1, ec);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Unknown EC: " + ec);
                return new EdgeCaseRule(
                        rs.getInt("ec_number"), rs.getInt("min_work"), rs.getInt("max_work"), rs.getInt("project_count"), rs.getInt("course_count"),
                        TemplateMode.fromDb(rs.getString("template_mode")), rs.getInt("page1_work_items"), rs.getInt("page2_work_items"), rs.getInt("max_total_work_items"), rs.getString("reason"));
            }
        } catch (SQLException e) { throw new IllegalStateException("Failed to load rule", e); }
    }

    public MockCandidate loadCandidate(int ec) {
        try (Connection c = pool.getConnection(); var ps = c.prepareStatement("SELECT * FROM mock_candidate WHERE ec_number=?")) {
            ps.setInt(1, ec);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Unknown candidate EC: " + ec);
                return new MockCandidate(
                        rs.getInt("ec_number"), rs.getString("en_full_name"), rs.getString("ru_full_name"), rs.getString("en_title"), rs.getString("ru_title"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("en_location"), rs.getString("ru_location"), rs.getString("linkedin"), rs.getString("portfolio"),
                        rs.getString("telegram"), rs.getString("whatsapp"), rs.getInt("work_count"), rs.getInt("project_count"), rs.getInt("course_count"));
            }
        } catch (SQLException e) { throw new IllegalStateException("Failed to load candidate", e); }
    }
}
