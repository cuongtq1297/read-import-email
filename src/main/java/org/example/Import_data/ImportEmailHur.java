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

    public static boolean importData(String data, Long emailConfigId) throws Exception {
        Connection connection1 = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection connection2 = null;
        boolean result = false;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        String line;
        try {
            String tableImport = "";
            connection1 = GetConnection.connect();
            String sql = "select * from email.email_database_connection where type_name = 'HUR'";
            stmt = connection1.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                tableImport = rs.getString("table_import");
            } else {
                return false;
            }
            connection2 = GetConnectionToImport.connectNew("HUR");
            connection2.setAutoCommit(false);
            String hplmn = "";
            String vplmn = "";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("H")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    hplmn = fields.get(1);
                    vplmn = fields.get(2);
                }

                if (line.startsWith("P")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    result = InsertData(connection1, connection2, fields, tableImport, emailConfigId, hplmn, vplmn);
                    if (!result) {
                        break;
                    }
                } else if (line.startsWith("C")) {
                    List<String> fields = Arrays.asList(line.split(","));
                    result = InsertData(connection1, connection2, fields, tableImport, emailConfigId, hplmn, vplmn);
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


    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, Long emailConfigId, String hplmn, String vplmn) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> lstAll = new ArrayList<>();
        List<Map<String, String>> lstCheckExist = new ArrayList<>();
        try {
            String getDataImportConfig = "select * from email.email_config_detail where email_config_id = ? ";
            ps = connection1.prepareStatement(getDataImportConfig);
            ps.setLong(1, emailConfigId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type");
                String columnImport = rs.getString("column_import");
                String require = rs.getString("require");
                if (type.equals("text")) {
                    String seq = rs.getString("seq_in_file");
                    int seqInt = Integer.parseInt(seq) - 1;
                    if (require.equals("1")) {
                        if (fields.get(seqInt) == null || fields.get(seqInt).equals("")) {
                            return false;
                        } else {
                            Map<String, String> map = new HashMap<>();
                            map.put("column_import", columnImport);
                            map.put("value", fields.get(seqInt));
                            lstCheckExist.add(map);
                        }
                    }
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, String> map = new HashMap<>();
                        map.put("column_import", columnImport);
                        map.put("value", fields.get(seqInt));
                        lstAll.add(map);
                    }
                } else if (type.equals("datetime")) {
                    String seqInFile = rs.getString("seq_in_file");
                    String[] seqInFileLst = seqInFile.split(";");
                    String dateTime = "";
                    for (String seq : seqInFileLst) {
                        int seqInt = Integer.parseInt(seq) - 1;
                        if (!fields.get(seqInt).isBlank()) {
                            dateTime += fields.get(seqInt);
                        }
                    }
                    if (require.equals("1")) {
                        if (dateTime.equals("")) {
                            return false;
                        } else {
                            Map<String, String> map = new HashMap<>();
                            map.put("column_import", columnImport);
                            map.put("value", formatDatetime(dateTime));
                            lstCheckExist.add(map);
                        }
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("column_import", columnImport);
                    map.put("value", formatDatetime(dateTime));
                    lstAll.add(map);
                } else if (type.equals("number")) {
                    String seq = rs.getString("seq_in_file");
                    int seqInt = Integer.parseInt(seq) - 1;
                    if (require.equals("1")) {
                        if (fields.get(seqInt) == null || fields.get(seqInt).equals("")) {
                            return false;
                        } else {
                            Map<String, String> map = new HashMap<>();
                            map.put("column_import", columnImport);
                            map.put("value", fields.get(seqInt));
                            lstCheckExist.add(map);
                        }
                    }
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, String> map = new HashMap<>();
                        map.put("column_import", columnImport);
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
                String insertQuery = "INSERT INTO " + tableImport + " (hplmn, vplmn,";
                for (int i = 0; i < lstAll.size(); i++) {
                    String column = (String) lstAll.get(i).get("column_import");
                    insertQuery += column;
                    if (i < lstAll.size() - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ") VALUES (" + "'" + hplmn + "','" + vplmn + "',";
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
