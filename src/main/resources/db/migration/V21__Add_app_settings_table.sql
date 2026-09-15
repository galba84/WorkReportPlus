-- create table for application settings
CREATE TABLE IF NOT EXISTS app_settings (
                                            setting_key    TEXT            PRIMARY KEY,
                                            setting_value  TEXT,
                                            setting_data   JSONB           NOT NULL DEFAULT '{}'::jsonb,
                                            format         TEXT  NOT NULL DEFAULT 'string',
                                            description    TEXT,
                                            created_on     TIMESTAMPTZ      NOT NULL DEFAULT now(),
                                            updated_on     TIMESTAMPTZ      NOT NULL DEFAULT now()
);

-- optional: GIN index if you query inside the JSONB
CREATE INDEX IF NOT EXISTS idx_app_settings_data_gin
    ON app_settings
        USING GIN (setting_data);
