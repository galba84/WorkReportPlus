-- ✅ Table: AreaType
CREATE TABLE IF NOT EXISTS AreaType
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Changed to UUID
    type_name TEXT NOT NULL     -- Changed VARCHAR(255) to TEXT
);

-- ✅ Table: Place
CREATE TABLE IF NOT EXISTS Place (
                       id         uuid        DEFAULT gen_random_uuid() PRIMARY KEY,
                       name       text        NOT NULL,
                       county     text        NOT NULL,
                       district   text        NOT NULL,
                       region     text        NOT NULL,
                       area_type  text        NOT NULL,
                       region_id   uuid        REFERENCES Region(id) ON DELETE SET NULL,
                       coeficient text
);

-- ✅ Table: Positions
CREATE TABLE IF NOT EXISTS Positions
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    position_name TEXT NOT NULL
);

-- ✅ Table: Region
CREATE TABLE IF NOT EXISTS Region
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    region_name TEXT NOT NULL UNIQUE -- Added UNIQUE constraint
);

-- ✅ Table: Group (Quoted to avoid reserved keyword conflict)
CREATE TABLE IF NOT EXISTS "Group"
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name      TEXT NOT NULL UNIQUE, -- Ensured uniqueness for Group name
    region_id UUID NOT NULL,
    FOREIGN KEY (region_id) REFERENCES Region (id) ON DELETE CASCADE
);

-- ✅ Table: Unit
CREATE TABLE IF NOT EXISTS Unit
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_name TEXT NOT NULL
);

-- ✅ Table: Contractor
CREATE TABLE IF NOT EXISTS Contractor
(
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name              TEXT    NOT NULL,
    last_name               TEXT    NOT NULL,
    middle_name             TEXT    NOT NULL DEFAULT '',
    nick_name               TEXT    NOT NULL,
    gender                  CHAR(1) NOT NULL CHECK (gender IN ('M', 'F')), -- Enforce 'M' or 'F'
    birth_date              DATE    NOT NULL,
    nationality             TEXT    NOT NULL,
    date_of_arrival_to_unit DATE    NOT NULL,
    contractor_status       TEXT    NOT NULL,
    c_rank                  TEXT    NOT NULL,
    position_id             UUID    NOT NULL,
    unit_id                 UUID    NOT NULL,
    created_by              TEXT    NOT NULL,
    created_on              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_by              TEXT    NOT NULL,
    updated_on              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    status                  BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (position_id) REFERENCES Positions (id) ON DELETE SET NULL,
    FOREIGN KEY (unit_id) REFERENCES Unit (id) ON DELETE CASCADE
);


-- ✅ Table: RegionReport
CREATE TABLE IF NOT EXISTS RegionReport
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_date          DATE    NOT NULL,
    region_id            UUID    NOT NULL,
    region_description   TEXT    NOT NULL,
    arrived_contractors  UUID[], -- Changed JSON → JSONB
    departed_contractors UUID[], -- Changed JSON → JSONB
    created_by           TEXT    NOT NULL,
    created_on           TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_by           TEXT    NOT NULL,
    updated_on           TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    status               BOOLEAN NOT NULL DEFAULT TRUE,
    extra_data           JSONB,
    FOREIGN KEY (region_id) REFERENCES Region (id) ON DELETE CASCADE
);

-- ✅ Table: GroupReport
CREATE TABLE IF NOT EXISTS GroupReport
(
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id                UUID    NOT NULL,
    region_report_id        UUID    NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    description             TEXT    NOT NULL,
    looses                  UUID[],
    success_reported        TEXT,
    is_worked               BOOLEAN NOT NULL DEFAULT FALSE,
    contractors_ids         UUID[]  NOT NULL, -- Changed TEXT to UUID ARRAY
    place_ids               UUID[]  NOT NULL, -- Changed TEXT to UUID ARRAY
    status                  BOOLEAN NOT NULL DEFAULT TRUE,
    report_date             DATE    NOT NULL,
    extra_data_group_report JSONB,            -- PostgreSQL prefers JSONB over JSON
    created_by              TEXT    NOT NULL,
    created_on              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_by              TEXT    NOT NULL,
    updated_on              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES "Group" (id) ON DELETE CASCADE,
    FOREIGN KEY (region_report_id) REFERENCES RegionReport (id) ON DELETE CASCADE
);



-- ✅ Indexes for Faster Queries
CREATE INDEX IF NOT EXISTS idx_contractor_position_id ON Contractor (position_id);
CREATE INDEX IF NOT EXISTS idx_contractor_unit_id ON Contractor (unit_id);
CREATE INDEX IF NOT EXISTS idx_group_region_id ON "Group" (region_id);
CREATE INDEX IF NOT EXISTS idx_groupreport_group_id ON GroupReport (group_id);
CREATE INDEX IF NOT EXISTS idx_regionreport_region_id ON RegionReport (region_id);

-- ✅ Indexes for Faster Filtering
CREATE INDEX IF NOT EXISTS idx_regionreport_report_date ON RegionReport (report_date, region_id);
CREATE INDEX IF NOT EXISTS idx_groupreport_report_date ON GroupReport (report_date);

-- ✅ Indexes for Faster JSONB Queries
CREATE INDEX IF NOT EXISTS idx_regionreport_extra_data ON RegionReport USING GIN (extra_data);
CREATE INDEX IF NOT EXISTS idx_groupreport_extra_data ON GroupReport USING GIN (extra_data_group_report);

-- ✅ Indexes for Filtering by Active Status
CREATE INDEX IF NOT EXISTS idx_contractor_status ON Contractor (status);
CREATE INDEX IF NOT EXISTS idx_groupreport_status ON GroupReport (status);
CREATE INDEX IF NOT EXISTS idx_regionreport_status ON RegionReport (status);
