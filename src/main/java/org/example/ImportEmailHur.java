package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ImportEmailHur {
    public static void importData(String data) {
        BufferedReader reader = new BufferedReader(new StringReader(data));
        String line;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection.connect();
            connection.setAutoCommit(false);
            String importSql = "INSERT INTO email.hur" +
                    "(sender, recipient, sequence_no, threshold, date_time_of_analysis, date_time_of_report_creation, BEGINNING_OF_THE_OBSERVATION_PERIOD, END_OF_THE_OBSERVATION_PERIOD,create_at)\n" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?,NOW())";
            ps = connection.prepareStatement(importSql);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("H")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    ps.setString(1, fields.get(1));
                    ps.setString(2, fields.get(2));
                    ps.setString(3, fields.get(3));
                    ps.setString(4, fields.get(4));
                    ps.setString(5, fields.get(5));
                    ps.setString(6, fields.get(6));
                }
                if(line.startsWith("N")){
                    List<String> fields = Arrays.asList(line.split(","));
                    ps.setString(7, fields.get(1));
                    ps.setString(8, fields.get(2));
                }
            }
            ps.executeUpdate();
            connection.commit();
            connection.close();
            ps.close();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
