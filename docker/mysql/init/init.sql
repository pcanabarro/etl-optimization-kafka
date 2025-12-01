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

DROP PROCEDURE IF EXISTS populate_job_positions $$
CREATE PROCEDURE populate_job_positions(IN total INT)
BEGIN
    DECLARE i INT DEFAULT 1;

    DECLARE titles JSON;
    DECLARE depts JSON;

    SET titles = JSON_ARRAY(
            'Software Engineer','Data Analyst','Frontend Developer','Backend Developer',
            'DevOps Engineer','Network Specialist','Product Manager','UI/UX Designer',
            'QA Engineer','Project Manager','Security Analyst','Cloud Architect'
                 );

    SET depts = JSON_ARRAY(
            'Engineering','Data','HR','Finance','Sales','Marketing',
            'Operations','Support','Product','Legal','Admin','Security'
                );

    WHILE i <= total DO
            INSERT INTO job_position (title, department)
            VALUES (
                       JSON_UNQUOTE(JSON_EXTRACT(titles, CONCAT('$[', FLOOR(RAND() * JSON_LENGTH(titles)), ']'))),
                       JSON_UNQUOTE(JSON_EXTRACT(depts, CONCAT('$[', FLOOR(RAND() * JSON_LENGTH(depts)), ']')))
                   );
            SET i = i + 1;
        END WHILE;
END $$

DROP PROCEDURE IF EXISTS populate_employees $$
CREATE PROCEDURE populate_employees(IN total INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE jp_count INT;
    DECLARE jp_id INT;

    DECLARE firstNames JSON;
    DECLARE lastNames JSON;

    SET firstNames = JSON_ARRAY('Pedro','Lucas','Mariana','Ana','João','Carlos','Beatriz','Sofia','Paulo','Fernando','Julia','Ricardo','Larissa','Gabriel');
    SET lastNames  = JSON_ARRAY('Silva','Souza','Pereira','Oliveira','Santos','Costa','Almeida','Barros','Cardoso','Rezende','Gomes','Mendes');

    SELECT COUNT(*) INTO jp_count FROM job_position;

    WHILE i <= total DO
            -- Pick a random job_position id
            SELECT id INTO jp_id
            FROM job_position
            ORDER BY RAND()
            LIMIT 1;

            INSERT INTO employee (name, email, job_position_id, hired_at)
            VALUES (
                       CONCAT(
                               JSON_UNQUOTE(JSON_EXTRACT(firstNames, CONCAT('$[', FLOOR(RAND() * JSON_LENGTH(firstNames)), ']'))),
                               ' ',
                               JSON_UNQUOTE(JSON_EXTRACT(lastNames, CONCAT('$[', FLOOR(RAND() * JSON_LENGTH(lastNames)), ']')))
                       ),
                       CONCAT('user', UUID_SHORT(), '@example.com'),
                       jp_id,
                       DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND()*2000) DAY)
                   );

            SET i = i + 1;
        END WHILE;
END $$

DROP PROCEDURE IF EXISTS populate_salaries $$
CREATE PROCEDURE populate_salaries(IN total INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE emp_id INT;

    WHILE i <= total DO
            SELECT id INTO emp_id
            FROM employee
            ORDER BY RAND()
            LIMIT 1;

            INSERT INTO salary (employee_id, amount, effective_from)
            VALUES (
                       emp_id,
                       ROUND(2500 + RAND() * 15000, 2),
                       DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND()*1500) DAY)
                   );

            SET i = i + 1;
        END WHILE;
END $$

DELIMITER ;

ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'rootpassword';
FLUSH PRIVILEGES;
