CREATE TABLE reports (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         date DATE NOT NULL,
                         region VARCHAR(50) NOT NULL,
                         description TEXT NOT NULL
);
