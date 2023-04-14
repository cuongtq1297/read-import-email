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

public class ImportEmailDfd {
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);
    public static boolean importData(String data) throws SQLException {
        boolean result = false;
        int resultInsert = 0;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isReading = false;
        String line;
        String line1;
        Connection connection = null;
        PreparedStatement ps = null;
        StringBuilder sb = new StringBuilder();
        String importSql = "INSERT INTO email.dfd_email_attachment_data" +
                "(hpmn,seqnr,cut_off_time,first_call_time,no_records,tax,charge,cur,received_time,validated_time,cnv,sent_time,rj,qr,sn,create_at)" +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,NOW())";
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(importSql);
            while ((line = reader.readLine()) != null) {
                if (line.contains("----- -----")) {
                    isReading = true;
                } else if (line.contains("Total") && isReading) {
                    break;
                } else if (isReading) {
                    sb.append(line.trim() + "\n");
                }
            }
            BufferedReader reader1 = new BufferedReader(new StringReader(sb.toString().trim()));
            while ((line1 = reader1.readLine()) != null) {
                String[] fields = line1.split("\\s+");
                String hpmn = fields[0];
                String seqnr = fields[1];
                String cutOffTime = String.join(" ", fields[2], fields[3]);
                String firstCallTime = String.join(" ", fields[4], fields[5]);
                String noOfRecords = fields[6];
                String tax = fields[7];
                String charge = fields[8];
                String cur = fields[9];
                String receivedTime = String.join(" ", fields[10], fields[11]);
                String validatedTime = String.join(" ", fields[12], fields[13]);
                String cnv = fields[14];
                String sentTime = String.join(" ", fields[15], fields[16]);
                String rj = fields[17];
                String qr = fields[18];
                String sn = fields[19];

                ps.setString(1, hpmn);
                ps.setString(2, seqnr);
                ps.setString(3, cutOffTime);
                ps.setString(4, firstCallTime);
                ps.setString(5, noOfRecords);
                ps.setString(6, tax);
                ps.setString(7, charge);
                ps.setString(8, cur);
                ps.setString(9, receivedTime);
                ps.setString(10, validatedTime);
                ps.setString(11, cnv);
                ps.setString(12, sentTime);
                ps.setString(13, rj);
                ps.setString(14, qr);
                ps.setString(15, sn);
                resultInsert =  ps.executeUpdate();
                if(resultInsert == 1){
                    result = true;
                }
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
