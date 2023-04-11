package org.example;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class ConnectDb {
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
            e.getMessage();
        }
    }
    public static Connection connect() throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to Database.");
        } catch (SQLException e) {
            System.out.println("Cannot connect to Database.");
            e.printStackTrace();
        }
        return connection;
    }
    public static void main(String[] args) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = connect();
            connection.setAutoCommit(false);
            String sql = "select * from luck.user";
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString("email"));
            }
            // Your SQL queries here
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                    System.out.println("Disconnected from Database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
