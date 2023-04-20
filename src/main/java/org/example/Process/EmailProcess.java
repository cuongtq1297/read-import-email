package org.example.Process;

import com.sun.mail.util.BASE64DecoderStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Filter_email.FilterEmail;
import org.example.Get_config.*;
import org.example.Import_data.*;
import org.example.Insert_email_infor.CheckEmail;
import org.example.Insert_email_infor.InsertEmail;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class EmailProcess {
    private static final Logger logger = LogManager.getLogger(EmailProcess.class);
    private static String USER_NAME;
    private static String PASSWORD;
    private static String HOST;
    private static String PORT;
    private static String HUR_TYPE;
    private static String TAP_TYPE;
    private static String RAP_TYPE;
    private static String DFD_TYPE;
    private static String MCL_TYPE;

    public static final String CONFIG_FILE_PATH = "config/email-infor.cfg";

    static {
        Properties properties = new Properties();
        FileInputStream propsFile = null;
        try {
            propsFile = new FileInputStream(CONFIG_FILE_PATH);
            properties.load(propsFile);
            USER_NAME = properties.getProperty("USERNAME");
            PASSWORD = properties.getProperty("PASSWORD");
            HOST = properties.getProperty("HOST");
            PORT = properties.getProperty("PORT");

            HUR_TYPE = properties.getProperty("HUR_TYPE");
            TAP_TYPE = properties.getProperty("TAP_TYPE");
            RAP_TYPE = properties.getProperty("RAP_TYPE");
            DFD_TYPE = properties.getProperty("DFD_TYPE");
            MCL_TYPE = properties.getProperty("MCL_TYPE");

        } catch (Exception e) {
            logger.error("error config" + e);
        }
    }

    public static String readContentFromBASE64DecoderStream(BASE64DecoderStream base64DecoderStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(base64DecoderStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    public static void EmailProcess() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USER_NAME, PASSWORD);
                    }
                });

        try {
            List<List<Object>> lstHurMailConfig = GetEmailConfig.getConfig("HUR");
            List<List<Object>> lstTapMailConfig = GetEmailConfig.getConfig("TAP");
            List<List<Object>> lstDfdMailConfig = GetEmailConfig.getConfig("DFD");
            List<List<Object>> lstMclMailConfig = GetEmailConfig.getConfig("MCL");
            List<List<Object>> lstRapMailConfig = GetEmailConfig.getConfig("RAP");

            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", USER_NAME, PASSWORD);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
//            Folder targetFolder = store.getFolder("PROCESS_SUCCESS");
            Message[] messages = folder.getMessages();
            System.out.println("Có " + messages.length + " thư từ trong INBOX");
            for (int i = 0; i < messages.length; i++) {
                boolean checkEmail = false;
                boolean resultImport = false;
                Message message = messages[i];

                // xử lý string sender mail
                int startIdx = message.getFrom()[0].toString().indexOf("<") + 1;
                int endIdx = message.getFrom()[0].toString().indexOf(">");
                String senderMail = message.getFrom()[0].toString().substring(startIdx, endIdx);
                String subjectMail = message.getSubject();

                // kiểm tra các phần của email có multipart hay là text
                if (message.getContent() instanceof Multipart) {
                    Multipart multipart = (Multipart) message.getContent();
                    // duyệt qua từng phần của multipart
                    for (int j = 0; j < multipart.getCount(); j++) {
                        BodyPart bodyPart = multipart.getBodyPart(j);
                        // kiểm tra phần có phải là file đính kèm được gửi từ email hay không
                        if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase("attachment")) {
                            // kiểm tra định dạng file
                            String fileName = bodyPart.getFileName();

                            List isHur = FilterEmail.Filter(senderMail, subjectMail, fileName, lstHurMailConfig);
                            List isTap = FilterEmail.Filter(senderMail, subjectMail, fileName, lstTapMailConfig);
                            List isRap = FilterEmail.Filter(senderMail, subjectMail, fileName, lstRapMailConfig);
                            List isDfd = FilterEmail.Filter(senderMail, subjectMail, fileName, lstDfdMailConfig);
                            List isMcl = FilterEmail.Filter(senderMail, subjectMail, fileName, lstMclMailConfig);

                            // email canh bao hur
                            if (!isHur.isEmpty()) {
                                String ipDb = (String) ((ArrayList) isHur.get(0)).get(7);
                                String user = (String) ((ArrayList) isHur.get(0)).get(8);
                                String password = (String) ((ArrayList) isHur.get(0)).get(9);
                                String tableImport = (String) ((ArrayList) isHur.get(0)).get(10);
                                BASE64DecoderStream base64DecoderStream = null;
                                try {
                                    base64DecoderStream = (BASE64DecoderStream) ((MimeBodyPart) bodyPart).getContent();
                                } catch (ClassCastException e) {
                                    continue;
                                }

                                String content;
                                if (base64DecoderStream != null) {
                                    content = readContentFromBASE64DecoderStream(base64DecoderStream);
                                } else {
                                    DataHandler handler = bodyPart.getDataHandler();
                                    content = handler.getContent().toString();
                                }
                                checkEmail = CheckEmail.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, HUR_TYPE);

                                if (checkEmail) {
                                    resultImport = ImportEmailHur.importData(content, ipDb, user, password, tableImport);
                                    InsertEmail.insertEmail(resultImport, senderMail, USER_NAME, message.getSubject(), fileName, HUR_TYPE);
//                                    targetFolder.appendMessages(new Message[]{message});
                                }
                            }
//                            // email canh bao tap missing
//                            if (isTap) {
//                                String ipDb = lstTapMailConfig.get(4).toString();
//                                String user = lstTapMailConfig.get(5).toString();
//                                String password = lstTapMailConfig.get(6).toString();
//                                String tableImport = lstTapMailConfig.get(7).toString();
//                                String attachmentContent = "";
//                                // Lấy InputStream của đối tượng BodyPart và giải mã BASE64 nếu cần
//                                BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
//                                StringBuilder stringBuilder = new StringBuilder();
//                                int bufferSize;
//                                byte[] buffer = new byte[8 * 1024];
//                                while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
//                                    stringBuilder.append(new String(buffer, 0, bufferSize));
//                                }
//                                attachmentContent = stringBuilder.toString();
//                                checkEmail = CheckEmail.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, TAP_TYPE);
//                                if (checkEmail) {
//                                    resultImport = ImportEmailTap.importData(attachmentContent, ipDb, user, password, tableImport);
//                                    InsertEmail.insertEmail(resultImport, senderMail, USER_NAME, message.getSubject(), fileName, TAP_TYPE);
////                                    targetFolder.appendMessages(new Message[]{message});
//                                }
//                            }
//                            // email canh bao rap file
//                            if (isRap) {
//                                String ipDb = lstRapMailConfig.get(4).toString();
//                                String user = lstRapMailConfig.get(5).toString();
//                                String password = lstRapMailConfig.get(6).toString();
//                                String tableImport = lstRapMailConfig.get(7).toString();
//                                String attachmentContent = "";
//                                // Lấy InputStream của đối tượng BodyPart và giải mã BASE64 nếu cần
//                                BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
//                                StringBuilder stringBuilder = new StringBuilder();
//                                int bufferSize;
//                                byte[] buffer = new byte[8 * 1024];
//                                while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
//                                    stringBuilder.append(new String(buffer, 0, bufferSize));
//                                }
//                                attachmentContent = stringBuilder.toString();
//                                checkEmail = CheckEmail.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, RAP_TYPE);
//                                if (checkEmail) {
//                                    resultImport = ImportEmailRapFile.importData(attachmentContent, ipDb, user, password, tableImport);
//                                    InsertEmail.insertEmail(resultImport, senderMail, USER_NAME, message.getSubject(), fileName, RAP_TYPE);
////                                    targetFolder.appendMessages(new Message[]{message});
//                                }
//                            }
//                            // email canh bao dfd
//                            if (isDfd) {
//                                String ipDb = lstDfdMailConfig.get(4).toString();
//                                String user = lstDfdMailConfig.get(5).toString();
//                                String password = lstDfdMailConfig.get(6).toString();
//                                String tableImport = lstDfdMailConfig.get(7).toString();
//                                String attachmentContent = "";
//                                // Lấy InputStream của đối tượng BodyPart và giải mã BASE64 nếu cần
//                                BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
//                                StringBuilder stringBuilder = new StringBuilder();
//                                int bufferSize;
//                                byte[] buffer = new byte[8 * 1024];
//                                while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
//                                    stringBuilder.append(new String(buffer, 0, bufferSize));
//                                }
//                                attachmentContent = stringBuilder.toString();
//                                checkEmail = CheckEmail.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, DFD_TYPE);
//                                if (checkEmail) {
//                                    resultImport = ImportEmailDfd.importData(attachmentContent, ipDb, user, password, tableImport);
//                                    InsertEmail.insertEmail(resultImport, senderMail, USER_NAME, message.getSubject(), fileName, DFD_TYPE);
////                                    targetFolder.appendMessages(new Message[]{message});
//                                }
//                            }
//
//                            // email canh bao missing config
//                            if (isMcl) {
//                                String ipDb = lstMclMailConfig.get(4).toString();
//                                String user = lstMclMailConfig.get(5).toString();
//                                String password = lstMclMailConfig.get(6).toString();
//                                String tableImport = lstMclMailConfig.get(7).toString();
//                                String xlsContent = "";
//                                InputStream is = bodyPart.getInputStream();
//                                ByteArrayOutputStream output = new ByteArrayOutputStream();
//                                byte[] buffer = new byte[1024];
//                                int n = 0;
//                                while ((n = is.read(buffer)) != -1) {
//                                    output.write(buffer, 0, n);
//                                }
//                                xlsContent = output.toString();
//                                checkEmail = CheckEmail.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, MCL_TYPE);
//                                if (checkEmail) {
//                                    resultImport = ImportEmailMissingConfig.importData(xlsContent, ipDb, user, password, tableImport);
//                                    InsertEmail.insertEmail(resultImport, senderMail, USER_NAME, message.getSubject(), fileName, MCL_TYPE);
////                                    targetFolder.appendMessages(new Message[]{message});
//                                }
//                            }
                        }
                    }
                }
            }
            folder.close(false);
            store.close();
        } catch (Exception e) {
            logger.error("import data fail \n" + e);
        }
    }

}
