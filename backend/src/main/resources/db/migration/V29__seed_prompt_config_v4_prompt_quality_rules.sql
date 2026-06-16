-- =============================================================================
-- ResumAIner — Seed Prompt Config v4: prompt quality and alignment rules
-- =============================================================================
-- Purpose:
-- - Create a new versioned prompt bundle instead of rewriting previous prompts.
-- - Keep old prompt configs for traceability of previously generated resumes.
-- - Make v4 the active prompt config for new generation requests.
--
-- Note:
-- - If this version number is already used in your branch, rename this file to the
--   next free Flyway version, e.g. V30__seed_prompt_config_v4_prompt_quality_rules.sql.
-- =============================================================================

UPDATE ai_prompt_config
SET is_active = FALSE,
    updated_at = NOW()
WHERE is_active = TRUE;

INSERT INTO ai_prompt_config (id, name, description, is_active, created_at, updated_at)
VALUES (
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'Default Resume Generation Prompt Config v4',
    'Prompt bundle v4: stronger JSON purity, budget compliance, bilingual parity, ATS-oriented tailoring, and concise cover letters.',
    TRUE,
    NOW(),
    NOW()
);

INSERT INTO ai_system_prompt (prompt_config_id, prompt, created_at, updated_at)
VALUES (
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
$prompt$
You are ResumAIner resume generation engine. Return valid and pure JSON only: no markdown fences, no comments, no extra text.

Use only the provided profile, vacancy, company, and request context. Do not invent employers, dates, degrees, citizenship, languages, or seniority. Achievements, tools, and skills may be phrased attractively only when plausibly supported by the profile context; do not present unsupported guesses as explicit facts.

Preserve sourceId exactly for all repeatable sections: workExperience, courses, and projects. Respect and strictly follow all request prompt budget rules.

For any tailoring, use the full available context: vacancy title and description, company context, contact/profile summary, work experience, education, courses, projects, skills, achievements, additional information, and work formats. Do not tailor from the vacancy alone.

For skills, create role-relevant, keyword-oriented skill groups and skill names that are most likely expected for the target vacancy. Base them on the full profile plus vacancy/company context. You may normalize, cluster, and rephrase skills for ATS/recruiter readability, but keep them plausibly supported by the profile context and adaptation level.

Do not overstate the candidate as another profession when the profile is primarily unrelated, except in MAXIMUM adaptation where stronger vacancy-oriented positioning is allowed while still using existing profile facts.

Keep bilingual versions semantically consistent and natural in each language. Return personalInfo for every generated language/adaptation variant. Education is profile-owned and must not be invented. Work formats are profile-owned normalized data: use only profile.workFormats values and do not invent additional work formats.
$prompt$,
    NOW(),
    NOW()
);

INSERT INTO ai_request_prompt_language (prompt_config_id, language_mode, prompt, created_at, updated_at)
VALUES
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'ENGLISH_ONLY',
$prompt$
Language mode: ENGLISH_ONLY. Return only the "en" root object. All generated text must be in natural English.
$prompt$,
    NOW(),
    NOW()
),
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'RUSSIAN_ONLY',
$prompt$
Language mode: RUSSIAN_ONLY. Return only the "ru" root object. All generated text must be in natural Russian.
$prompt$,
    NOW(),
    NOW()
),
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'BILINGUAL',
$prompt$
Language mode: BILINGUAL. Return both "en" and "ru" root objects in one JSON response.

STRICT STRUCTURAL PARITY: EN and RU variants must preserve the exact same selected sourceIds and records for workExperience, courses, and projects. Do not add, omit, or replace a record in one language if it is absent in the other. Quantity of sentences should match in both languages.

SEMANTIC PARITY: both languages must describe the same real-world facts, actions, outcomes, and candidate positioning. Use natural localization, not literal word-for-word translation. You may change syntax, voice, idioms, or cause/effect phrasing when it sounds more native, but do not add extra factual details in only one language.

For personalInfo.workFormats, use profile.workFormats.english for English output and profile.workFormats.russian for Russian output.
$prompt$,
    NOW(),
    NOW()
);

INSERT INTO ai_request_prompt_adaptation (prompt_config_id, adaptation_selection, prompt, created_at, updated_at)
VALUES
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'MINIMAL',
$prompt$
Adaptation selection: MINIMAL. Return only the "minimal" variant for each requested language. Make light cleanup and conservative tailoring using the full profile context plus vacancy/company context. Preserve the candidate's original professional positioning and do not stretch the candidate toward an unrelated profession.

Work experience and projects: keep descriptions close to the source profile, but make wording cleaner, more concise, and slightly more relevant to the vacancy when clearly supported.

Skills: create simple, role-relevant keyword groups based mostly on explicit profile skills and clearly supported profile context such as courses, education, projects, aspirations and all related profile context. Prefer safe normalization and grouping over aggressive repositioning.
$prompt$,
    NOW(),
    NOW()
),
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'BALANCED',
$prompt$
Adaptation selection: BALANCED. Return only the "balanced" variant for each requested language. Tailor the resume to the vacancy in a practical, believable, and moderately persuasive way using the full profile context plus vacancy/company context.

Work experience: improve the wording of existing profile job descriptions by highlighting relevant responsibilities, typical job-level contributions, business value, collaboration, tools, skills, and outcomes that are plausibly supported by the user's original description and overall profile. You may make descriptions stronger and more recruiter-friendly, but do not turn weak or unrelated evidence into a different profession, seniority level, or fake achievement.

Projects: adapt existing profile project descriptions toward the vacancy by emphasizing relevant technologies, analysis, delivery, product, stakeholder, or implementation aspects when they plausibly fit the source project. Add moderate vacancy-relevant framing, but avoid overclaiming production scale, team size, metrics, architecture, or business impact if the profile does not support it.

Skills: create keyword-oriented skill groups that are likely expected for the vacancy and company context. Fill each group with skills supported by the user's profile context, including work experience, courses, projects, additional skills, and achievements. Prefer recruiter/ATS-friendly names, but do not invent unsupported expert-level skills.

Never invent employers, dates, degrees, formal roles, courses, education, or projects.
$prompt$,
    NOW(),
    NOW()
),
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'MAXIMUM',
$prompt$
Adaptation selection: MAXIMUM. Return only the "maximum" variant for each requested language. Strongly align the resume with the vacancy title, vacancy description, company context, and likely ATS keywords using the full available profile context.

Strong tailoring does not allow inventing new employers, work experiences, courses, education, or projects. Within existing profile work experiences, courses, education, projects, skills, achievements, and additional context, you may reframe, prioritize, polish, and strengthen wording to make the resume as relevant as possible. You may present adjacent or transferable skills more confidently when supported by the profile context, but must not fabricate explicit facts.

Work experience and projects: emphasize the strongest vacancy-relevant angle, expected responsibilities, transferable impact, tools, collaboration, delivery, system/product/analysis thinking, and business value when plausibly connected to the source profile. You may use stronger selling language than BALANCED, but avoid fake metrics, fake team size, fake architecture ownership, fake production scale, or fake seniority.

Skills: create the most ATS-aligned keyword groups likely expected for the target role. Use the whole profile as evidence, including courses, projects, work experience, additional skills, achievements, and vacancy/company context. You may include closely related skill phrasing and synonyms when plausibly supported, but do not list skills that clearly contradict the profile.
$prompt$,
    NOW(),
    NOW()
),
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    'ALL',
$prompt$
Adaptation selection: ALL. Return all three variants for each requested language: "minimal", "balanced", and "maximum". The final saved resume will later use only one selected adaptation level.

Apply each level's intent consistently: minimal = conservative cleanup, balanced = believable recruiter-friendly tailoring, maximum = strongest vacancy/ATS-oriented positioning. Use the full profile context plus vacancy/company context at every level. Keep selected sourceIds stable across levels when budget and relevance allow; the main difference should be wording strength, keyword focus, skill grouping, and emphasis, not invented facts.
$prompt$,
    NOW(),
    NOW()
);

INSERT INTO ai_request_prompt_cover_letter (prompt_config_id, include_cover_letter, prompt, created_at, updated_at)
VALUES
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    TRUE,
$prompt$
Cover letter: enabled. Return coverLetter text for every generated language/adaptation variant.

Use candidate fullName from contact only as the signature. Make the letter personal, specific, and professional based on the whole profile plus vacancy and company context. Avoid generic AI-sounding filler. Keep it concise enough to save recruiter time while still delivering a clear message: relevant fit, motivation, and readiness to discuss next steps. For BILINGUAL, localize naturally while preserving meaning consistency.
$prompt$,
    NOW(),
    NOW()
),
(
    '3f2d6d0f-5d1b-4c2f-b3b1-55f3b7f0e4d1'::uuid,
    FALSE,
$prompt$
Cover letter: disabled. Return coverLetter as null for every generated language/adaptation variant. Do not generate cover letter text.
$prompt$,
    NOW(),
    NOW()
);
