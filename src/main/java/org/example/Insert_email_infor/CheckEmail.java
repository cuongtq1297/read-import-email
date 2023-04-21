package org.example.Insert_email_infor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckEmail {
    private static final Logger logger = LogManager.getLogger(CheckEmail.class);

    public static boolean check(String senderMail, String receiverMail, String subject, String attachmentName) throws SQLException {
        boolean result = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String checkSql = "select * from email.email_processing " +
                    "where sender_mail = ? and receiver_mail = ? and subject = ? and attachment_name = ? and status = ?";
            ps = connection.prepareStatement(checkSql);
            ps.setString(1,senderMail);
            ps.setString(2,receiverMail);
            ps.setString(3,subject);
            ps.setString(4,attachmentName);
            ps.setString(5,"0");
            rs = ps.executeQuery();
            if(!rs.next()){
                result = true;
            }
        } catch (Exception e){
            logger.error("insert email information fail" + e);
        } finally {
            connection.close();
            ps.close();
            rs.close();
        }
        return result;
    }
}
