package org.example.Insert_email_infor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertEmail {
    private static final Logger logger = LogManager.getLogger(InsertEmail.class);

    public static boolean insertEmailPending(String senderMail, String subject, String attachmentName, int typeId, String receiverMail, String receivedDate) throws SQLException {
        boolean result = false;
        int insert;
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
            ps.setString(5, "pending");
            ps.setString(6, receiverMail);
            ps.setString(7, receivedDate);
            insert = ps.executeUpdate();
            if (insert == 1) {
                result = true;
            }
            connection.commit();
        } catch (Exception e) {
            logger.error("insert pending email information fail" + e);
        } finally {
            connection.close();
            ps.close();
        }
        return result;
    }

    public static void updateStatus(String senderMail, String subject, String attachmentName) throws SQLException {
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
            ps.setString(4, attachmentName);

            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            logger.error("insert email information fail" + e);
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
}
