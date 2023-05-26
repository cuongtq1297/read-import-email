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
import java.util.*;

public class ImportEmailMissingConfig {
    private static final Logger logger = LogManager.getLogger(ImportEmailMissingConfig.class);

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport, Long emailConfigId) throws Exception {
        boolean result = false;
        Connection connection2 = null;
        Connection connection1 = null;
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connectNew("MCL");
            connection2.setAutoCommit(false);
            BufferedReader reader = new BufferedReader(new StringReader(data.trim()));
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> fields = Arrays.asList(line.split("\\s+"));
                if (!fields.get(0).equals("Tapname")) {
                    result = InsertData(connection1, connection2, fields, tableImport, emailConfigId);
                    if (!result) {
                        break;
                    }
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

    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, Long emailConfigId) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> lstAll = new ArrayList<>();
        try {
            String getDataImportConfig = "select * from email.email_config_detail where email_config_id = ? ";
            ps = connection1.prepareStatement(getDataImportConfig);
            ps.setLong(1, emailConfigId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type");
                if (type.equals("text")) {
                    String seq = rs.getString("seq_in_file");
                    int seqInt = Integer.parseInt(seq);
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("column_import", rs.getString("column_import"));
                        map.put("value", fields.get(seqInt));
                        lstAll.add(map);
                    }
                } else if (type.equals("number")) {
                    String seq = rs.getString("seq_in_file");
                    int seqInt = Integer.parseInt(seq);
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("column_import", rs.getString("column_import"));
                        map.put("value", fields.get(seqInt));
                        lstAll.add(map);
                    }
                }
            }
            String insertQuery = "INSERT INTO " + tableImport + " (";
            for (int i = 0; i < lstAll.size(); i++) {
                String column = (String) lstAll.get(i).get("column_import");
                insertQuery += column;
                if (i < lstAll.size() - 1) {
                    insertQuery += ",";
                }
            }
            insertQuery += ") VALUES (";
            for (int i = 0; i < lstAll.size(); i++) {
                String value = (String) lstAll.get(i).get("value");
                insertQuery += "'" + value + "'";
                if (i < lstAll.size() - 1) {
                    insertQuery += ",";
                }
            }
            insertQuery += ")";
            ps = connection2.prepareStatement(insertQuery);
            resultInsert = ps.executeUpdate();
            if (resultInsert == 1) {
                result = true;
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
