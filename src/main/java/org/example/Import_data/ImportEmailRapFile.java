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
import java.text.SimpleDateFormat;
import java.util.*;

public class ImportEmailRapFile {
    private static final Logger logger = LogManager.getLogger(ImportEmailRapFile.class);

    public static boolean importData(String data, Long emailConfigId) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isRapIn = false;
        boolean isRapOut = false;
        String line;
        StringBuilder sbRapIn = new StringBuilder();
        StringBuilder sbRapOut = new StringBuilder();
        try {
            connection1 = GetConnection.connect();
            String tableImport = "";
            connection1 = GetConnection.connect();
            String sql = "select * from email.email_database_connection where type_name = 'RAP'";
            stmt = connection1.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                tableImport = rs.getString("table_import");
            } else {
                return false;
            }
            connection2 = GetConnectionToImport.connectNew("RAP");
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
            BufferedReader readerIn = new BufferedReader(new StringReader(sbRapIn.toString()));
            String lineIn;
            while ((lineIn = readerIn.readLine()) != null && !lineIn.contains("No RAP IN Files created")) {
                result = false;
                String[] fields = sbRapIn.toString().split("\\s+");
                List<String> list = new ArrayList<>();
                String errorDescription = String.join(" ", Arrays.copyOfRange(fields, 7, fields.length));
                list.add(0, fields[0]);
                list.add(1, fields[1]);
                list.add(2, fields[2]);
                list.add(3, fields[3]);
                list.add(4, fields[4]);
                list.add(5, fields[5]);
                list.add(6, fields[6]);
                list.add(7, errorDescription);
                list.add(8, "I");
                list.add(9,fields[1].substring(2,7));
                list.add(10,fields[1].substring(7,12));
                result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
            }
            BufferedReader readerOut = new BufferedReader(new StringReader(sbRapOut.toString()));
            String lineOut;
            while ((lineOut = readerOut.readLine()) != null && !lineOut.contains("No RAP OUT Files created")) {
                result = false;
                String[] fields = sbRapOut.toString().split("\\s+");
                List<String> list = new ArrayList<>();
                String errorDescription = String.join(" ", Arrays.copyOfRange(fields, 7, fields.length));
                list.add(0, fields[0]);
                list.add(1, fields[1]);
                list.add(2, fields[2]);
                list.add(3, fields[3]);
                list.add(4, fields[4]);
                list.add(5, fields[5]);
                list.add(6, fields[6]);
                list.add(7, errorDescription);
                list.add(8, "O");
                list.add(9,fields[1].substring(7,12));
                list.add(10,fields[1].substring(2,7));
                result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
            }
            reader.close();
            readerIn.close();
            readerOut.close();
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

    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, Long emailConfigId) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String getDataImportConfig = "select * from email.email_config_detail where email_config_id = ? ";
            ps = connection1.prepareStatement(getDataImportConfig);
            ps.setLong(1, emailConfigId);
            rs = ps.executeQuery();
            List<Map<String, String>> lstAll = new ArrayList<>();
            List<Map<String, String>> lstCheckExist = new ArrayList<>();
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
                    Map<String, String> map = new HashMap<>();
                    map.put("column_import", rs.getString("column_import"));
                    map.put("value", value);
                    lstAll.add(map);
                } else if (type.equals("datetime")) {
                    Map<String, String> map = new HashMap<>();
                    map.put("column_import", rs.getString("column_import"));
                    map.put("value", formatDatetime(value));
                    lstAll.add(map);
                } else if (type.equals("number")) {
                    Map<String, String> map = new HashMap<>();
                    map.put("column_import", rs.getString("column_import"));
                    map.put("value", value);
                    lstAll.add(map);
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

    public static String formatDatetime(String dateTimeString) throws Exception {
        String formattedDateTime = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            Date date = formatter.parse(dateTimeString);
            SimpleDateFormat newFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formattedDateTime = newFormatter.format(date);
        } catch (Exception e) {
            logger.error(e);
        }
        return formattedDateTime;
    }
}
