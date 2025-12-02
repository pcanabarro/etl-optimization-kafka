package com.pcanabarro.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbc;

    public DatabaseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void execute(String sql) {
        if (sql == null) return;
        jdbc.update(sql);
    }
}
