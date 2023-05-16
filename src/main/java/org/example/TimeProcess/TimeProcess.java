package org.example.TimeProcess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Database.GetConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;

public class TimeProcess {
    private static final Logger logger = LogManager.getLogger(TimeProcess.class);
    public static boolean checkTimeProcess(String typeName) throws Exception {
        boolean checkTime = false;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = GetConnection.connect();
            String sql = "select * from email.email_process_time where type_name= ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, typeName);
            rs = ps.executeQuery();
            if (rs.next()) {
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                checkTime = isBetween(startTime, endTime);
            }
        } catch (Exception e) {
            logger.error("check time process fail \n" + e);
        }
        return checkTime;
    }

    public static boolean isBetween(String start, String end) {
        LocalTime startTime = LocalTime.parse(start);
        LocalTime endTime = LocalTime.parse(end);
        LocalTime now = LocalTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }

}
