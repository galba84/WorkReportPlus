CREATE TABLE IF NOT EXISTS employee (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          first_name VARCHAR(50) NOT NULL,
                          second_name VARCHAR(50),
                          last_name VARCHAR(50) NOT NULL,
                          tax_code VARCHAR(20) UNIQUE NOT NULL,
                          nick_name VARCHAR(50),
                          position_id INT NOT NULL,
                          position_number VARCHAR(50) NOT NULL,
                          rank_id INT NOT NULL,
                          date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by VARCHAR(50) NOT NULL,
                          date_changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          changed_by VARCHAR(50) NOT NULL
);



-- Create Positions Table
CREATE TABLE IF NOT EXISTS positions (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           position_name VARCHAR(100) NOT NULL
);

-- Create Ranks Table
CREATE TABLE IF NOT EXISTS ranks (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       rank_name VARCHAR(100) NOT NULL
);

-- Add Foreign Keys
ALTER TABLE employee ADD CONSTRAINT fk_position FOREIGN KEY (position_id) REFERENCES positions(id);
ALTER TABLE employee ADD CONSTRAINT fk_rank FOREIGN KEY (rank_id) REFERENCES ranks(id);

-- Insert Default Data (Optional)
INSERT INTO positions (position_name) VALUES ('Manager'), ('Engineer'), ('Technician');
INSERT INTO ranks (rank_name) VALUES ('Senior'), ('Junior'), ('Trainee');
