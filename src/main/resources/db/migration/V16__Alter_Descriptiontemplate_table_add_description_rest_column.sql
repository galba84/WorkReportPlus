-- V__add_content_rest_column.sql

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'descriptiontemplate'
              AND column_name = 'content_rest'
        ) THEN
            ALTER TABLE descriptiontemplate
                ADD COLUMN content_rest text;
        END IF;
    END $$;

