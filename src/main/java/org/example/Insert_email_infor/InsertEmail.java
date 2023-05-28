package org.example.Insert_email_infor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertEmail {
    private static final Logger logger = LogManager.getLogger(InsertEmail.class);

    public static boolean insertPending(String senderMail, String subjectMail, String receiverMail, String attachment, Long emailConfigId, String typeName, String messageId, String fileEml) throws Exception {
        boolean result = false;
        boolean exist = true;
        Connection connection = null;
        PreparedStatement ps = null;
        File emlFile = new File(fileEml);
        try {
            exist = checkPendingRecord(messageId);
            if (!exist) {
                FileInputStream fis = new FileInputStream(fileEml);
                connection = GetConnection.connect();
                connection.setAutoCommit(false);
                String sql = "insert into email.email_process_results (email_config_id, sender_mail, receiver_mail, subject_mail, attachment, type_name, message_id ,status ,create_at, email_data) " +
                        "values(?,?,?,?,?,?,?,'0',NOW(),?)";
                ps = connection.prepareStatement(sql);
                ps.setLong(1, emailConfigId);
                ps.setString(2, senderMail);
                ps.setString(3, receiverMail);
                ps.setString(4, subjectMail);
                ps.setString(5, attachment);
                ps.setString(6, typeName);
                ps.setString(7, messageId);
                ps.setBinaryStream(8, fis, emlFile.length());
                if (ps.executeUpdate() == 1) {
                    result = true;
                }
                connection.commit();
            } else {
                result = true;
            }
        } catch (Exception e) {
            logger.error("insert pending fail");
        }
        return result;
    }


    public static boolean checkPendingRecord(String messageId) throws SQLException {
        boolean exist = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            String checkPendingRecord = "select 1 from email.email_process_results where message_id = ? and status = '0'";
            ps = connection.prepareStatement(checkPendingRecord);
            ps.setString(1, messageId);
            rs = ps.executeQuery();
            if (rs.next()) {
                exist = true;
            }
        } catch (Exception e) {
            logger.error("insert pending email information fail" + e);
        } finally {
            connection.close();
            ps.close();
            rs.close();
        }
        return exist;
    }

    public static void updateStatus(String senderMail, String subject, String attachmentNames) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String importSql = "UPDATE email.email_processing SET status = ? WHERE sender_mail = ? and subject = ? and attachment_name = ? ";
            ps = connection.prepareStatement(importSql);
            ps.setString(1, "success");
            ps.setString(2, senderMail);
            ps.setString(3, subject);
            ps.setString(4, attachmentNames);

            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            logger.error("insert email information fail" + e);
        } finally {
            connection.close();
            ps.close();
        }
    }

    public static void updateStatusNew(String messageId, String status) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String importSql = "UPDATE email.email_process_results SET status = ? WHERE message_id = ?  ";
            ps = connection.prepareStatement(importSql);
            ps.setString(1, status);
            ps.setString(2, messageId);
            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            logger.error("update fail" + e);
        } finally {
            connection.close();
            ps.close();
        }
    }

    public static void insertTypeNotDefineEmail(String senderMail, String subject, String attachmentName, int typeId, String receiverMail, String receivedDate) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String importSql = "INSERT INTO email.email_processing(sender_mail,subject,attachment_name,type_id,status,receiver_mail,received_date,create_at)\n" +
                    "values (?,?,?,?,?,?,?,NOW())";
            ps = connection.prepareStatement(importSql);
            ps.setString(1, senderMail);
            ps.setString(2, subject);
            ps.setString(3, attachmentName);
            ps.setInt(4, typeId);
            ps.setString(5, "type not define");
            ps.setString(6, receiverMail);
            ps.setString(7, receivedDate);
            ps.executeUpdate();

            connection.commit();
        } catch (Exception e) {
            logger.error("insert pending email information fail" + e);
        } finally {
            connection.close();
            ps.close();
        }
    }

    public static void insertNotDefine(String senderMail, String subjectMail, String receiverMail, String attachment, Long emailConfigId, String typeName, String messageId) throws Exception {
        boolean result = false;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String sql = "insert into email.email_process_results ( sender_mail, receiver_mail, subject_mail, attachment, type_name, message_id ,status ,create_at) " +
                    "values(?,?,?,?,?,?,'2',NOW())";
            ps = connection.prepareStatement(sql);
            ps.setString(1, senderMail);
            ps.setString(2, receiverMail);
            ps.setString(3, subjectMail);
            ps.setString(4, attachment);
            ps.setString(5, typeName);
            ps.setString(6, messageId);
            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            logger.error("insert fail");
        }
    }

}
