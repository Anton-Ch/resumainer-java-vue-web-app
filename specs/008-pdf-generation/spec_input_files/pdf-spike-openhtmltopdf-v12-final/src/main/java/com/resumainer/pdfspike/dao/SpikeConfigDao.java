package com.resumainer.pdfspike.dao;

import com.resumainer.pdfspike.db.SimpleConnectionPool;
import com.resumainer.pdfspike.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public final class SpikeConfigDao {
    private final SimpleConnectionPool pool;
    public SpikeConfigDao(SimpleConnectionPool pool) { this.pool = pool; }

    public FitLimits loadFitLimits() {
        try (Connection c = pool.getConnection(); var st = c.createStatement(); var rs = st.executeQuery("SELECT * FROM pdf_fit_limits WHERE id=1")) {
            if (!rs.next()) throw new IllegalStateException("pdf_fit_limits row id=1 not found");
            return new FitLimits(
                    rs.getInt("max_attempts"), rs.getDouble("step_percent"), rs.getDouble("page2_delta_limit_percent"),
                    rs.getDouble("body_font_min_px"), rs.getDouble("body_font_default_px"), rs.getDouble("body_font_max_px"),
                    rs.getDouble("line_height_min"), rs.getDouble("line_height_default"), rs.getDouble("line_height_max"),
                    rs.getDouble("section_gap_min_px"), rs.getDouble("section_gap_default_px"), rs.getDouble("section_gap_max_px"),
                    rs.getDouble("item_gap_min_px"), rs.getDouble("item_gap_default_px"), rs.getDouble("item_gap_max_px"),
                    rs.getDouble("paragraph_gap_min_px"), rs.getDouble("paragraph_gap_default_px"), rs.getDouble("paragraph_gap_max_px"),
                    rs.getDouble("bullet_gap_min_px"), rs.getDouble("bullet_gap_default_px"), rs.getDouble("bullet_gap_max_px"));
        } catch (SQLException e) { throw new IllegalStateException("Failed to load fit limits", e); }
    }

    public Map<Integer, FillTarget> loadFillTargets(int pageCount) {
        Map<Integer, FillTarget> map = new LinkedHashMap<>();
        try (Connection c = pool.getConnection(); var ps = c.prepareStatement("SELECT * FROM pdf_fill_targets WHERE page_count=? ORDER BY page_number")) {
            ps.setInt(1, pageCount);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    Double max = rs.getObject("max_fill_ratio") == null ? null : rs.getDouble("max_fill_ratio");
                    FillTarget t = new FillTarget(rs.getInt("page_count"), rs.getInt("page_number"), rs.getDouble("min_fill_ratio"), max, rs.getInt("required_non_empty") == 1);
                    map.put(t.pageNumber(), t);
                }
            }
        } catch (SQLException e) { throw new IllegalStateException("Failed to load fill targets", e); }
        return map;
    }
}
