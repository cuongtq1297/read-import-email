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
import java.util.List;
import java.util.Map;

public class ImportEmailHur {
    private static final Logger logger = LogManager.getLogger(ImportEmailHur.class);

    public static boolean importData(String data) throws Exception {
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        String line;
        try {
            Map<String, String> map1 = new HashMap<>();
            Map<String, String> map2 = new HashMap<>();
            Map<String, String> mapMerge = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("H")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    map1.put("sender", fields.get(1));
                    map1.put("recipient", fields.get(2));
                    map1.put("sequence_no", fields.get(3));
                    map1.put("threshold", fields.get(4));
                    map1.put("date_time_of_analysis", fields.get(5));
                    map1.put("date_time_of_report_creation", fields.get(6));
                } else if (line.startsWith("N")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    map2.put("BEGINNING_OF_THE_OBSERVATION_PERIOD", fields.get(1));
                    map2.put("END_OF_THE_OBSERVATION_PERIOD", fields.get(2));
                }
            }
            mapMerge = mergeMaps(map1, map2);
            result = InsertData(mapMerge);
        } catch (Exception e) {
            logger.error("import data fail" + e);
        }
        return result;
    }

    public static Map<String, String> mergeMaps(Map<String, String>... maps) {
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> map : maps) {
            result.putAll(map);
        }
        return result;
    }

    public static boolean InsertData(Map mapMerge) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String getDataImportConfig = "select fields from email.data_import_config where type = 'HUR'";
            ps = connection.prepareStatement(getDataImportConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                String fields = rs.getString("fields");
                String[] fieldNames = fields.split(",");
                String insertQuery = "INSERT INTO email.hur_email_attachment_data (";
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
                    ps.setString(i + 1, String.valueOf(mapMerge.get(fieldNames[i])));
                }
                resultInsert = ps.executeUpdate();
                if (resultInsert == 1) {
                    result = true;
                }
            }
            connection.commit();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            connection.close();
            ps.close();
            rs.close();
        }
        return result;
    }
}
