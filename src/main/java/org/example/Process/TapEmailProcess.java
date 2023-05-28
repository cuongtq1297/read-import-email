package org.example.Process;

import com.sun.mail.pop3.POP3Message;
import com.sun.mail.util.BASE64DecoderStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.CreateSessionMail.GetMessage;
import org.example.Database.GetConnection;
import org.example.EmailAccount.GetEmailAccount;
import org.example.EmailObject.EmailAccount;
import org.example.EmailObject.EmailConfig;
import org.example.Filter_email.FilterEmail;
import org.example.Get_config.GetEmailConfig;
import org.example.Import_data.ImportEmailTap;
import org.example.Insert_email_infor.CheckEmail;
import org.example.Insert_email_infor.InsertEmail;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class TapEmailProcess {
    private static final Logger logger = LogManager.getLogger(TapEmailProcess.class);
    private static final String TYPE_NAME = "TAP";
    public static void TapEmailProcess() throws Exception {
        try {
            // Lay account
            List<EmailAccount> accountList = GetEmailAccount.getAccount();
            // lay config
            List<EmailConfig> lstEmailConfig = GetEmailConfig.getEmailConfigNew(TYPE_NAME);
            for (EmailAccount account : accountList) {
                Message[] messages = GetMessage.getMessageFromInboxFolder(account.getUserName(), account.getPassword(), account.getHost(), account.getPort());
                System.out.println("co " + messages.length + " thu");
                for (int i = 0; i < messages.length; i++) {
                    boolean checkRecord = false;
                    boolean isMulti = false;
                    Message message = messages[i];
                    String messageId = ((POP3Message) message).getMessageID().replace("<", "").replace(">", "");
                    // check da xu ly
                    checkRecord = CheckEmail.checkRecord(messageId);
                    isMulti = message.getContent() instanceof Multipart;
                    if (checkRecord && isMulti) {
                        int startIdx = message.getFrom()[0].toString().indexOf("<") + 1;
                        int endIdx = message.getFrom()[0].toString().indexOf(">");
                        String senderMail = message.getFrom()[0].toString().substring(startIdx, endIdx);
                        String subjectMail = message.getSubject();
                        String receiverMail = "";
                        if (message.getAllRecipients()[0].toString().contains("<")) {
                            int start = message.getAllRecipients()[0].toString().indexOf("<") + 1;
                            int end = message.getAllRecipients()[0].toString().indexOf(">");
                            receiverMail = message.getAllRecipients()[0].toString().substring(start, end);
                        } else {
                            receiverMail = message.getAllRecipients()[0].toString();
                        }
                        Multipart multipart = (Multipart) message.getContent();
                        List<String> fileNames = new ArrayList<>();
                        List<BodyPart> bodyParts = new ArrayList<>();
                        for (int j = 0; j < multipart.getCount(); j++) {
                            BodyPart bodyPart = multipart.getBodyPart(j);
                            String fileName = bodyPart.getFileName();
                            // kiểm tra phần có phải là file đính kèm được gửi từ email hay không
                            if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase("attachment")) {
                                fileNames.add(fileName);
                                bodyParts.add(bodyPart);
                            }
                        }
                        String fileNameLst = "";
                        for (int a = 0; a < fileNames.size(); a++) {
                            fileNameLst += fileNames.get(a);
                            if (a < fileNames.size() - 1) {
                                fileNameLst += ";";
                            }
                        }
                        EmailConfig emailConfig = FilterEmail.checkSenderSubject(senderMail, subjectMail, lstEmailConfig);
                        String fileEmlName = account.getAccountId() + "-" + messageId+ ".eml";
                        File file = new File(fileEmlName);
                        FileOutputStream output = new FileOutputStream(file);
                        message.writeTo(output);
                        output.close();

                        if (emailConfig.getEmailConfigId() != null) {
                            // insert pending
                            boolean insertPending = InsertEmail.insertPending(senderMail, subjectMail, receiverMail, fileNameLst, emailConfig.getEmailConfigId(), TYPE_NAME, messageId, fileEmlName);
                            if (insertPending) {
                                boolean checkAttachment = FilterEmail.checkAttachment(fileNames, emailConfig);
                                if (checkAttachment) {
                                    for (BodyPart bodyPart : bodyParts) {
                                        String attachmentContent = "";
                                        BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int bufferSize;
                                        byte[] buffer = new byte[8 * 1024];
                                        while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
                                            stringBuilder.append(new String(buffer, 0, bufferSize));
                                        }
                                        attachmentContent = stringBuilder.toString();
                                        boolean resultImport = ImportEmailTap.importData(attachmentContent, emailConfig.getEmailConfigId());
                                        if (resultImport) {
                                            InsertEmail.updateStatusNew(messageId, "1");
                                        }
                                    }
                                } else {
                                    // update attachment khong hop le
                                    InsertEmail.updateStatusNew(messageId, "3");
                                }
                            }
                        } else {
                            logger.info("email: Không phải email tap" + "\n" + senderMail + subjectMail);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in tap process : " + e);
        }
    }

    static class MyTask extends TimerTask {
        public void run() {
            try {
                System.out.println("start");
                TapEmailProcess();
                System.out.println("done");
            } catch (Exception e) {
                logger.error(e.getMessage() + e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String timeConfig = "";
        try {
            connection = GetConnection.connect();
            String sql = "Select time_config from email.email_database_connection where type_name = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, TYPE_NAME);
            rs = stmt.executeQuery();
            if (rs.next()) {
                timeConfig = rs.getString("time_config");
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + e);
        }
        String[] time = timeConfig.split(":");
        int h = Integer.parseInt(time[0]);
        int m = Integer.parseInt(time[1]);
        int s = Integer.parseInt(time[2]);
        Timer timer = new Timer();
        // Chay theo thoi gian cau hinh bao nhieu phut chay lai
        timer.schedule(new TapEmailProcess.MyTask(), 0, h * 36000000 + m * 60000 + s * 1000);
    }
}
