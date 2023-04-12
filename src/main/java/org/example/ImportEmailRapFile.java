package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class ImportEmailRapFile {
    public static void importData(String data){
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isReading = false;
        String line;
        Connection connection = null;
        PreparedStatement ps = null;
        StringBuilder sb = new StringBuilder();
        String importSql = "INSERT INTO email.rap_file(rap_file,tap_file,date_received,tap_charge,record_type,error_charge,error_code,error_description,create_at)" +
                "values(?, ?, ?, ?, ?, ?, ?, ?,NOW())";
        try {
            connection = getConnection.connect();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(importSql);
            while ((line = reader.readLine()) != null){
                if(line.contains("Received")){
                    isReading = true;
                } else if (line.contains("RAP OUT") && isReading) {
                    break;
                } else if (isReading) {
                    sb.append(line.trim());
                }
            }
            String[] fields = sb.toString().split("\\s+");
            String rapFile = fields[0];
            String tapFile = fields[1];
            String dateReceived = fields[2];
            String tapCharge = fields[3];
            String recordType = fields[4];
            String errorCharge = fields[5];
            String errorCode = fields[6];
            String errorDescription = String.join(" ", Arrays.copyOfRange(fields, 7, fields.length));
            ps.setString(1,rapFile);
            ps.setString(2,tapFile);
            ps.setString(3,dateReceived);
            ps.setString(4,tapCharge);
            ps.setString(5,recordType);
            ps.setString(6,errorCharge);
            ps.setString(7,errorCode);
            ps.setString(8,errorDescription);

            ps.executeUpdate();
            connection.commit();
            connection.close();
            reader.close();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
