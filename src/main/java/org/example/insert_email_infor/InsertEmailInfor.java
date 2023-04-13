package org.example.insert_email_infor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.database.getConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertEmailInfor {
    private static final Logger logger = LogManager.getLogger(InsertEmailInfor.class);

    public static boolean insertEmailInfor(String senderMail, String receiverMail, String subject, String attachmentName, String type) throws SQLException {
        boolean result = false;
        Connection connection = null;
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            connection = getConnection.connect();
            connection.setAutoCommit(false);
            String checkSql = "select * from email.email_processing " +
                    "where sender_mail = ? and receiver_mail = ? and subject = ? and attachment_name = ? and type = ?";
            String importSql = "INSERT INTO email.email_processing(sender_mail,receiver_mail,subject,attachment_name,type,create_at)\n" +
                    "values (?,?,?,?,?,NOW())";
            ps = connection.prepareStatement(importSql);
            ps1 = connection.prepareStatement(checkSql);
            ps1.setString(1,senderMail);
            ps1.setString(2,receiverMail);
            ps1.setString(3,subject);
            ps1.setString(4,attachmentName);
            ps1.setString(5,type);
            rs = ps1.executeQuery();
            if(!rs.next()){
                ps.setString(1,senderMail);
                ps.setString(2,receiverMail);
                ps.setString(3,subject);
                ps.setString(4,attachmentName);
                ps.setString(5,type);
                ps.executeUpdate();
                connection.commit();
                result = true;
            }
        } catch (Exception e){
            logger.error("insert email information fail" + e);
        } finally {
            connection.close();
            ps.close();
            ps1.close();
            rs.close();
        }
        return result;
    }
}
