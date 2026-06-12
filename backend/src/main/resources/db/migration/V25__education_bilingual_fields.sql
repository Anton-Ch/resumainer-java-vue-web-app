-- =============================================================================
-- ResumAIner — Update education table with bilingual fields
-- =============================================================================
-- Education is profile-owned factual data (not AI-generated). Bilingual fields
-- enable EN/RU resume rendering without AI translation. Old single-language
-- columns are migrated to both language variants. (DEC-070, ERD v4.0)
-- =============================================================================

-- Step 1: Add new bilingual columns (nullable initially for data migration)
ALTER TABLE education
    ADD COLUMN IF NOT EXISTS institution_name_ru VARCHAR(255),
    ADD COLUMN IF NOT EXISTS institution_name_en VARCHAR(255),
    ADD COLUMN IF NOT EXISTS degree_ru           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS degree_en           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS field_of_study_ru   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS field_of_study_en   VARCHAR(255);

-- Step 2: Migrate existing data — copy single-language values to both RU and EN
-- This preserves existing data; users can edit individual variants later.
UPDATE education
SET
    institution_name_ru = COALESCE(institution_name_ru, institution_name),
    institution_name_en = COALESCE(institution_name_en, institution_name),
    degree_ru           = COALESCE(degree_ru, degree),
    degree_en           = COALESCE(degree_en, degree),
    field_of_study_ru   = COALESCE(field_of_study_ru, field_of_study),
    field_of_study_en   = COALESCE(field_of_study_en, field_of_study)
WHERE
    institution_name_ru IS NULL
    OR institution_name_en IS NULL
    OR degree_ru IS NULL
    OR degree_en IS NULL
    OR field_of_study_ru IS NULL
    OR field_of_study_en IS NULL;

-- Step 3: Make new columns NOT NULL after data migration
ALTER TABLE education
    ALTER COLUMN institution_name_ru SET NOT NULL,
    ALTER COLUMN institution_name_en SET NOT NULL,
    ALTER COLUMN degree_ru           SET NOT NULL,
    ALTER COLUMN degree_en           SET NOT NULL,
    ALTER COLUMN field_of_study_ru   SET NOT NULL,
    ALTER COLUMN field_of_study_en   SET NOT NULL;

-- Step 4: Drop old single-language columns
ALTER TABLE education
    DROP COLUMN IF EXISTS institution_name,
    DROP COLUMN IF EXISTS degree,
    DROP COLUMN IF EXISTS field_of_study;

COMMENT ON COLUMN education.institution_name_ru IS 'Institution name in Russian';
COMMENT ON COLUMN education.institution_name_en IS 'Institution name in English';
COMMENT ON COLUMN education.degree_ru           IS 'Degree in Russian (Bachelor, Master, etc.)';
COMMENT ON COLUMN education.degree_en           IS 'Degree in English (Bachelor, Master, etc.)';
COMMENT ON COLUMN education.field_of_study_ru   IS 'Field of study in Russian';
COMMENT ON COLUMN education.field_of_study_en   IS 'Field of study in English';
