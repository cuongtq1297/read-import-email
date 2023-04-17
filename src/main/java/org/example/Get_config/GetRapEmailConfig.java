package org.example.Get_config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetRapEmailConfig {
    private static final Logger logger = LogManager.getLogger(GetRapEmailConfig.class);
    public static List<String>[] getRapSenderMail() {
        List<String> lstSubjectMails = new ArrayList<>();
        List<String> lstSenderMails = new ArrayList<>();
        List<String> lstPatternSelector = new ArrayList<>();
        List<String> lstAttachFileType = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            String getEmailConfig = "select * from email.email_config where type = 'RAP'";
            ps = connection.prepareStatement(getEmailConfig);
            rs = ps.executeQuery();
            while (rs.next()) {
                String senderMailList = rs.getString("sender_mail");
                List<String> lstSenderMail = Arrays.asList(senderMailList.split(";"));
                lstSenderMails.addAll(lstSenderMail);

                String listSubject = rs.getString("mail_subject");
                List<String> lstSubject = Arrays.asList(listSubject.split(";"));
                lstSubjectMails.addAll(lstSubject);

                String attachFileType = rs.getString("attach_file_type");
                List<String> lstFileType = Arrays.asList(attachFileType);
                lstAttachFileType.addAll(lstFileType);

                String patternSelector = rs.getString("pattern_selector");
                List<String> lstPattern = Arrays.asList(patternSelector.split(";"));
                lstPatternSelector.addAll(lstPattern);
            }
        } catch (Exception e) {
            logger.error("error get email config " + e);
        }
        List<String>[] listAll = new List[]{lstSenderMails,lstSubjectMails,lstPatternSelector,lstAttachFileType};
        return listAll;
    }
}
