package org.example.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.process.Process;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class getConnection {
    private static final Logger logger = LogManager.getLogger(getConnection.class);
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    public static final String CONFIG_FILE_PATH =  "config/database.cfg";
    static {
        Properties properties = new Properties();
        FileInputStream propsFile = null;
        try{
            propsFile = new FileInputStream(CONFIG_FILE_PATH);
            properties.load(propsFile);
            URL = properties.getProperty("URL");
            USER = properties.getProperty("USER");
            PASSWORD = properties.getProperty("PASSWORD");
        } catch (Exception e){
            logger.error("get database information fail" + e);
        }
    }
    public static Connection connect() throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            logger.error("connect database fail" + e);
        }
        return connection;
    }
}
