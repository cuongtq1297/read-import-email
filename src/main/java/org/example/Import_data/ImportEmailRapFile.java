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
import java.util.Map;

public class ImportEmailRapFile {
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        Map<String, String> map = new HashMap<>();
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isRapIn = false;
        boolean isRapOut = false;
        String line;
        StringBuilder sbRapIn = new StringBuilder();
        StringBuilder sbRapOut = new StringBuilder();
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connect(ipDb, user, password);
            connection2.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.contains("Received")) {
                    isRapIn = true;
                } else if (line.contains("RAP OUT") && isRapIn) {
                    isRapIn = false;
                    isRapOut = true;
                } else if (isRapIn) {
                    sbRapIn.append(line.trim());
                } else if (line.contains("END OF REPORT") && isRapOut) {
                    isRapOut = false;
                } else if (isRapOut) {
                    sbRapOut.append(line.trim());
                }
            }
            if (!sbRapIn.toString().contains("No RAP IN Files created")) {
                String[] fields = sbRapIn.toString().split("\\s+");
                String errorDescription = String.join(" ", Arrays.copyOfRange(fields, 7, fields.length));
                map.put("rap_file", fields[0]);
                map.put("tap_file", fields[1]);
                map.put("date_received", fields[2]);
                map.put("tap_charge", fields[3]);
                map.put("record_type", fields[4]);
                map.put("error_charge", fields[5]);
                map.put("error_code", fields[6]);
                map.put("error_description", errorDescription);
                map.put("direction","I");
                map.put("hplmn",fields[1].substring(3,8));
                map.put("vplmn",fields[1].substring(8,13));
                result = InsertData(connection1, connection2, map, tableImport);
            }
            if (!sbRapOut.toString().contains("No RAP OUT Files created")) {
                String[] fields = sbRapIn.toString().split("\\s+");
                String errorDescription = String.join(" ", Arrays.copyOfRange(fields, 7, fields.length));
                map.put("rap_file", fields[0]);
                map.put("tap_file", fields[1]);
                map.put("date_received", fields[2]);
                map.put("tap_charge", fields[3]);
                map.put("record_type", fields[4]);
                map.put("error_charge", fields[5]);
                map.put("error_code", fields[6]);
                map.put("error_description", errorDescription);
                map.put("direction","O");
                map.put("hplmn",fields[1].substring(8,13));
                map.put("vplmn",fields[1].substring(3,8));
                result = InsertData(connection1, connection2, map, tableImport);
            }
            reader.close();
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

    public static boolean InsertData(Connection connection1, Connection connection2, Map map, String tableImport) throws Exception {
        boolean result = false;
        int resultInsert = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String getDataImportConfig = "select fields from email.data_import_config where type = 'RAP'";
            ps = connection1.prepareStatement(getDataImportConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                result = false;
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
                    ps.setString(i + 1, String.valueOf(map.get(fieldNames[i])));
                }
                resultInsert = ps.executeUpdate();
                if (resultInsert == 1) {
                    result = true;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            rs.close();
            ps.close();
        }
        return result;
    }
}
