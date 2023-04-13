package org.example.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;


public class Process {
    private static final Logger logger = LogManager.getLogger(Process.class);
    static class MyTask extends TimerTask {
        public void run() {
            logger.info("email process start");
            EmailProcess.EmailProcess();
            logger.info("email process is done");
        }
    }
    public static void main(String[] args) throws SQLException {
        Timer timer = new Timer();
        timer.schedule(new MyTask(), 0, 5 * 60 * 1000); //schedule the task to run every 5 minutes
    }
}
