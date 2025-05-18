DO $$
    BEGIN
        -- Check if column 'ammunition' exists in "groupreport" table
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'groupreport'
              AND column_name = 'ammunition'
        ) THEN
            EXECUTE 'ALTER TABLE "groupreport" ADD COLUMN ammunition JSONB NOT NULL DEFAULT ''{}''';
        END IF;
    END $$;
