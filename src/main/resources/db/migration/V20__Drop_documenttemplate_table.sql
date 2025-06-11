DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name = 'documenttemplate'
        ) THEN
            DROP TABLE documenttemplate CASCADE;
        END IF;
    END$$;
