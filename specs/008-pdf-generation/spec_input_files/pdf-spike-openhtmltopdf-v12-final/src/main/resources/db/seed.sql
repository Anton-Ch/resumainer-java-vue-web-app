DELETE FROM mock_scenario;
DELETE FROM mock_candidate;
DELETE FROM edge_case_rule;
DELETE FROM pdf_fill_targets;
DELETE FROM pdf_fit_limits;

INSERT INTO pdf_fit_limits VALUES (
  1,
  20,
  0.10,
  0.65,
  9.0, 12.5, 16.0,
  1.05, 1.35, 1.75,
  2.4, 15.0, 50.0,
  2.4, 9.0, 30.0,
  1.6, 5.0, 24.0,
  0.8, 3.0, 18.0
);

INSERT INTO pdf_fill_targets VALUES
  (1, 1, 0.80, 0.96, 1),
  (2, 1, 0.85, 0.96, 1),
  (2, 2, 0.50, 0.96, 1),
  (3, 1, 0.85, 0.96, 1),
  (3, 2, 0.85, 0.96, 1),
  (3, 3, 0.01, 0.96, 1);

-- SPIKE_ONLY_DO_NOT_PORT: these EC rows are only a test matrix for the standalone spike.
-- Do not port edge_case_rule/mock_candidate/mock_scenario seed data into the capstone production DB.
-- Port only the approved breakpoint logic into the real budget/config resolver.
INSERT INTO edge_case_rule VALUES
  (1, 1, 1, 0, 1, 'one_page', 1, 0, 1, '1 job, no projects'),
  (2, 2, 2, 0, 2, 'one_page', 2, 0, 2, '2 jobs, no projects'),
  (3, 3, 3, 0, 4, 'one_page', 3, 0, 3, '3 jobs, no projects'),
  (4, 1, 1, 1, 1, 'two_page', 1, 0, 1, '1 job with projects'),
  (5, 2, 2, 1, 2, 'two_page', 2, 0, 2, '2 jobs with 1 project'),
  (6, 2, 2, 2, 2, 'two_page', 2, 0, 2, '2 jobs with 2 projects'),
  (7, 3, 3, 1, 3, 'two_page', 3, 0, 3, '3 jobs with 1 project'),
  (8, 3, 3, 2, 3, 'two_page', 3, 0, 3, '3 jobs with 2 projects'),
  (9, 3, 3, 3, 4, 'two_page', 3, 0, 3, '3 jobs with 3 projects'),
  (10, 4, 4, 0, 4, 'two_page', 2, 2, 4, '4 jobs, no projects'),
  (11, 4, 4, 2, 4, 'two_page', 2, 2, 4, '4 jobs with projects'),
  (12, 5, 5, 0, 5, 'two_page', 3, 2, 5, '5 jobs, no projects'),
  (13, 5, 5, 2, 5, 'two_page', 3, 2, 5, '5 jobs with projects'),
  (14, 6, 6, 0, 5, 'two_page', 3, 3, 6, '6 jobs, no projects'),
  (15, 6, 6, 3, 5, 'two_page', 3, 3, 6, '6 jobs with projects'),
  (16, 8, 8, 3, 5, 'two_page', 3, 5, 8, '7+ jobs edge, projects first on page 2'),
  (17, 5, 5, 0, 0, 'one_page', 5, 0, 5, 'special one page expansion, no courses, no projects');

INSERT INTO mock_candidate SELECT
  ec_number,
  'Edge Candidate ' || printf('%02d', ec_number),
  'Кандидат EC-' || printf('%02d', ec_number),
  'Senior Business Analyst',
  'Старший бизнес-аналитик',
  '+7-701-000-' || printf('%04d', ec_number),
  'ec' || printf('%02d', ec_number) || '@test.com',
  'Astana, Kazakhstan',
  'Астана, Казахстан',
  'https://www.linkedin.com/in/ec' || printf('%02d', ec_number) || '/',
  'https://portfolio.example.com/ec' || printf('%02d', ec_number),
  '@ec' || printf('%02d', ec_number),
  '+7-777-777-' || printf('%02d', ec_number) || '-' || printf('%02d', ec_number),
  max_work,
  project_count,
  course_count
FROM edge_case_rule;

INSERT INTO mock_scenario
SELECT 'ec' || printf('%02d', ec_number) || '_en', ec_number, 'EN', CASE WHEN template_mode = 'one_page' THEN 1 ELSE 2 END FROM edge_case_rule;
INSERT INTO mock_scenario
SELECT 'ec' || printf('%02d', ec_number) || '_ru', ec_number, 'RU', CASE WHEN template_mode = 'one_page' THEN 1 ELSE 2 END FROM edge_case_rule;
