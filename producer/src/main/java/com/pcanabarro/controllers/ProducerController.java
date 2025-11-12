package com.pcanabarro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/produce")
    public String produceCreateMessages() {
        try {
            jdbcTemplate.execute("CALL populate_job_positions();");
            jdbcTemplate.execute("CALL populate_employees();");
            jdbcTemplate.execute("CALL populate_salaries();");

            return ResponseEntity.ok( "Create Messages created!").toString();
        } catch (Exception e) {
            return ResponseEntity.ok( e.getMessage()).toString();
        }
    }

    @PatchMapping("/produce")
    public String produceUpdateMessages() {
        try {
            jdbcTemplate.execute("UPDATE job_position SET department = department || '_updated';");
            jdbcTemplate.execute("UPDATE employee SET email = email + 'a';");
            jdbcTemplate.execute("UPDATE salary SET amount = amount + 1;");
            return ResponseEntity.ok( "Update Messages created!").toString();
        } catch (Exception e) {
            return ResponseEntity.ok( e.getMessage()).toString();
        }
    }
}
