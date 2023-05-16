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
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport, Long emailConfigId) throws Exception {
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
                result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
            }
            if (!sbRapOut.toString().contains("No RAP OUT Files created")) {
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
                list.add(8, "O");
                result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
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

    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, Long emailConfigId) throws Exception {
        boolean result = false;
        int resultInsert = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String getDataImportConfig = "select * from email.email_config_detail where email_config_id = ?";
            ps = connection1.prepareStatement(getDataImportConfig);
            ps.setLong(1, emailConfigId);
            rs = ps.executeQuery();
            List<Map<String, Object>> lstAll = new ArrayList<>();
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
                    int seqInt = Integer.parseInt(seqInFile);
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("column_import", rs.getString("column_import"));
                        map.put("value", formatDatetime(fields.get(seqInt)));
                        lstAll.add(map);
                    }
                } else if (type.equals("number")) {
                    String seqInFile = rs.getString("seq_in_file");
                    int seqInt = Integer.parseInt(seqInFile);
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
