package org.example.Import_data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImportEmailRapFile {
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);
    public static boolean importData(String data) throws Exception {
        Map<String, String> map = new HashMap<>();
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isReading = false;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
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
            String errorDescription = String.join(" ", Arrays.copyOfRange(fields, 7, fields.length));
            map.put("rap_file",fields[0]);
            map.put("tap_file",fields[1]);
            map.put("date_received",fields[2]);
            map.put("tap_charge",fields[3]);
            map.put("record_type",fields[4]);
            map.put("error_charge",fields[5]);
            map.put("error_code",fields[6]);
            map.put("error_description",errorDescription);
            result = InsertData(map);
            reader.close();
        } catch (Exception e) {
            logger.error("import data fail" + e);
        }
        return result;
    }
    public static boolean InsertData(Map map) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String getDataImportConfig = "select fields from email.data_import_config where type = 'RAP'";
            ps = connection.prepareStatement(getDataImportConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                String fields = rs.getString("fields");
                String[] fieldNames = fields.split(",");
                String insertQuery = "INSERT INTO email.rap_email_attachment_data (";
                for (int i = 0; i < fieldNames.length; i++) {
                    insertQuery += fieldNames[i];
                    if (i < fieldNames.length - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ") VALUES (";
                for (int i = 0; i < fieldNames.length; i++) {
                    insertQuery += "?";
                    if (i < fieldNames.length - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ")";
                ps = connection.prepareStatement(insertQuery);
                for (int i = 0; i < fieldNames.length; i++) {
                    ps.setString(i + 1, String.valueOf(map.get(fieldNames[i])));
                }
                resultInsert = ps.executeUpdate();
                if (resultInsert == 1) {
                    result = true;
                }
            }
            connection.commit();
        } catch (Exception e){
            logger.error(e);
        } finally {
            connection.close();
            rs.close();
            ps.close();
        }
        return result;
    }
}
