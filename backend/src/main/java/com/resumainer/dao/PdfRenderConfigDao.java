package com.resumainer.dao;

import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.PdfFitLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for PDF render configuration: fit limits and fill targets (Feature 008).
 */
@Repository
public class PdfRenderConfigDao {

    private static final Logger log = LoggerFactory.getLogger(PdfRenderConfigDao.class);

    private static final String SELECT_ACTIVE_FIT_LIMITS =
            "SELECT * FROM resume_pdf_fit_limits WHERE active = TRUE";

    private static final String SELECT_FILL_TARGETS_BY_FIT_ID =
            "SELECT * FROM resume_pdf_fill_targets WHERE fit_limits_id = ? ORDER BY priority DESC, id";

    private final DataSource dataSource;

    public PdfRenderConfigDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Load the active fit limits configuration. Returns null if no active config exists. */
    public PdfFitLimits findActive() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_FIT_LIMITS);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapFitLimits(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error("Error loading active PDF fit limits", e);
            throw new RuntimeException("Failed to load PDF render config", e);
        }
    }

    /** Load all fill targets for a given fit limits configuration. */
    public List<PdfFillTarget> findFillTargets(long fitLimitsId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_FILL_TARGETS_BY_FIT_ID)) {
            stmt.setLong(1, fitLimitsId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<PdfFillTarget> targets = new ArrayList<>();
                while (rs.next()) {
                    targets.add(mapFillTarget(rs));
                }
                return targets;
            }
        } catch (SQLException e) {
            log.error("Error loading fill targets for fit_limits_id={}", fitLimitsId, e);
            throw new RuntimeException("Failed to load PDF fill targets", e);
        }
    }

    private PdfFitLimits mapFitLimits(ResultSet rs) throws SQLException {
        PdfFitLimits fl = new PdfFitLimits();
        fl.setId(rs.getLong("id"));
        fl.setConfigKey(rs.getString("config_key"));
        fl.setActive(rs.getBoolean("active"));
        fl.setBodyFontMinPx(rs.getBigDecimal("body_font_min_px"));
        fl.setBodyFontMaxPx(rs.getBigDecimal("body_font_max_px"));
        fl.setLineHeightMin(rs.getBigDecimal("line_height_min"));
        fl.setLineHeightMax(rs.getBigDecimal("line_height_max"));
        fl.setSectionGapMinPx(rs.getBigDecimal("section_gap_min_px"));
        fl.setSectionGapMaxPx(rs.getBigDecimal("section_gap_max_px"));
        fl.setItemGapMinPx(rs.getBigDecimal("item_gap_min_px"));
        fl.setItemGapMaxPx(rs.getBigDecimal("item_gap_max_px"));
        fl.setParagraphGapMinPx(rs.getBigDecimal("paragraph_gap_min_px"));
        fl.setParagraphGapMaxPx(rs.getBigDecimal("paragraph_gap_max_px"));
        fl.setBulletGapMinPx(rs.getBigDecimal("bullet_gap_min_px"));
        fl.setBulletGapMaxPx(rs.getBigDecimal("bullet_gap_max_px"));
        fl.setMaxAttempts(rs.getInt("max_attempts"));
        fl.setPage2DeltaLimitPercent(rs.getBigDecimal("page2_delta_limit_percent"));
        fl.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return fl;
    }

    private PdfFillTarget mapFillTarget(ResultSet rs) throws SQLException {
        PdfFillTarget ft = new PdfFillTarget();
        ft.setId(rs.getLong("id"));
        ft.setFitLimitsId(rs.getLong("fit_limits_id"));
        ft.setTargetPageCount(rs.getInt("target_page_count"));
        ft.setPageNumber(rs.getInt("page_number"));
        ft.setLanguageCode(rs.getString("language_code"));
        ft.setProjectCountMin(rs.getObject("project_count_min") != null ? rs.getInt("project_count_min") : null);
        ft.setProjectCountMax(rs.getObject("project_count_max") != null ? rs.getInt("project_count_max") : null);
        ft.setMinFill(rs.getBigDecimal("min_fill"));
        ft.setMaxFill(rs.getBigDecimal("max_fill"));
        ft.setPriority(rs.getInt("priority"));
        return ft;
    }
}
