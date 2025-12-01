CREATE ROLE app_readonly LOGIN PASSWORD 'readonly_password';
CREATE ROLE app_writer LOGIN PASSWORD 'writer_password';
-- CREATE ROLE app_admin LOGIN PASSWORD 'Root@Passord123';

-- Grant privileges
-- Read-only: can only SELECT
GRANT CONNECT ON DATABASE postgres TO app_readonly;
GRANT USAGE ON SCHEMA public TO app_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO app_readonly;

-- Writer: can SELECT, INSERT, UPDATE, DELETE
GRANT CONNECT ON DATABASE postgres TO app_writer;
GRANT USAGE ON SCHEMA public TO app_writer;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_writer;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_writer;

-- Admin: all privileges
GRANT ALL PRIVILEGES ON DATABASE postgres TO app_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO app_admin;

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
