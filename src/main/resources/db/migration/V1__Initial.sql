CREATE TABLE IF NOT EXISTS AreaType
(
    id        BIGSERIAL PRIMARY KEY, -- ✅ Changed to BIGSERIAL
    type_name TEXT NOT NULL          -- ✅ Changed VARCHAR(255) to TEXT
);

CREATE TABLE IF NOT EXISTS Place
(
    id           BIGSERIAL PRIMARY KEY, -- ✅ Changed to BIGSERIAL
    name         TEXT NOT NULL,         -- ✅ Changed VARCHAR(255) to TEXT
    area_type_id BIGSERIAL NOT NULL,       -- ✅ Foreign key type must match
    county       TEXT NOT NULL,
    district     TEXT NOT NULL,
    region       TEXT NOT NULL,
    FOREIGN KEY (area_type_id) REFERENCES AreaType (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS Positions
(
    id            BIGSERIAL PRIMARY KEY,
    position_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Region
(
    id          BIGSERIAL PRIMARY KEY,
    region_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "Group" -- ✅ Quoted to avoid reserved keyword conflict
(
    id        BIGSERIAL PRIMARY KEY,
    name      TEXT NOT NULL,
    region_id BIGSERIAL NOT NULL,
    FOREIGN KEY (region_id) REFERENCES Region (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS Unit
(
    id        BIGSERIAL PRIMARY KEY,
    unit_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Contractor
(
    id                      BIGSERIAL PRIMARY KEY,
    first_name              TEXT NOT NULL,
    last_name               TEXT NOT NULL,
    middle_name             TEXT NOT NULL DEFAULT '',
    nick_name               TEXT NOT NULL,
    gender                  CHAR(1) NOT NULL CHECK (gender IN ('M', 'F')), -- ✅ Enforce 'M' or 'F'
    birth_date              DATE NOT NULL,
    nationality             TEXT NOT NULL,
    date_of_arrival_to_unit DATE NOT NULL,
    contractor_status       TEXT NOT NULL,
    c_rank                  TEXT NOT NULL,
    position_id             BIGSERIAL NOT NULL,
    unit_id                 BIGSERIAL NOT NULL,
    created_by              TEXT NOT NULL,
    created_on              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by              TEXT NOT NULL,
    updated_on              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status                  BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (position_id) REFERENCES Positions (id) ON DELETE SET NULL,
    FOREIGN KEY (unit_id) REFERENCES Unit (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS GroupReport
(
    id                      BIGSERIAL PRIMARY KEY,
    group_id                BIGINT NOT NULL,
    description             TEXT NOT NULL,
    looses                  TEXT NOT NULL,
    success_reported        TEXT,
    is_worked               BOOLEAN NOT NULL DEFAULT FALSE,
    contractors_ids         TEXT NOT NULL, -- ✅ Consider ARRAY if storing multiple values
    place_ids               TEXT NOT NULL, -- ✅ Consider ARRAY if storing multiple values
    status                  BOOLEAN NOT NULL DEFAULT TRUE,
    report_date             DATE NOT NULL,
    extra_data_group_report JSONB, -- ✅ PostgreSQL prefers JSONB over JSON
    created_by              TEXT NOT NULL,
    created_on              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by              TEXT NOT NULL,
    updated_on              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES "Group" (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS RegionReport
(
    id                   BIGSERIAL PRIMARY KEY,
    report_date          DATE NOT NULL,
    region_id            BIGSERIAL NOT NULL,
    region_description   TEXT NOT NULL,
    arrived_contractors  JSONB, -- ✅ Changed JSON → JSONB
    departed_contractors JSONB, -- ✅ Changed JSON → JSONB
    created_by           TEXT NOT NULL,
    created_on           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by           TEXT NOT NULL,
    updated_on           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status               BOOLEAN NOT NULL DEFAULT TRUE,
    extra_data           JSONB,
    FOREIGN KEY (region_id) REFERENCES Region (id) ON DELETE CASCADE
    );

-- ✅ Indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_contractor_position_id ON Contractor (position_id);
CREATE INDEX IF NOT EXISTS idx_contractor_unit_id ON Contractor (unit_id);
CREATE INDEX IF NOT EXISTS idx_group_region_id ON "Group" (region_id);
CREATE INDEX IF NOT EXISTS idx_groupreport_group_id ON GroupReport (group_id);
CREATE INDEX IF NOT EXISTS idx_regionreport_region_id ON RegionReport (region_id);
