package com.pcanabarro.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbc;

    public DatabaseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // return true if slow query
    public boolean execute(String sql) {
        try {
            long start = System.nanoTime();
            jdbc.execute(sql);
            long ns = System.nanoTime() - start;
            return ns > 10_000_000; // > 10 ms
        } catch (Exception e) {
            throw e;
        }
    }
}
