use source_db;
CREATE TABLE job_position (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              title VARCHAR(100) NOT NULL,
                              department VARCHAR(100),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(100) NOT NULL,
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

DELIMITER $$

-- Populate job_position
DROP PROCEDURE IF EXISTS populate_job_positions $$
CREATE PROCEDURE populate_job_positions()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE departments JSON;
    SET departments = JSON_ARRAY('Engineering','HR','Finance','Sales','Marketing','Operations','IT Support','Product','Legal','Admin');

    WHILE i <= 100 DO
            INSERT INTO job_position (title, department)
            VALUES (
                       CONCAT('Position_', i),
                       JSON_UNQUOTE(JSON_EXTRACT(departments, CONCAT('$[', FLOOR(RAND() * JSON_LENGTH(departments)), ']')))
                   );
            SET i = i + 1;
        END WHILE;
END $$

-- Populate employee
DROP PROCEDURE IF EXISTS populate_employees $$
CREATE PROCEDURE populate_employees()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE rand_job INT;

    WHILE i <= 100 DO
            SET rand_job = FLOOR(1 + (RAND() * 100)); -- random job_position_id between 1 and 100
            INSERT INTO employee (name, email, job_position_id, hired_at)
            VALUES (
                       CONCAT('Employee_', i),
                       CONCAT('employee', i, '@example.com'),
                       rand_job,
                       DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 2000) DAY)
                   );
            SET i = i + 1;
        END WHILE;
END $$

-- Populate salary
DROP PROCEDURE IF EXISTS populate_salaries $$
CREATE PROCEDURE populate_salaries()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE rand_emp INT;
    DECLARE rand_salary DECIMAL(10,2);

    WHILE i <= 100 DO
            SET rand_emp = FLOOR(1 + (RAND() * 100)); -- random employee_id
            SET rand_salary = ROUND(30000 + (RAND() * 70000), 2); -- between 30k–100k

            INSERT INTO salary (employee_id, amount, effective_from)
            VALUES (
                       rand_emp,
                       rand_salary,
                       DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 1000) DAY)
                   );
            SET i = i + 1;
        END WHILE;
END $$

DELIMITER ;

ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'rootpassword';
FLUSH PRIVILEGES;
