CREATE DATABASE IF NOT EXISTS source_db;

USE source_db;

CREATE TABLE job_position (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              title VARCHAR(100) NOT NULL,
                              department VARCHAR(100),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(100) UNIQUE NOT NULL,
                          job_position_id INT,
                          hired_at DATE,
                          FOREIGN KEY (job_position_id) REFERENCES job_position(id)
);

CREATE TABLE salary (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        employee_id INT NOT NULL,
                        amount DECIMAL(10,2) NOT NULL,
                        effective_from DATE NOT NULL,
                        FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'dbz';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
FLUSH PRIVILEGES;
