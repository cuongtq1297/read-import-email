package org.example.Get_config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GetEmailConfig {
    private static final Logger logger = LogManager.getLogger(GetEmailConfig.class);

    public static List<List<Object>> getConfig(String type) {
        List<List<Object>> lstAll = new ArrayList<>();
        String attachFileType = "";
        String ipDb = "";
        String userPassword = "";
        String patternSelector = "";
        String tableImport = "";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            String getEmailConfig = "select * from email.email_config where type = ?";
            ps = connection.prepareStatement(getEmailConfig);
            ps.setString(1, type);
            rs = ps.executeQuery();
            while (rs.next()) {
                String senderMailList = rs.getString("sender_mail");
                List<String> lstSenderMail = Arrays.asList(senderMailList.split(";"));

                String senderSelector = rs.getString("sender_selector");

                String listSubject = rs.getString("mail_subject");
                List<String> lstSubject = Arrays.asList(listSubject.split(";"));

                String mailSubjectSelector = rs.getString("mail_subject_selector");

                String patternAttachment = rs.getString("pattern_attachment");
                List<String> lstPatternAttachment = Arrays.asList(patternAttachment.split(";"));

                patternSelector = rs.getString("pattern_selector");

                attachFileType = rs.getString("attach_file_type");

                ipDb = rs.getString("ip_db");

                userPassword = rs.getString("user_password_db");

                tableImport = rs.getString("table_import");
                String[] UP = userPassword.split(",");
                String user = UP[0];
                String password = UP[1];
                List<Object> list = new ArrayList<>();
                list.add(0, lstSenderMail);
                list.add(1, senderSelector);
                list.add(2, lstSubject);
                list.add(3, mailSubjectSelector);
                list.add(4, lstPatternAttachment);
                list.add(5, patternSelector);
                list.add(6, attachFileType);
                list.add(7, ipDb);
                list.add(8, user);
                list.add(9, password);
                list.add(10, tableImport);
                lstAll.add(list);
            }

        } catch (Exception e) {
            logger.error("error get email config " + e);
        }
        return lstAll;
    }
}
