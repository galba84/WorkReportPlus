CREATE TABLE IF NOT EXISTS regionReportTemplate
(
    id        UUID        DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    regionId  UUID                                  NOT NULL REFERENCES region (id) ON DELETE CASCADE,
    preamble  TEXT        DEFAULT ''                NOT NULL,
    signature TEXT        DEFAULT ''                NOT NULL,
    createdBy TEXT                                  NOT NULL,
    updatedBy TEXT                                  NOT NULL,
    createdOn TIMESTAMPTZ DEFAULT now()             NOT NULL,
    updatedOn TIMESTAMPTZ DEFAULT now()             NOT NULL
);
