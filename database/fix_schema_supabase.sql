-- ============================================================
-- FIX 1 & 2: Pass_slip schema patches for Supabase PostgreSQL
-- Run this once in Supabase SQL Editor (or via psql)
-- ============================================================

-- ── FIX 1: Add missing 'category' column ────────────────────
-- Required by PassSlipDAO.createPassSlip() and mapResultSet()
ALTER TABLE "Pass_slip"
    ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'Official Business';

-- Backfill any existing rows that have a NULL category
UPDATE "Pass_slip"
    SET category = 'Official Business'
    WHERE category IS NULL;

-- ── FIX 2: Expand status CHECK to include 'Overdue' ─────────
-- The current constraint blocks OverdueCheckerService from
-- writing status = 'Overdue', causing silent update failures.

-- Step 1: Drop the old constraint (find its name first)
-- In Supabase the constraint is usually auto-named like:
--   Pass_slip_status_check
-- Run this to confirm:
--   SELECT conname FROM pg_constraint
--   WHERE conrelid = '"Pass_slip"'::regclass AND contype = 'c';
-- Then drop by name:
ALTER TABLE "Pass_slip" DROP CONSTRAINT IF EXISTS "Pass_slip_status_check";

-- Step 2: Add the corrected constraint with 'Overdue' included
ALTER TABLE "Pass_slip"
    ADD CONSTRAINT "Pass_slip_status_check"
    CHECK (status IN ('Pending', 'Approved', 'Rejected', 'Returned', 'Overdue'));

-- ── Verify the result ────────────────────────────────────────
-- You can run these to confirm everything looks right:
--
-- SELECT column_name, data_type, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'Pass_slip'
-- ORDER BY ordinal_position;
--
-- SELECT conname, pg_get_constraintdef(oid)
-- FROM pg_constraint
-- WHERE conrelid = '"Pass_slip"'::regclass;
