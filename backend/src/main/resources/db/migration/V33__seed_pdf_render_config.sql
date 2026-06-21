-- =============================================================================
-- ResumAIner — Seed PDF render config with spike V12.1 defaults (Feature 008)
-- =============================================================================
-- Ports the tested fit limits and fill targets from the standalone PDF spike.
-- Adaptive page2 min-fill: 0 projects → min fill 0.30 (spike V12 rule).
-- =============================================================================

-- Fit limits: min/max values ported from spike seed.sql, max_attempts set to 30 per spec
INSERT INTO resume_pdf_fit_limits (
    config_key, active,
    body_font_min_px, body_font_max_px,
    line_height_min, line_height_max,
    section_gap_min_px, section_gap_max_px,
    item_gap_min_px, item_gap_max_px,
    paragraph_gap_min_px, paragraph_gap_max_px,
    bullet_gap_min_px, bullet_gap_max_px,
    max_attempts, page2_delta_limit_percent
) VALUES (
    'default-v1', TRUE,
    9.0, 16.0,
    1.05, 1.75,
    2.4, 50.0,
    2.4, 30.0,
    1.6, 24.0,
    0.8, 18.0,
    30, 50.0
);

-- Fill targets: page-level fill requirements per plan.md schema
INSERT INTO resume_pdf_fill_targets (fit_limits_id, target_page_count, page_number, language_code, project_count_min, project_count_max, min_fill, max_fill, priority)
VALUES
    -- 1-page: single page at 80%+ fill
    (1, 1, 1, NULL, NULL, NULL, 0.80, 0.96, 100),

    -- 2-page: page 1 at 85%+ fill
    (1, 2, 1, NULL, NULL, NULL, 0.85, 0.96, 100),

    -- 2-page: page 2 with projects → 50%+ fill (default)
    (1, 2, 2, NULL, 1, NULL, 0.50, 0.96, 100),

    -- 2-page: page 2 with 0 projects → 30%+ fill (adaptive, per V12 rule)
    (1, 2, 2, NULL, 0, 0, 0.30, 0.96, 200),

    -- 3-page fallback: page 1 at 85%+
    (1, 3, 1, NULL, NULL, NULL, 0.85, 0.96, 100),

    -- 3-page fallback: pages 2-3 at 85%+
    (1, 3, 2, NULL, NULL, NULL, 0.85, 0.96, 100),
    (1, 3, 3, NULL, NULL, NULL, 0.01, 0.96, 100);
