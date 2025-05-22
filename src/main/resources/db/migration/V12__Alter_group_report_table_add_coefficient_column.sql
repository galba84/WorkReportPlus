DO $$
    BEGIN
        -- Check if column 'coefficient' exists in "groupreport" table
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'groupreport'
              AND column_name = 'coefficient'
        ) THEN
            EXECUTE 'ALTER TABLE "groupreport" ADD COLUMN coefficient JSONB NOT NULL DEFAULT ''{}''';
        END IF;
    END $$;
