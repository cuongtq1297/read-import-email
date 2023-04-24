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

public class ImportEmailDfd {
    private static final Logger logger = LogManager.getLogger(ImportEmailDfd.class);

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport, int typeId) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        boolean result = false;
        boolean result1 = false;
        boolean result2 = true;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isHpmn = false;
        boolean isVpmn = false;
        String line = "";
        String line1 = "";
        String line2 = "";
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connect(ipDb, user, password);
            connection2.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.contains("HPMN") && line.contains("Seqnr")) {
                    isHpmn = true;
                } else if (line.contains("Total number of VPMN") && isHpmn) {
                    isHpmn = false;
                } else if (isHpmn) {
                    sb.append(line.trim() + "\n");
                } else if (line.contains("VPMN") && line.contains("Seqnr")) {
                    isVpmn = true;
                } else if (line.contains("Total number of HPMN") && isVpmn) {
                    isVpmn = false;
                } else if (isVpmn) {
                    sb1.append(line.trim() + "\n");
                }
            }
            BufferedReader reader1 = new BufferedReader(new StringReader(sb.toString().trim()));
            while ((line1 = reader1.readLine()) != null) {
                if (!line1.matches("[\\s-]+")) {
                    List<String> list = new ArrayList<>();
                    list.add(0, (String) line1.subSequence(0, 5));
                    list.add(1, (String) line1.subSequence(6, 11));
                    list.add(2, (String) line1.subSequence(12, 27));
                    list.add(3, (String) line1.subSequence(28, 43));
                    list.add(4, (String) line1.subSequence(44, 51));
                    list.add(5, (String) line1.subSequence(52, 55));
                    list.add(6, (String) line1.subSequence(56, 66));
                    list.add(7, (String) line1.subSequence(67, 70));
                    list.add(8, (String) line1.subSequence(71, 86));
                    list.add(9, (String) line1.subSequence(87, 102));
                    list.add(10, (String) line1.subSequence(103, 104));
                    list.add(11, (String) line1.subSequence(105, 120));
                    list.add(12, (String) line1.subSequence(121, 124));
                    list.add(13, (String) line1.subSequence(125, 128));
                    list.add(14, (String) line1.subSequence(129, 132));
                    list.add(15, "");
                    result1 = InsertData(connection1, connection2, list, tableImport, typeId);
                    if (!result1) {
                        break;
                    }
                }
            }
            BufferedReader reader2 = new BufferedReader(new StringReader(sb1.toString().trim()));
            while ((line2 = reader2.readLine()) != null) {
                if (!line2.matches("[\\s-]+")) {
                    result2 = false;
                    List<String> list = new ArrayList<>();
                    list.add(0, "");
                    list.add(1, (String) line2.subSequence(6, 11));
                    list.add(2, (String) line2.subSequence(12, 27));
                    list.add(3, (String) line2.subSequence(28, 43));
                    list.add(4, (String) line2.subSequence(44, 51));
                    list.add(5, (String) line2.subSequence(52, 55));
                    list.add(6, (String) line2.subSequence(56, 66));
                    list.add(7, (String) line2.subSequence(67, 70));
                    list.add(8, (String) line2.subSequence(71, 86));
                    list.add(9, (String) line2.subSequence(87, 102));
                    list.add(10, (String) line2.subSequence(103, 104));
                    list.add(11, (String) line2.subSequence(105, 120));
                    list.add(12, (String) line2.subSequence(121, 124));
                    list.add(13, (String) line2.subSequence(125, 128));
                    list.add(14, (String) line2.subSequence(129, 132));
                    list.add(15, (String) line2.subSequence(0, 5));
                    result2 = InsertData(connection1, connection2, list, tableImport, typeId);
                    if (!result2) {
                        break;
                    }
                }
            }
            if (result1 && result2) {
                result = true;
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

    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, int typeId) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> lstAll = new ArrayList<>();
        try {
            String getDataImportConfig = "select * from email.email_config_detail where type_id = ?";
            ps = connection1.prepareStatement(getDataImportConfig);
            ps.setInt(1, typeId);
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
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy HH:mm");
            Date date = formatter.parse(dateTimeString);
            SimpleDateFormat newFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formattedDateTime = newFormatter.format(date);
        } catch (Exception e) {
            logger.error(e);
        }
        return formattedDateTime;
    }
}
