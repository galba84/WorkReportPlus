DO $$
    BEGIN
        -- Check if column 'status' exists in "Group" table
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'Group'
              AND column_name = 'status'
        ) THEN
            ALTER TABLE "Group"
                ADD COLUMN status boolean DEFAULT true NOT NULL;
        END IF;
    END $$;


