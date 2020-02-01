package com.ken.kenutils.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	
	public static Connection getConnection(String databaseName) {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:./"+databaseName;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return conn;
    }

}
