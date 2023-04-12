package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImportEmailMissingConfig {
    public static void importData(String data) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection.connect();
            connection.setAutoCommit(false);
            BufferedReader reader = new BufferedReader(new StringReader(data.trim()));
            String line;
            String importSql = "insert into email.missing_config (tap_name,record,sdr_amount,create_at)" +
                    " values(?,?,?,now())" ;
            ps = connection.prepareStatement(importSql);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if(!parts[0].equals("Tapname")){
                    ps.setString(1,parts[0]);
                    ps.setString(2,parts[1]);
                    ps.setString(3,parts[2]);
                    ps.executeUpdate();
                }
            }
            connection.commit();
            connection.close();
            reader.close();

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
