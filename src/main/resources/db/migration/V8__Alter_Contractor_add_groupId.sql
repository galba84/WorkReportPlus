DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'contractor'
              AND column_name = 'group_id'
        ) THEN
            ALTER TABLE contractor
                ADD COLUMN group_id uuid REFERENCES "Group"(id) ON DELETE SET NULL;
        END IF;
    END
$$;
