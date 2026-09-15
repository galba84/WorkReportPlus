CREATE TABLE IF NOT EXISTS DescriptionTemplate
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id   UUID UNIQUE,                                  -- Matches Group.id type
    content    TEXT NOT NULL,
    created_by TEXT NOT NULL,
    created_on TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT NOT NULL,
    updated_on TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES "Group" (id) ON DELETE SET NULL
);
