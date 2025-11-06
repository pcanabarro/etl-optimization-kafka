-- Create job positions
CREATE TABLE job_position (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create employees
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    job_position_id INT,
    hired_at DATE,
    FOREIGN KEY (job_position_id) REFERENCES job_position(id)
);

-- Create salaries
CREATE TABLE salary (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    effective_from DATE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);
