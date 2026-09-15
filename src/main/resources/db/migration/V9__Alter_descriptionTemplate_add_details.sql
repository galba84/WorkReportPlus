DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'descriptiontemplate'
              AND column_name = 'details'
        ) THEN
            ALTER TABLE descriptiontemplate
                ADD COLUMN details varchar(256);
        END IF;
    END
$$;
