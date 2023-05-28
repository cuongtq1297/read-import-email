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

    public static boolean check(String senderMail, String receiverMail, String subject, String receivedDate) throws SQLException {
        boolean result = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String checkSql = "select * from email.email_processing " +
                    "where sender_mail = ? and subject = ? and receiver_mail = ? and received_date = ? and status != ?";
            ps = connection.prepareStatement(checkSql);
            ps.setString(1,senderMail);
            ps.setString(2,subject);
            ps.setString(3,receiverMail);
            ps.setString(4,receivedDate);
            ps.setString(5,"pending");
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
    public static boolean checkNew(String messageId) throws Exception{
        boolean result = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String checkSql = "select * from email.email_processing " +
                    "where message_id = ? and status != ?";
            ps = connection.prepareStatement(checkSql);
            ps.setString(1,messageId);
            ps.setString(2,"0");

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
    public static boolean checkRecord(String messageId) throws Exception{
        boolean result = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            String checkSql = "select * from email.email_process_results " +
                    "where message_id = ? and status != '0'";
            ps = connection.prepareStatement(checkSql);
            ps.setString(1,messageId);
            rs = ps.executeQuery();
            if(!rs.next()){
                result = true;
            }
        } catch (Exception e){
            logger.error("check email fail" + e);
        } finally {
            connection.close();
            ps.close();
            rs.close();
        }
        return result;
    }
}
