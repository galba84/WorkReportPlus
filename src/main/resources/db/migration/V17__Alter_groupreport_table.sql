DO $$
    BEGIN
        -- Rename contractors_ids to fighting_contractors if not already renamed
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'contractors_ids'
        ) THEN
            EXECUTE 'ALTER TABLE groupreport RENAME COLUMN contractors_ids TO fighting_contractors';
        END IF;

        -- Add rest_contractors if not exists
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'rest_contractors'
        ) THEN
            ALTER TABLE groupreport ADD COLUMN rest_contractors UUID[] DEFAULT '{}'::uuid[];
        END IF;

        -- Rename place_ids to fighting_places
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'place_ids'
        ) THEN
            EXECUTE 'ALTER TABLE groupreport RENAME COLUMN place_ids TO fighting_places';
        END IF;

        -- Add rest_places if not exists
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'rest_places'
        ) THEN
            ALTER TABLE groupreport ADD COLUMN rest_places UUID[] DEFAULT '{}'::uuid[];
        END IF;

        -- Rename success_reported to success_report
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'success_reported'
        ) THEN
            EXECUTE 'ALTER TABLE groupreport RENAME COLUMN success_reported TO success_report';
        END IF;

        -- Rename is_worked to worked
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'is_worked'
        ) THEN
            EXECUTE 'ALTER TABLE groupreport RENAME COLUMN is_worked TO worked';
        END IF;

        -- Add ammo_verified if not exists
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'groupreport' AND column_name = 'ammo_verified'
        ) THEN
            ALTER TABLE groupreport ADD COLUMN ammo_verified BOOLEAN DEFAULT false NOT NULL;
        END IF;
    END
$$;

-- Create indices idempotently
CREATE INDEX IF NOT EXISTS idx_groupreport_region_report_id ON groupreport (region_report_id);
CREATE INDEX IF NOT EXISTS idx_groupreport_ammo_verified ON groupreport (ammo_verified);
