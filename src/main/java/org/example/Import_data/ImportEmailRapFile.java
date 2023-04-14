package org.example.Import_data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class ImportEmailRapFile {
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);
    public static boolean importData(String data) throws SQLException {
        boolean result = false;
        int resultInsert = 0;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isReading = false;
        String line;
        Connection connection = null;
        PreparedStatement ps = null;
        StringBuilder sb = new StringBuilder();
        String importSql = "INSERT INTO email.rap_email_attachment_data" +
                "(rap_file,tap_file,date_received,tap_charge,record_type,error_charge,error_code,error_description,create_at)" +
                "values(?, ?, ?, ?, ?, ?, ?, ?,NOW())";
        try {
            connection = GetConnection.connect();
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

            resultInsert =  ps.executeUpdate();
            if(resultInsert == 1){
                result = true;
            }
            connection.commit();
            reader.close();
        } catch (SQLException | IOException e) {
            logger.error("import data fail" + e);
        } finally {
            connection.close();
            ps.close();
        }
        return result;
    }
}
