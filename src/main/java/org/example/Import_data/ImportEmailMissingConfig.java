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

    public static boolean importData(String data, Long emailConfigId) throws Exception {
        boolean result = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection connection2 = null;
        Connection connection1 = null;
        try {
            String tableImport = "";
            connection1 = GetConnection.connect();
            String sql = "select * from email.email_database_connection where type_name = 'MCL'";
            stmt = connection1.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                tableImport = rs.getString("table_import");
            } else {
                return false;
            }
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
        List<Map<String, String>> lstCheckExist = new ArrayList<>();
        try {
            String getDataImportConfig = "select * from email.email_config_detail where email_config_id = ? ";
            ps = connection1.prepareStatement(getDataImportConfig);
            ps.setLong(1, emailConfigId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String require = rs.getString("require");
                String seq = rs.getString("seq_in_file");
                int seqInt = Integer.parseInt(seq) - 1;
                String type = rs.getString("type");
                String columnImport = rs.getString("column_import");
                String value = fields.get(seqInt);
                if (require.equals("1")) {
                    if (value == null || value.equals("")) {
                        return false;
                    } else {
                        Map<String, String> map = new HashMap<>();
                        map.put("column_import", columnImport);
                        map.put("value", value);
                        lstCheckExist.add(map);
                    }
                }
                if (type.equals("text")) {
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("column_import", rs.getString("column_import"));
                        map.put("value", fields.get(seqInt));
                        lstAll.add(map);
                    }
                } else if (type.equals("number")) {
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("column_import", rs.getString("column_import"));
                        map.put("value", fields.get(seqInt));
                        lstAll.add(map);
                    }
                }
            }
            StringBuilder queryBuilder = new StringBuilder("SELECT 1 FROM ");
            queryBuilder.append(tableImport + " WHERE ");
            for (int i = 0; i < lstCheckExist.size(); i++) {
                Map<String, String> data = lstCheckExist.get(i);
                String columnName = data.get("column_import");
                String value = data.get("value");
                queryBuilder.append(columnName + " = '" + value + "'");

                if (i < lstCheckExist.size() - 1) {
                    queryBuilder.append(" AND ");
                }
            }
            ps = connection2.prepareStatement(queryBuilder.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("du lieu da ton tai");
                return false;
            } else {
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
            }
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
