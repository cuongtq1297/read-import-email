package org.example.Import_data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;
import org.example.Database.GetConnectionToImport;

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

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        String line;
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connect(ipDb, user, password);
            connection2.setAutoCommit(false);
            Map<String, String> map1 = new HashMap<>();
            Map<String, String> map2 = new HashMap<>();
            Map<String, String> map3 = new HashMap<>();
            Map<String, String> map4 = new HashMap<>();
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
                } else if (line.startsWith("P")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    map3.put("imsi", fields.get(1));
                    map3.put("date_first_event", fields.get(2));
                    map3.put("time_first_event", fields.get(3));
                    map3.put("date_last_event", fields.get(4));
                    map3.put("time_last_event", fields.get(5));
                    map3.put("dc", fields.get(6));
                    map3.put("nc", fields.get(7));
                    map3.put("volume", fields.get(8));
                    map3.put("sdr", fields.get(9));
                } else if (line.startsWith("C")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    map4.put("imsi", fields.get(1));
                    map4.put("date_first_event", fields.get(2));
                    map4.put("time_first_event", fields.get(3));
                    map4.put("date_last_event", fields.get(4));
                    map4.put("time_last_event", fields.get(5));
                    map4.put("dc", fields.get(6));
                    map4.put("nc", fields.get(7));
                    map4.put("volume", fields.get(8));
                    map4.put("sdr", fields.get(9));
                }
                if (!map3.isEmpty() && line.startsWith("P")) {
                    mapMerge = mergeMaps(map1, map2, map3);
                    result = InsertData(connection1, connection2, mapMerge, tableImport);
                    if (!result) {
                        break;
                    }
                }
                if (!map4.isEmpty() && line.startsWith("C")) {
                    mapMerge = mergeMaps(map1, map2, map4);
                    result = InsertData(connection1, connection2, mapMerge, tableImport);
                    if (!result) {
                        break;
                    }
                }
            }
            if (result) {
                connection2.commit();
            }

        } catch (Exception e) {
            logger.error("import data fail" + e);
        } finally {
            connection1.close();
            connection2.close();
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

    public static boolean InsertData(Connection connection1, Connection connection2, Map mapMerge, String tableImport) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String getDataImportConfig = "select fields from email.data_import_config where type = 'HUR'";
            ps = connection1.prepareStatement(getDataImportConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                String fields = rs.getString("fields");
                String[] fieldNames = fields.split(",");
                String insertQuery = "INSERT INTO " + tableImport + " (";
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
                ps = connection2.prepareStatement(insertQuery);
                for (int i = 0; i < fieldNames.length; i++) {
                    ps.setString(i + 1, String.valueOf(mapMerge.get(fieldNames[i])));
                }
                resultInsert = ps.executeUpdate();
                if (resultInsert == 1) {
                    result = true;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            ps.close();
            rs.close();
        }
        return result;
    }
}
