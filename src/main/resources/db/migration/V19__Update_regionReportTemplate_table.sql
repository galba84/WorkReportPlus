-- Add 'file_format' column if it doesn't exist
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'regionreporttemplate'
              AND column_name = 'file_format'
        ) THEN
            ALTER TABLE regionreporttemplate ADD COLUMN file_format text;
        END IF;
    END$$;

-- Add 'content' column if it doesn't exist
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'regionreporttemplate'
              AND column_name = 'content'
        ) THEN
            ALTER TABLE regionreporttemplate ADD COLUMN content text;
        END IF;
    END$$;

-- Add 'variables' column if it doesn't exist
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'regionreporttemplate'
              AND column_name = 'variables'
        ) THEN
            ALTER TABLE regionreporttemplate ADD COLUMN variables text[];
        END IF;
    END$$;

-- Ensure NOT NULL constraints
ALTER TABLE regionreporttemplate
    ALTER COLUMN file_format SET NOT NULL,
    ALTER COLUMN content SET NOT NULL,
    ALTER COLUMN variables SET NOT NULL;

-- Add check constraint if it doesn't exist
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'documenttemplate_file_format_check'
        ) THEN
            ALTER TABLE regionreporttemplate
                ADD CONSTRAINT documenttemplate_file_format_check
                    CHECK (file_format = ANY (ARRAY['RTF'::text, 'DOCX'::text, 'PDF'::text]));
    END IF;
END$$;
