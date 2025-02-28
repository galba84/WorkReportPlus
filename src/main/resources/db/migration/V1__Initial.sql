-- V1__Create_AreaType_Table.sql
CREATE TABLE IF NOT EXISTS AreaType (
                          id VARCHAR(255) PRIMARY KEY,
                          type_name VARCHAR(255)
);

-- V2__Create_Place_Table.sql
CREATE TABLE IF NOT EXISTS Place (
                       id VARCHAR(255) PRIMARY KEY,
                       name VARCHAR(255),
                       area_type_id VARCHAR(255),
                       county VARCHAR(255),
                       district VARCHAR(255),
                       region VARCHAR(255),
                       FOREIGN KEY (area_type_id) REFERENCES AreaType(id)
);

CREATE TABLE IF NOT EXISTS positions (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           position_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS Region (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        region_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS `Group` (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100),
                         region_id INT,
                         FOREIGN KEY (region_id) REFERENCES Region(id)
);

CREATE TABLE IF NOT EXISTS Unit (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      unit_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS Contractor (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            first_name VARCHAR(100),
                            last_name VARCHAR(100),
                            middle_name VARCHAR(100),
                            nick_name VARCHAR(100),
                            gender CHAR(1),
                            birth_date DATE,
                            nationality VARCHAR(100),
                            date_of_arrival_to_unit DATE,
                            contractor_status VARCHAR(100),
                            c_rank VARCHAR(100),
                            position_id INT,
                            unit_id INT,
                            created_by VARCHAR(100) NOT NULL,
                            created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_by VARCHAR(100) NOT NULL,
                            updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            status VARCHAR(100),
                            FOREIGN KEY (position_id) REFERENCES positions(id),
                            FOREIGN KEY (unit_id) REFERENCES Unit(id)
);

CREATE TABLE IF NOT EXISTS GroupReport (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             group_id INT NOT NULL,
                             description TEXT NOT NULL,
                             is_worked BOOLEAN,
                             created_by VARCHAR(100) NOT NULL,
                             created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_by VARCHAR(100) NOT NULL,
                             updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             status VARCHAR(100),
                             extra_data_group_report JSON,
                             FOREIGN KEY (group_id) REFERENCES `Group`(id)
);

CREATE TABLE IF NOT EXISTS RegionReport (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              report_date DATE NOT NULL,
                              region_id INT NOT NULL,
                              region_description TEXT,
                              created_by VARCHAR(100) NOT NULL,
                              created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_by VARCHAR(100) NOT NULL,
                              updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              status VARCHAR(100),
                              extra_data JSON,
                              FOREIGN KEY (region_id) REFERENCES Region(id)
);

CREATE INDEX IF NOT EXISTS idx_contractor_position_id ON Contractor (position_id);
CREATE INDEX IF NOT EXISTS  idx_contractor_unit_id ON Contractor (unit_id);
CREATE INDEX IF NOT EXISTS  idx_group_region_id ON `Group` (region_id);
CREATE INDEX IF NOT EXISTS  idx_groupreport_group_id ON GroupReport (group_id);
CREATE INDEX IF NOT EXISTS  idx_regionreport_region_id ON RegionReport (region_id);
