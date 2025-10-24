package com.pcanabarro.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

    public static Connection getConnection() {
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
            return DriverManager.getConnection(url, user, password);
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            System.out.println("Error connecting to the database: " + ex.getMessage());
            return null;
        }
    }
}
