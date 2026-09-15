CREATE TABLE IF NOT EXISTS audit_log (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         user_id UUID REFERENCES users(id) ON DELETE SET NULL,
                                         action VARCHAR(100) NOT NULL,
                                         service_id VARCHAR(255),
                                         entity_id VARCHAR(255),
                                         ip_address VARCHAR(45),
                                         timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                         details TEXT
);
