package com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    String driver = "org.postgresql.Driver";
    String connectionString = "jdbc:postgresql://localhost:5432/Application";
    String username = "postgres";
    String password = "Sanjay@1206";
    private Connection connection;

    private static Connect connect = null;

    private Connect() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(connectionString, username, password);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Connection getConnection() {
        try {
          if (connection.isClosed()) {
            connection = DriverManager.getConnection(connectionString, username, password);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return connection;
      }

    public static Connect getInstance() {
        if (connect == null) {
            connect = new Connect();
        }
        return connect;
    }

}
