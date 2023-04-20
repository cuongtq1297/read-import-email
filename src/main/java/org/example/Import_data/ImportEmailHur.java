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
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("P")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    result = InsertData(connection1, connection2, fields, tableImport);
                    if (!result) {
                        break;
                    }
                } else if (line.startsWith("C")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    result = InsertData(connection1, connection2, fields, tableImport);
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


    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> lstAll = new ArrayList<>();
        try {
            String getDataImportConfig = "select * from email.hur_email_config_detail";
            ps = connection1.prepareStatement(getDataImportConfig);
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
                } else if (type.equals("datetime")) {
                    String seqInFile = rs.getString("seq_in_file");
                    String[] seqInFileLst = seqInFile.split(",");
                    String dateTime = "";
                    for (String seq : seqInFileLst) {
                        int seqInt = Integer.parseInt(seq);
                        if (!fields.get(seqInt).isBlank()) {
                            dateTime += fields.get(seqInt);
                        }
                    }
                    if (!dateTime.isBlank()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("column_import", rs.getString("column_import"));
                        map.put("value", formatDatetime(dateTime));
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
            ps.close();
            rs.close();
        }
        return result;
    }

    public static String formatDatetime(String dateTimeString) throws Exception {
        String formattedDateTime = "";
        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateTime = inputFormatter.parse(dateTimeString);
            formattedDateTime = outputFormatter.format(dateTime);
        } catch (Exception e) {
            logger.error(e);
        }
        return formattedDateTime;
    }
}
