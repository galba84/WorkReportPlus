-- Create users table with role as 'ADMIN', 'USER', 'POWER_USER'
CREATE TABLE IF NOT EXISTS users (
                       id        UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       nickname  VARCHAR(50) NOT NULL,
                       email     VARCHAR(255) NOT NULL UNIQUE,
                       role      VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER', 'POWER_USER'))
);
