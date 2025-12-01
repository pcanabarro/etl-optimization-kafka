package com.pcanabarro.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class Database {
    private static Connection connection = null;

    static {
        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }

            prop.load(input);

            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");
            String driver = prop.getProperty("db.driver");

            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);

        } catch (IOException | ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error connecting to the database: " + ex.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void executePostgresStatement(String sql) {
        if (sql == null || sql.isEmpty()) {
            System.err.println("SQL statement is null or empty.");
            return;
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
//            System.out.println("Executed: " + sql);
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
        }
    }
}
