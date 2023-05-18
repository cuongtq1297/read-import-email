package org.example.Process;

import com.sun.mail.pop3.POP3Message;
import com.sun.mail.util.BASE64DecoderStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.CreateSessionMail.GetMessage;
import org.example.EmailAccount.GetEmailAccount;
import org.example.EmailObject.EmailAccount;
import org.example.EmailObject.EmailConfig;
import org.example.Filter_email.FilterEmail;
import org.example.Get_config.GetEmailConfig;
import org.example.Import_data.ImportEmailRapFile;
import org.example.Insert_email_infor.CheckEmail;
import org.example.Insert_email_infor.InsertEmail;
import org.example.TimeProcess.TimeProcess;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import java.util.ArrayList;
import java.util.List;


public class RapEmailProcess {
    private static final Logger logger = LogManager.getLogger(RapEmailProcess.class);
    private static final String TYPE_NAME = "RAP";
    public static void RapEmailProcess() throws Exception {
        try {
            // Lay account
            List<EmailAccount> accountList = GetEmailAccount.getAccount();
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
                    System.out.println(message.getSubject());
                    if (checkRecord && isMulti ) {
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
                        List<EmailConfig> lstEmailConfig = GetEmailConfig.getEmailConfigNew(TYPE_NAME);
                        EmailConfig emailConfig = FilterEmail.checkSenderSubject(senderMail, subjectMail, lstEmailConfig);


                        if (emailConfig.getEmailConfigId() != null) {
                            // insert pending
                            boolean insertPending = InsertEmail.insertPending(senderMail, subjectMail, receiverMail, fileNameLst, emailConfig.getEmailConfigId(), TYPE_NAME, messageId);
                            if (insertPending) {
                                boolean checkAttachment = FilterEmail.checkAttachment(fileNames, emailConfig);
                                if (checkAttachment) {
                                    String ipDb = emailConfig.getIpDb();
                                    String user = emailConfig.getUsername();
                                    String password = emailConfig.getPassword();
                                    String tableImport = emailConfig.getTableImport();
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
                                        boolean resultImport = ImportEmailRapFile.importData(attachmentContent, ipDb, user, password, tableImport, emailConfig.getEmailConfigId());
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

    public static void main(String[] args) throws Exception {
        boolean checkTime = TimeProcess.checkTimeProcess(TYPE_NAME);
        if(checkTime){
            System.out.println("start");
            logger.info("Tien trinh quet email rap bat dau");
            RapEmailProcess();
            logger.info("Tien trinh hoan thanh");
            System.out.println("end");
        } else {
            System.out.println("khong phai thoi gian");
            logger.info("Khong trong thoi gian chay tien trinh tap");
        }
    }
}