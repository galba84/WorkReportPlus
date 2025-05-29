-- Add 'is_fighting' column to "Group" table if it doesn't exist
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'Group'
              AND column_name = 'is_fighting'
        ) THEN
            ALTER TABLE "Group"
                ADD COLUMN is_fighting BOOLEAN DEFAULT false NOT NULL;
        END IF;
    END
$$;
