-- CHECK DATA COUNT BEFORE INSERT/DELETE
select count(*) from job_position;
select count(*) from employee;
select count(*) from salary;

-- CHECK UPDATED DATA
select * from job_position where department like '%_update%';
select * from employee where email like '%_updated%';
select * from salary where amount = 50000;