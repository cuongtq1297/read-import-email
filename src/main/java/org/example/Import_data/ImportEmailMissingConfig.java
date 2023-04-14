package org.example.Import_data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ImportEmailMissingConfig {
    private static final Logger logger = LogManager.getLogger(ImportEmailMissingConfig.class);
    public static boolean importData(String data) throws SQLException {
        boolean result = false;
        int resultInsert = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = GetConnection.connect();
            connection.setAutoCommit(false);
            BufferedReader reader = new BufferedReader(new StringReader(data.trim()));
            String line;
            String importSql = "insert into email.mcl_email_attachment_data (tap_name,record,sdr_amount,create_at)" +
                    " values(?,?,?,now())" ;
            ps = connection.prepareStatement(importSql);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if(!parts[0].equals("Tapname")){
                    ps.setString(1,parts[0]);
                    ps.setString(2,parts[1]);
                    ps.setString(3,parts[2]);
                    resultInsert =  ps.executeUpdate();
                    if(resultInsert == 1){
                        result = true;
                    }
                }
            }
            connection.commit();
            reader.close();

        } catch (SQLException | IOException e) {
            logger.error("import data fail" + e);
            connection.rollback();
            result = false;
        } finally {
            connection.close();
            ps.close();
        }
        return result;
    }
}
