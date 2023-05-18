package org.example.EmailAccount;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;
import org.example.EmailObject.EmailAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GetEmailAccount {
    private static final Logger logger = LogManager.getLogger(GetEmailAccount.class);
    public static List<EmailAccount> getAccount() {
        List<EmailAccount> lstAccount = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            String sql = "select * from email.email_account where status = '0'";
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                EmailAccount emailAccount = new EmailAccount();
                String username = rs.getString("username");
                String password = rs.getString("password");
                String host = rs.getString("host");
                int port = rs.getInt("port");
                emailAccount.setUserName(username);
                emailAccount.setPassword(password);
                emailAccount.setHost(host);
                emailAccount.setPort(port);
                lstAccount.add(emailAccount);
            }
        } catch (Exception ex){
            logger.error("error get email account " + ex);
        }
        return lstAccount;
    }
}
