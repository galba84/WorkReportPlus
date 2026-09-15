CREATE TABLE IF NOT EXISTS DocumentTemplate
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name TEXT NOT NULL UNIQUE, -- Unique name for the template
    file_format   TEXT NOT NULL CHECK (file_format IN ('RTF', 'DOCX', 'PDF')), -- Supported formats
    content       TEXT NOT NULL, -- Stores the raw template content as text
    variables     TEXT[] NOT NULL, -- List of template variables
    parent_id     UUID NULL, -- Parent template (NULL if it is a parent itself)
    created_by    TEXT NOT NULL, -- Creator username
    created_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_by    TEXT NOT NULL, -- Last modified by
    updated_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Last updated timestamp
    status        TEXT NOT NULL CHECK (status IN ('active', 'deleted')), -- Template status
    FOREIGN KEY (parent_id) REFERENCES DocumentTemplate (id) ON DELETE SET NULL
);

-- ✅ Indexes for Faster Queries
CREATE INDEX IF NOT EXISTS idx_documenttemplate_status ON DocumentTemplate (status);
CREATE INDEX IF NOT EXISTS idx_documenttemplate_template_name ON DocumentTemplate (template_name);
CREATE INDEX IF NOT EXISTS idx_documenttemplate_file_format ON DocumentTemplate (file_format);
CREATE INDEX IF NOT EXISTS idx_documenttemplate_parent ON DocumentTemplate (parent_id);


INSERT INTO DocumentTemplate (id, template_name, file_format, content, variables, created_by, updated_by, status, parent_id)
VALUES (
           '550e8400-e29b-41d4-a716-446655440000', -- Manually set a UUID (for linking)
           'Region Report',
           'RTF',
           'Бойове донесення регіону з {{regionName}} регіону, за {{reportDate}}.\n\n' ||
           '{{extraData}}\n\n' ||
           '{{arrivedContractors}}\n\n' ||
           '{{departedContractors}}\n\n' ||
           '{{regionReportDescription}}\n\n' ||
           '{{groupReports}}\n',
           ARRAY['regionName', 'reportDate', 'extraData', 'arrivedContractors', 'departedContractors', 'regionReportDescription', 'groupReports'],
           'admin',
           'admin',
           'active',
           NULL -- Parent report (no parent)
       );


INSERT INTO DocumentTemplate (template_name, file_format, content, variables, created_by, updated_by, status, parent_id)
VALUES (
           'Group Report',
           'RTF',
           'Бойове донесення групи {{groupName}}, за {{reportDate}}.\n\n' ||
           'На виконання бойового розпорядження {{order}}\n' ||
           'група у складі:\n\n' ||
           '{{contractors}}\n\n' ||
           'здійснила:\n\n' ||
           '{{groupReportDescription}}\n\n' ||
           'на наступних напрямках:\n\n' ||
           '{{places}}\n\n' ||
           'група понесла втрати:\n\n' ||
           '{{looses}}\n\n' ||
           'група досягла успіху:\n\n' ||
           '{{success}}\n\n' ||
           '{{extraFields}}\n',
           ARRAY['groupName', 'reportDate', 'order', 'contractors', 'groupReportDescription', 'places', 'looses', 'success', 'extraFields'],
           'admin',
           'admin',
           'active',
           '550e8400-e29b-41d4-a716-446655440000' -- Parent ID is Region Report
       );

