package org.example.Database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;

public class GetConnectionToImport {
    private static final Logger logger = LogManager.getLogger(GetConnectionToImport.class);
    public static Connection connect(String url, String userName, String password) throws Exception {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            logger.error("connect database to import fail \n" + e);
        }
        return connection;
    }
}
