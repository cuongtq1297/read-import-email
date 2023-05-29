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

    public static boolean importData(String data, Long emailConfigId) throws Exception {
        Connection connection1 = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection connection2 = null;
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        boolean isHpmn = false;
        String line = "";
        String line1 = "";
        StringBuilder sb = new StringBuilder();
        try {
            String tableImport = "";
            connection1 = GetConnection.connect();
            String sql = "select * from email.email_database_connection where type_name = 'DFD'";
            stmt = connection1.prepareStatement(sql);

            rs = stmt.executeQuery();
            if (rs.next()) {
                tableImport = rs.getString("table_import");
            } else {
                return false;
            }
            String vpmn = "";
            connection2 = GetConnectionToImport.connectNew("DFD");
            connection2.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.contains("Customer")) {
                    String[] parts = line.split(" ");
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].contains("Customer:")) {
                            vpmn = parts[i + 1];
                            break;
                        }
                    }
                }
                if (line.contains("HPMN") && line.contains("Seqnr")) {
                    isHpmn = true;
                } else if (line.contains("Total number of VPMN") && isHpmn) {
                    isHpmn = false;
                    break;
                } else if (isHpmn) {
                    sb.append(line.trim() + "\n");
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
                    result = InsertData(connection1, connection2, list, tableImport, emailConfigId, vpmn);
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

    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, Long emailConfigId, String vpmn) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> lstAll = new ArrayList<>();
        List<Map<String, String>> lstCheckExist = new ArrayList<>();
        // todo: - lấy các trường require. check các trường require có null không nếu null thì không insert
        // todo: - nếu không null tiến hành check tồn tại trong db, chưa tồn tại thì insert
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
                        if (type.equals("text")) {
                            Map<String, String> map = new HashMap<>();
                            map.put("column_import", columnImport);
                            map.put("value", value);
                            lstCheckExist.add(map);
                        } else if (type.equals("datetime")) {
                            Map<String, String> map = new HashMap<>();
                            map.put("column_import", columnImport);
                            map.put("value", formatDatetime(value));
                            lstCheckExist.add(map);
                        } else if (type.equals("number")) {
                            Map<String, String> map = new HashMap<>();
                            map.put("column_import", columnImport);
                            map.put("value", value);
                            lstCheckExist.add(map);
                        }
                    }
                }
                if (type.equals("text")) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("column_import", columnImport);
                    map.put("value", value);
                    lstAll.add(map);
                } else if (type.equals("datetime")) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("column_import", columnImport);
                    map.put("value", formatDatetime(value));
                    lstAll.add(map);
                } else if (type.equals("number")) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("column_import", columnImport);
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
                String insertQuery = "INSERT INTO " + tableImport + " (vpmn,";
                for (int i = 0; i < lstAll.size(); i++) {
                    String column = (String) lstAll.get(i).get("column_import");
                    insertQuery += column;
                    if (i < lstAll.size() - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ") VALUES (" + "'" + vpmn + "',";
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
