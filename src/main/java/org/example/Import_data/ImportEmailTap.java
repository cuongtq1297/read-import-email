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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImportEmailTap {
    private static final Logger logger = LogManager.getLogger(ImportEmailTap.class);

    public static boolean importData(String data, String ipDb, String user, String password, String tableImport) throws Exception {
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
        String line;
        try {
            connection1 = GetConnection.connect();
            connection2 = GetConnectionToImport.connect(ipDb, user, password);
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
            String tapInPendingLst = sbTapInPending.toString().replaceAll("[-_]", "").trim();
            String tapOutPendingLst = sbTapOutPending.toString().replaceAll("[-_]", "").trim();
            String tapInMissingLst = sbTapInMissing.toString().replaceAll("[-_]", "").trim();
            String tapOutMissingLst = sbTapOutMissing.toString().replaceAll("[-_]", "").trim();

            String[] tapInPendingParts = tapInPendingLst.split("\n\n");
            String[] tapOutPendingParts = tapOutPendingLst.split("\n\n");

            for (String part : tapInPendingParts) {
                result = false;
                Map<String, String> map = new HashMap<>();
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
                while ((lineTapInPending = readerTapInPending.readLine()) != null) {
                    if (lineTapInPending.contains("pending for")) {
                        String[] fields = lineTapInPending.trim().split(" ");
                        tapName = fields[0];
                        pendingTime = String.join(" ", Arrays.copyOfRange(fields, 3, fields.length));
                    } else if (lineTapInPending.trim().startsWith("Charge")) {
                        charge = lineTapInPending.split(":")[1].trim();
                    } else if (lineTapInPending.trim().startsWith("Error Charge")) {
                        errorCharge = lineTapInPending.split(":")[1].trim();
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
                map.put("tap_name", tapName);
                map.put("pending_time", pendingTime);
                map.put("charge", charge);
                map.put("error_charge", errorCharge);
                map.put("first_call", firstCall);
                map.put("file_count", fileCount);
                map.put("error", error);
                map.put("action", action);
                map.put("type", "TAP IN PENDING");
                result = InsertData(connection1, connection2, map, tableImport);
                if (!result) {
                    break;
                }
            }

            for (String part : tapOutPendingParts) {
                result = false;
                Map<String, String> map = new HashMap<>();
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
                while ((lineTapOutPending = readerTapInPending.readLine()) != null) {
                    if (lineTapOutPending.contains("pending for")) {
                        String[] fields = lineTapOutPending.trim().split(" ");
                        tapName = fields[0];
                        pendingTime = String.join(" ", Arrays.copyOfRange(fields, 3, fields.length));
                    } else if (lineTapOutPending.trim().startsWith("Charge")) {
                        charge = lineTapOutPending.split(":")[1].trim();
                    } else if (lineTapOutPending.trim().startsWith("Error Charge")) {
                        errorCharge = lineTapOutPending.split(":")[1].trim();
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
                map.put("tap_name", tapName);
                map.put("pending_time", pendingTime);
                map.put("charge", charge);
                map.put("error_charge", errorCharge);
                map.put("first_call", firstCall);
                map.put("file_count", fileCount);
                map.put("error", error);
                map.put("action", action);
                map.put("type", "TAP OUT PENDING");
                result = InsertData(connection1, connection2, map, tableImport);
                if (!result) {
                    break;
                }
            }
            BufferedReader readerTapInMissing = new BufferedReader(new StringReader(tapInMissingLst));
            String lineTapInMissing;
            while ((lineTapInMissing = readerTapInMissing.readLine()) != null) {
                result = false;
                Map<String, String> map = new HashMap<>();
                String[] fields = lineTapInMissing.split(" ");
                String tapName = fields[2];
                String informationTap = String.join(" ", Arrays.copyOfRange(fields, 3, fields.length));
                map.put("tap_name", tapName);
                map.put("information_tap_missing", informationTap);
                map.put("type", "TAP IN MISSING");
                result = InsertData(connection1, connection2, map, tableImport);
                if (!result) {
                    break;
                }
            }
            readerTapInMissing.close();
            BufferedReader readerTapOutMissing = new BufferedReader(new StringReader(tapOutMissingLst));
            String lineTapOutMissing;
            while ((lineTapOutMissing = readerTapOutMissing.readLine()) != null) {
                result = false;
                Map<String, String> map = new HashMap<>();
                String[] fields = lineTapOutMissing.split(" ");
                String tapName = fields[2];
                String informationTap = String.join(" ", Arrays.copyOfRange(fields, 3, fields.length));
                map.put("tap_name", tapName);
                map.put("information_tap_missing", informationTap);
                map.put("type", "TAP OUT MISSING");
                result = InsertData(connection1, connection2, map, tableImport);
                if (!result) {
                    break;
                }
            }
            readerTapOutMissing.close();
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

    public static boolean InsertData(Connection connection1, Connection connection2, Map map, String tableImport) throws Exception {
        boolean result = false;
        int resultInsert = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String getDataImportConfig = "select fields from email.data_import_config where type = 'TAP'";
            ps = connection1.prepareStatement(getDataImportConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                String fields = rs.getString("fields");
                String[] fieldNames = fields.split(",");
                String insertQuery = "INSERT INTO " + tableImport + " (";
                for (int i = 0; i < fieldNames.length; i++) {
                    insertQuery += fieldNames[i];
                    if (i < fieldNames.length - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ") VALUES (";
                for (int i = 0; i < fieldNames.length; i++) {
                    insertQuery += "?";
                    if (i < fieldNames.length - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ")";
                ps = connection2.prepareStatement(insertQuery);
                for (int i = 0; i < fieldNames.length; i++) {
                    ps.setString(i + 1, String.valueOf(map.get(fieldNames[i])));
                }
                resultInsert = ps.executeUpdate();
                if (resultInsert == 1) {
                    result = true;
                }
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