package com.pcanabarro.repositories;

import com.pcanabarro.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Custom queries

    @Query("SELECT e FROM Employee e WHERE e.department = :department")
    java.util.List<Employee> findByDepartment(@Param("department") String department);
}
