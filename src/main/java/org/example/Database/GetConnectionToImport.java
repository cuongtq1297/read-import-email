package org.example.Database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GetConnectionToImport {
    //todo: lấy dữ liệu connection từ bảng config database
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

    public static Connection connectNew(String typeName) throws Exception {
        Connection connection = null;
        Connection connectionIn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connectionIn = GetConnection.connect();
            String sql = "Select * from email.email_database_connection where type_name = ? and status = '0'";
            stmt = connectionIn.prepareStatement(sql);
            stmt.setString(1, typeName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String tns = rs.getString("tns");
                String username = rs.getString("username");
                String password = rs.getString("password");
                connection = DriverManager.getConnection(tns, username, password);
            }
        } catch (Exception e) {
            logger.error("connect database to import fail \n" + e);
        }
        return connection;
    }
}
