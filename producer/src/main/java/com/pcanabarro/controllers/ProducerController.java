package com.pcanabarro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProducerController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/produce/{value}")
    public String produceCreateMessages(@PathVariable("value") int value) {
        try {
            String resetUrl = "http://localhost:8080/metrics/reset-flow-counter";
            restTemplate.postForObject(resetUrl, null, String.class);

            jdbcTemplate.update("CALL populate_job_positions(?);", value);
            jdbcTemplate.update("CALL populate_employees(?);", value);
            jdbcTemplate.update("CALL populate_salaries(?);", value);

            return ResponseEntity.ok("Create Messages created!").toString();
        } catch (Exception e) {
            return ResponseEntity.ok(e.getMessage()).toString();
        }
    }

    @PatchMapping("/produce")
    public String produceUpdateMessages() {
        try {
            jdbcTemplate.execute("UPDATE job_position SET department = department || '_updated';");
            jdbcTemplate.execute("UPDATE employee SET email = email || 'a';");
            jdbcTemplate.execute("UPDATE salary SET amount = amount + 1;");

            return ResponseEntity.ok("Update Messages created!").toString();

        } catch (Exception e) {
            return ResponseEntity.ok(e.getMessage()).toString();
        }
    }
}
