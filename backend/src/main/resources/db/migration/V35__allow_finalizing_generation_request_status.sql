-- =============================================================================
-- ResumAIner — Allow FINALIZING status for resume finalization lock
-- =============================================================================
-- Feature 008 Phase 22B:
-- Adds 'finalizing' to resume_generation_request.status CHECK constraint.
-- Existing statuses ('pending', 'processing', 'completed', 'failed') remain unchanged.
-- =============================================================================

ALTER TABLE resume_generation_request
    DROP CONSTRAINT IF EXISTS resume_generation_request_status_check;

ALTER TABLE resume_generation_request
    ADD CONSTRAINT resume_generation_request_status_check
    CHECK (status IN ('pending', 'processing', 'completed', 'failed', 'finalizing'));
