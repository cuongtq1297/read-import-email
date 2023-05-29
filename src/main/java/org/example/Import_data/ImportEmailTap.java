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

public class ImportEmailTap {
    private static final Logger logger = LogManager.getLogger(ImportEmailTap.class);

    public static boolean importData(String data, Long emailConfigId) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        StringBuilder sbTapInPending = new StringBuilder();
        boolean isTapInPending = false;
        boolean result = false;
        String line;
        try {
            connection1 = GetConnection.connect();
            String tableImport = "";
            connection1 = GetConnection.connect();
            String sql = "select * from email.email_database_connection where type_name = 'TAP'";
            stmt = connection1.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                tableImport = rs.getString("table_import");
            } else {
                return false;
            }
            connection2 = GetConnectionToImport.connectNew("TAP");
            connection2.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.contains("TAP IN pending:")) {
                    isTapInPending = true;
                } else if (line.contains("TAP OUT pending:") && isTapInPending) {
                    break;
                } else if (isTapInPending) {
                    sbTapInPending.append(line + "\n");
                }
            }
//            while ((line = reader.readLine()) != null) {
//                if (line.contains("TAP IN pending:")) {
//                    isTapInPending = true;
//                } else if (line.contains("TAP OUT pending:") && isTapInPending) {
//                    isTapInPending = false;
//                    isTapOutPending = true;
//                } else if (isTapInPending) {
//                    sbTapInPending.append(line + "\n");
//                } else if (line.contains("Files missing or rejected") && isTapOutPending) {
//                    isTapOutPending = false;
//                } else if (isTapOutPending) {
//                    sbTapOutPending.append(line + "\n");
//                } else if (line.contains("TAP IN missing:")) {
//                    isTapInMissing = true;
//                } else if (line.contains("TAP Out missing:") && isTapInMissing) {
//                    isTapInMissing = false;
//                    isTapOutMissing = true;
//                } else if (isTapInMissing) {
//                    sbTapInMissing.append(line + "\n");
//                } else if (line.contains("END OF REPORT") && isTapOutMissing) {
//                    isTapOutMissing = false;
//                } else if (isTapOutMissing) {
//                    sbTapOutMissing.append(line + "\n");
//                }
//            }
            String tapInPendingLst = sbTapInPending.toString().replaceAll("-{2,}", "").replaceAll("_", "").trim();
            String[] tapInPendingParts = tapInPendingLst.split("\n\n");
            for (String part : tapInPendingParts) {
                result = false;
                BufferedReader readerTapInPending = new BufferedReader(new StringReader(part));
                String lineTapInPending;
                String tapName = "";
                String pendingTime = "";
                String charge = "";
                String errorCharge = "";
                String firstCall = "";
                String fileCount = "";
                String error = "";
                String action = "";
                List<String> list = new ArrayList<>();
                while ((lineTapInPending = readerTapInPending.readLine()) != null) {
                    if (lineTapInPending.contains("pending for")) {
                        String[] fields = lineTapInPending.trim().split(" ");
                        tapName = fields[0];
                        pendingTime = fields[3];
                    } else if (lineTapInPending.trim().startsWith("Charge")) {
                        charge = lineTapInPending.split(":")[1].replaceAll("SDR", "").trim();
                    } else if (lineTapInPending.trim().startsWith("Error Charge")) {
                        errorCharge = lineTapInPending.split(":")[1].replaceAll("SDR", "").trim();
                    } else if (lineTapInPending.trim().startsWith("First Call")) {
                        firstCall = lineTapInPending.split(":")[1].trim();
                    } else if (lineTapInPending.trim().startsWith("File Count")) {
                        fileCount = lineTapInPending.split(":")[1].trim();
                    } else if (lineTapInPending.trim().startsWith("Error")) {
                        error = lineTapInPending.split(":")[1].trim();
                    } else {
                        action += lineTapInPending.replaceAll("[-_:]", "").replace("ACTION", "").trim() + "; ";
                    }
                }
                readerTapInPending.close();

                list.add(0, tapName);
                list.add(1, charge);
                list.add(2, errorCharge);
                list.add(3, firstCall);
                list.add(4, fileCount);
                list.add(5, error);
                list.add(6, action);
                result = InsertData(connection1, connection2, list, tableImport, emailConfigId, pendingTime);
                if (!result) {
                    break;
                }
            }
            if (result) {
                connection2.commit();
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            connection1.close();
            connection2.close();
        }
        return result;
    }

    public static boolean InsertData(Connection connection1, Connection connection2, List<String> fields, String tableImport, Long emailConfigId, String pendingTime) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String getDataImportConfig = "select * from email.email_config_detail where email_config_id = ?";
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
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, String> map = new HashMap<>();
                        map.put("column_import", columnImport);
                        map.put("value", value);
                        lstAll.add(map);
                    }
                } else if (type.equals("datetime")) {
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, String> map = new HashMap<>();
                        map.put("column_import", columnImport);
                        map.put("value", formatDatetime(value));
                        lstAll.add(map);
                    }
                } else if (type.equals("number")) {
                    if (!fields.get(seqInt).isBlank()) {
                        Map<String, String> map = new HashMap<>();
                        map.put("column_import", columnImport);
                        map.put("value", value);
                        lstAll.add(map);
                    }
                }
            }
            String insertQuery = "INSERT INTO " + tableImport + " (pending_time,";
            for (int i = 0; i < lstAll.size(); i++) {
                String column = (String) lstAll.get(i).get("column_import");
                insertQuery += column;
                if (i < lstAll.size() - 1) {
                    insertQuery += ",";
                }
            }
            insertQuery += ") VALUES (" + "'" + pendingTime + "',";
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(dateTimeString);
            SimpleDateFormat newFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formattedDateTime = newFormatter.format(date);
        } catch (Exception e) {
            logger.error(e);
        }
        return formattedDateTime;
    }
}