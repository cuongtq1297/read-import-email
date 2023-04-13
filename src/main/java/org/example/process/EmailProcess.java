package org.example.process;

import com.sun.mail.util.BASE64DecoderStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.import_data.*;
import org.example.insert_email_infor.InsertEmailInfor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
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
    private static String HUR_ATTACHMENT_NAME;
    private static String TAP_ATTACHMENT_NAME;
    private static String RAP_ATTACHMENT_NAME;
    private static String DFD_ATTACHMENT_NAME;
    private static String MCL_ATTACHMENT_NAME;

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

            HUR_ATTACHMENT_NAME = properties.getProperty("HUR_ATTACHMENT_NAME");
            TAP_ATTACHMENT_NAME = properties.getProperty("TAP_ATTACHMENT_NAME");
            RAP_ATTACHMENT_NAME = properties.getProperty("RAP_ATTACHMENT_NAME");
            DFD_ATTACHMENT_NAME = properties.getProperty("DFD_ATTACHMENT_NAME");
            MCL_ATTACHMENT_NAME = properties.getProperty("MCL_ATTACHMENT_NAME");
        } catch (Exception e) {
            e.getMessage();
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
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", USER_NAME, PASSWORD);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            System.out.println("Có " + messages.length + " thư từ trong INBOX");
            for (int i = 0; i < messages.length; i++) {
                boolean checkEmail = false;
                Message message = messages[i];

                // xử lý string sender mail
                int startIdx = message.getFrom()[0].toString().indexOf("<") + 1;
                int endIdx = message.getFrom()[0].toString().indexOf(">");
                String senderMail = message.getFrom()[0].toString().substring(startIdx, endIdx);

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

                            // email canh bao hur
                            if (fileName.contains(HUR_ATTACHMENT_NAME) && fileName.endsWith(".csv")) {
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
                                checkEmail = InsertEmailInfor.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, HUR_TYPE);
                                if (checkEmail) {
                                    ImportEmailHur.importData(content);
                                }
                            }
                            // email canh bao tap missing
                            if (fileName.endsWith(".txt") && fileName.contains(TAP_ATTACHMENT_NAME)) {
                                String attachmentContent = "";
                                // Lấy InputStream của đối tượng BodyPart và giải mã BASE64 nếu cần
                                BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
                                StringBuilder stringBuilder = new StringBuilder();
                                int bufferSize;
                                byte[] buffer = new byte[8 * 1024];
                                while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
                                    stringBuilder.append(new String(buffer, 0, bufferSize));
                                }
                                attachmentContent = stringBuilder.toString();
                                checkEmail = InsertEmailInfor.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, TAP_TYPE);
                                if (checkEmail) {
                                    ImportEmailTap.importData(attachmentContent);
                                }
                            }
                            // email canh bao rap file
                            if (fileName.endsWith(".txt") && fileName.contains(RAP_ATTACHMENT_NAME)) {
                                String attachmentContent = "";
                                // Lấy InputStream của đối tượng BodyPart và giải mã BASE64 nếu cần
                                BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
                                StringBuilder stringBuilder = new StringBuilder();
                                int bufferSize;
                                byte[] buffer = new byte[8 * 1024];
                                while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
                                    stringBuilder.append(new String(buffer, 0, bufferSize));
                                }
                                attachmentContent = stringBuilder.toString();
                                checkEmail = InsertEmailInfor.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, RAP_TYPE);
                                if (checkEmail) {
                                    ImportEmailRapFile.importData(attachmentContent);
                                }
                            }
                            // email canh bao dfd
                            if (fileName.endsWith(".txt") && fileName.contains(DFD_ATTACHMENT_NAME)) {
                                String attachmentContent = "";
                                // Lấy InputStream của đối tượng BodyPart và giải mã BASE64 nếu cần
                                BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getInputStream();
                                StringBuilder stringBuilder = new StringBuilder();
                                int bufferSize;
                                byte[] buffer = new byte[8 * 1024];
                                while ((bufferSize = base64DecoderStream.read(buffer)) != -1) {
                                    stringBuilder.append(new String(buffer, 0, bufferSize));
                                }
                                attachmentContent = stringBuilder.toString();
                                checkEmail = InsertEmailInfor.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, DFD_TYPE);
                                if (checkEmail) {
                                    ImportEmailDfd.importData(attachmentContent);
                                }
                            }

                            // email canh bao missing config
                            if (fileName.endsWith(".xls") && fileName.contains(MCL_ATTACHMENT_NAME)) {
                                String xlsContent = "";
                                InputStream is = bodyPart.getInputStream();
                                ByteArrayOutputStream output = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int n = 0;
                                while ((n = is.read(buffer)) != -1) {
                                    output.write(buffer, 0, n);
                                }
                                xlsContent = output.toString();
                                checkEmail = InsertEmailInfor.insertEmailInfor(senderMail, USER_NAME, message.getSubject(), fileName, MCL_TYPE);
                                if (checkEmail) {
                                    ImportEmailMissingConfig.importData(xlsContent);
                                }
                            }
                        }
                    }
                }
            }
            folder.close(false);
            store.close();
        } catch (MessagingException | IOException | SQLException e) {
            logger.error("import data fail " + e);
        }
    }

}
