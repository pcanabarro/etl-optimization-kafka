package com.pcanabarro.services;

import com.pcanabarro.entities.Employee;
import com.pcanabarro.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseService {
    private final EmployeeRepository employeeRepository;

    public DatabaseService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> fetchEmployeeRecords() {
        return employeeRepository.findAll();
    }
}
