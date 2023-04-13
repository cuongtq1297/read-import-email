package org.example.process;


import org.example.database.getConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.process.EmailProcess.EmailProcess;

public class Process {
    public class CheckValidToRun {
        public static Boolean checkValidToRun() throws SQLException {
            Connection connection = null;
            PreparedStatement ps = null;
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            Boolean result = false;
            try {
                connection = getConnection.connect();
                connection.setAutoCommit(false);
                String checkSqp = "select process_name from email.process_log WHERE process_name = 'IMPORT_EMAIL' and DATE(create_at) = CURDATE()";
                String insertSql = "insert into email.process_log(process_name,create_at) " +
                        "values('IMPORT_EMAIL',NOW())";
                ps = connection.prepareStatement(checkSqp);
                ps1 = connection.prepareStatement(insertSql);
                rs = ps.executeQuery();
                if(!rs.next()){
                    result = true;
                    ps1.executeUpdate();
                }
                connection.commit();
            } catch (Exception e){
                e.getMessage();
            } finally {
                connection.close();
                ps.close();
                ps1.close();
                rs.close();
            }
            return result;
        }
    }
    public static void main(String[] args) throws SQLException {
        Boolean invalidToRun = CheckValidToRun.checkValidToRun();
        EmailProcess(invalidToRun);
    }
}
