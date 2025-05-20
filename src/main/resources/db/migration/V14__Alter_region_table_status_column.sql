DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'  -- optional: ensure correct schema
              AND table_name = 'region'
              AND column_name = 'status'
        ) THEN
            EXECUTE 'ALTER TABLE "region" ADD COLUMN status BOOLEAN DEFAULT true NOT NULL';
        END IF;
    END $$;
