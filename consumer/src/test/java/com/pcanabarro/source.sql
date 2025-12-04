-- CHECK DATA COUNT BEFORE INSERT/DELETE
select count(*) from job_position where department like '%_update%';
select count(*) from employee where email like '%_updated%';
select count(*) from salary where amount = 50000;

-- CHECK UPDATED DATA
select * from job_position where department like '%_updated%';
select * from employee where email like '%_updated%';
select * from salary where amount = 50000;

-- UPDATE TEST DATA
UPDATE job_position jp
    JOIN (
    SELECT id
    FROM job_position
    where department not like '%_update%'
    ORDER BY RAND()
    LIMIT 10000
    ) r USING (id)
SET jp.department = CONCAT(jp.department, '_updated');

UPDATE employee e
    JOIN (
    SELECT id
    FROM employee
    where email not like '%_updated%'
    ORDER BY RAND()
    LIMIT 10000
    ) r USING (id)
SET e.email = CONCAT(e.email, '_updated');

UPDATE salary s
    JOIN (
    SELECT id
    FROM salary
    where amount != 50000
    ORDER BY RAND()
    LIMIT 10000
    ) r USING (id)
SET s.amount = 50000;

-- DELETE TEST DATA
DELETE FROM job_position WHERE department LIKE '%_update%';
DELETE FROM employee WHERE email LIKE '%_updated%';
DELETE FROM salary WHERE amount = 50000;
