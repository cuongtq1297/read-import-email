package org.example.Insert_email_infor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertEmail {
    private static final Logger logger = LogManager.getLogger(InsertEmail.class);

    public static void insertEmail(boolean result, String senderMail, String receiverMail, String subject, String attachmentName, String type) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String importSql = "INSERT INTO email.email_processing(sender_mail,receiver_mail,subject,attachment_name,type,status,create_at)\n" +
                    "values (?,?,?,?,?,?,NOW())";
            ps = connection.prepareStatement(importSql);
            ps.setString(1, senderMail);
            ps.setString(2, receiverMail);
            ps.setString(3, subject);
            ps.setString(4, attachmentName);
            ps.setString(5, type);
            if (result) {
                ps.setString(6, "0");
            } else {
                ps.setString(6, "1");
            }
            ps.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            logger.error("insert email information fail" + e);
        } finally {
            connection.close();
            ps.close();
        }

    }
}
