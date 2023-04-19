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
import java.util.HashMap;
import java.util.Map;

public class ImportEmailDfd {
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        Map<String, String> map = new HashMap<>();
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isReading = false;
        String line;
        String line1;
        StringBuilder sb = new StringBuilder();
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connect(ipDb, user, password);
            connection2.setAutoCommit(false);
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

                map.put("hpmn", fields[0]);
                map.put("seqnr", fields[1]);
                map.put("cut_off_time", String.join(" ", fields[2], fields[3]));
                map.put("first_call_time", String.join(" ", fields[4], fields[5]));
                map.put("no_of_records", fields[6]);
                map.put("tax", fields[7]);
                map.put("charge", fields[8]);
                map.put("cur", fields[9]);
                map.put("received_time", String.join(" ", fields[10], fields[11]));
                map.put("validated_time", String.join(" ", fields[12], fields[13]));
                map.put("cnv", fields[14]);
                map.put("sent_time", String.join(" ", fields[15], fields[16]));
                map.put("rj", fields[17]);
                map.put("qr", fields[18]);
                map.put("sn", fields[19]);
                result = InsertData(connection1, connection2, map, tableImport);
                if (!result) {
                    break;
                }
            }
            if (result) {
                connection2.commit();
            }
            reader.close();
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
            String getDataImportConfig = "select fields from email.data_import_config where type = 'DFD'";
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
                    ps.setString(i + 1, String.valueOf(map.get(fieldNames[i])));
                }
                resultInsert = ps.executeUpdate();
                if (resultInsert == 1) {
                    result = true;
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
