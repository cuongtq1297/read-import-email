package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImportEmailHur {
    public static void importData(String data) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection.connect();
            connection.setAutoCommit(false);
            String sql = "select * from luck.user";
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString("email"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
