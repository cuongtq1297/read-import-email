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

public class GetDfdEmailConfig {
    private static final Logger logger = LogManager.getLogger(GetDfdEmailConfig.class);

    public static List<Object> getDfdSenderMail() {
        List<String> lstSubjectMails = new ArrayList<>();
        List<String> lstSenderMails = new ArrayList<>();
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
            String getEmailConfig = "select * from email.email_config where type = 'DFD'";
            ps = connection.prepareStatement(getEmailConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                String senderMailList = rs.getString("sender_mail");
                List<String> lstSenderMail = Arrays.asList(senderMailList.split(";"));
                lstSenderMails.addAll(lstSenderMail);

                String listSubject = rs.getString("mail_subject");
                List<String> lstSubject = Arrays.asList(listSubject.split(";"));
                lstSubjectMails.addAll(lstSubject);

                patternSelector = rs.getString("pattern_selector");

                attachFileType = rs.getString("attach_file_type");

                ipDb = rs.getString("ip_db");

                userPassword = rs.getString("user_password_db");

                tableImport = rs.getString("table_import");
            }
        } catch (Exception e) {
            logger.error("error get email config " + e);
        }
        String[] UP = userPassword.split(",");
        String user = UP[0];
        String password = UP[1];
        List<Object> list = new ArrayList<>();
        list.add(0, lstSenderMails);
        list.add(1, lstSubjectMails);
        list.add(2, patternSelector);
        list.add(3, attachFileType);
        list.add(4, ipDb);
        list.add(5, user);
        list.add(6, password);
        list.add(7, tableImport);
        return list;
    }
}
