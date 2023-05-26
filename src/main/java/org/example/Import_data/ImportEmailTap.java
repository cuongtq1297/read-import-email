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

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport, Long emailConfigId) throws Exception {
        Connection connection1 = null;
        Connection connection2 = null;
        BufferedReader reader = new BufferedReader(new StringReader(data));
        StringBuilder sbTapInPending = new StringBuilder();
        StringBuilder sbTapOutPending = new StringBuilder();
        StringBuilder sbTapInMissing = new StringBuilder();
        StringBuilder sbTapOutMissing = new StringBuilder();
        boolean isTapInPending = false;
        boolean isTapOutPending = false;
        boolean isTapInMissing = false;
        boolean isTapOutMissing = false;
        boolean result = false;
        boolean run = true;
        String line;
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connectNew("TAP");
            connection2.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.contains("TAP IN pending:")) {
                    isTapInPending = true;
                } else if (line.contains("TAP OUT pending:") && isTapInPending) {
                    isTapInPending = false;
                    isTapOutPending = true;
                } else if (isTapInPending) {
                    sbTapInPending.append(line + "\n");
                } else if (line.contains("Files missing or rejected") && isTapOutPending) {
                    isTapOutPending = false;
                } else if (isTapOutPending) {
                    sbTapOutPending.append(line + "\n");
                } else if (line.contains("TAP IN missing:")) {
                    isTapInMissing = true;
                } else if (line.contains("TAP Out missing:") && isTapInMissing) {
                    isTapInMissing = false;
                    isTapOutMissing = true;
                } else if (isTapInMissing) {
                    sbTapInMissing.append(line + "\n");
                } else if (line.contains("END OF REPORT") && isTapOutMissing) {
                    isTapOutMissing = false;
                } else if (isTapOutMissing) {
                    sbTapOutMissing.append(line + "\n");
                }
            }
            String tapInPendingLst = sbTapInPending.toString().replaceAll("-{2,}", "").replaceAll("_", "").trim();
            String tapOutPendingLst = sbTapOutPending.toString().replaceAll("-{2,}", "").replaceAll("_", "").trim();
            String tapInMissingLst = sbTapInMissing.toString().replaceAll("[-_]", "").trim();
            String tapOutMissingLst = sbTapOutMissing.toString().replaceAll("[-_]", "").trim();

            String[] tapInPendingParts = tapInPendingLst.split("\n\n");
            String[] tapOutPendingParts = tapOutPendingLst.split("\n\n");
            if (run) {
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
                    list.add(1, pendingTime);
                    list.add(2, charge);
                    list.add(3, errorCharge);
                    list.add(4, firstCall);
                    list.add(5, fileCount);
                    list.add(6, error);
                    list.add(7, action);
                    list.add(8, "");
                    list.add(9, "TAP OUT PENDING");
                    result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
                    if (!result) {
                        run = false;
                        break;
                    }
                }
            }
            if (run) {
                for (String part : tapOutPendingParts) {
                    result = false;
                    BufferedReader readerTapInPending = new BufferedReader(new StringReader(part));
                    String lineTapOutPending;
                    String tapName = "";
                    String pendingTime = "";
                    String charge = "";
                    String errorCharge = "";
                    String firstCall = "";
                    String fileCount = "";
                    String error = "";
                    String action = "";
                    List<String> list = new ArrayList<>();
                    while ((lineTapOutPending = readerTapInPending.readLine()) != null) {
                        if (lineTapOutPending.contains("pending for")) {
                            String[] fields = lineTapOutPending.trim().split(" ");
                            tapName = fields[0];
                            pendingTime = fields[3];
                        } else if (lineTapOutPending.trim().startsWith("Charge")) {
                            charge = lineTapOutPending.split(":")[1].replaceAll("SDR", "").trim();
                        } else if (lineTapOutPending.trim().startsWith("Error Charge")) {
                            errorCharge = lineTapOutPending.split(":")[1].replaceAll("SDR", "").trim();
                        } else if (lineTapOutPending.trim().startsWith("First Call")) {
                            firstCall = lineTapOutPending.split(":")[1].trim();
                        } else if (lineTapOutPending.trim().startsWith("File Count")) {
                            fileCount = lineTapOutPending.split(":")[1].trim();
                        } else if (lineTapOutPending.trim().startsWith("Error")) {
                            error = lineTapOutPending.split(":")[1].trim();
                        } else {
                            action += lineTapOutPending.replaceAll("[-_:]", "").replace("ACTION", "").trim() + "; ";
                        }
                    }
                    readerTapInPending.close();

                    list.add(0, tapName);
                    list.add(1, pendingTime);
                    list.add(2, charge);
                    list.add(3, errorCharge);
                    list.add(4, firstCall);
                    list.add(5, fileCount);
                    list.add(6, error);
                    list.add(7, action);
                    list.add(8, "");
                    list.add(9, "TAP OUT PENDING");
                    result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
                    if (!result) {
                        break;
                    }
                }
            }
            if (run) {
                BufferedReader readerTapInMissing = new BufferedReader(new StringReader(tapInMissingLst));
                String lineTapInMissing;

                while ((lineTapInMissing = readerTapInMissing.readLine()) != null) {
                    List<String> list = new ArrayList<>();
                    result = false;
                    String[] fields = lineTapInMissing.split(" ");
                    String tapName = fields[2];
                    String informationTap = String.join(" ", Arrays.copyOfRange(fields, 3, fields.length));

                    list.add(0, tapName);
                    list.add(1, "");
                    list.add(2, "");
                    list.add(3, "");
                    list.add(4, "");
                    list.add(5, "");
                    list.add(6, "");
                    list.add(7, "");
                    list.add(8, informationTap);
                    list.add(9, "TAP IN MISSING");
                    result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
                    if (!result) {
                        break;
                    }
                }
                readerTapInMissing.close();
            }
            if (run) {
                BufferedReader readerTapOutMissing = new BufferedReader(new StringReader(tapOutMissingLst));
                String lineTapOutMissing;
                while ((lineTapOutMissing = readerTapOutMissing.readLine()) != null) {
                    List<String> list = new ArrayList<>();
                    result = false;
                    String[] fields = lineTapOutMissing.split(" ");
                    String tapName = fields[2];
                    String informationTap = String.join(" ", Arrays.copyOfRange(fields, 3, fields.length));

                    list.add(0, tapName);
                    list.add(1, "");
                    list.add(2, "");
                    list.add(3, "");
                    list.add(4, "");
                    list.add(5, "");
                    list.add(6, "");
                    list.add(7, "");
                    list.add(8, informationTap);
                    list.add(9, "TAP OUT MISSING");
                    result = InsertData(connection1, connection2, list, tableImport, emailConfigId);
                    if (!result) {
                        break;
                    }
                }
                readerTapOutMissing.close();
            }
            if (result) {
                connection2.commit();
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            connection1.close();
            connection2.close();
            reader.close();
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