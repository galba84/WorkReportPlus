CREATE TABLE IF NOT EXISTS "rank"
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
);
