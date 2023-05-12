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
            folder.open(Folder.READ_WRITE);
//            Folder targetFolder = store.getFolder("fail");
//            targetFolder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            Message[] failMessages = null;
            System.out.println("Reading email ..... ");
            System.out.println("có " + messages.length + " thư");
            for (int i = 0; i < messages.length; i++) {
                boolean checkEmail = false;
                Message message = messages[i];

                // xử lý string sender mail
                int startIdx = message.getFrom()[0].toString().indexOf("<") + 1;
                int endIdx = message.getFrom()[0].toString().indexOf(">");
                String senderMail = message.getFrom()[0].toString().substring(startIdx, endIdx);
                String subjectMail = message.getSubject();
                // xử lý string người nhận
                String receiverMail = "";
                if (message.getAllRecipients()[0].toString().contains("<")) {
                    int start = message.getAllRecipients()[0].toString().indexOf("<") + 1;
                    int end = message.getAllRecipients()[0].toString().indexOf(">");
                    receiverMail = message.getAllRecipients()[0].toString().substring(start, end);
                } else {
                    receiverMail = message.getAllRecipients()[0].toString();
                }

                String receivedDate = message.getReceivedDate().toString();
                // kiểm tra email đã xử lý hay chưa
                checkEmail = CheckEmail.check(senderMail, receiverMail, subjectMail, receivedDate);
                boolean resultImport = false;
                List<String> fileNames = new ArrayList<>();
                List<BodyPart> bodyParts = new ArrayList<>();
                if (checkEmail) {
                    // kiểm tra các phần của email có multipart hay là text
                    if (message.getContent() instanceof Multipart) {
                        Multipart multipart = (Multipart) message.getContent();
                        // duyệt qua từng phần của multipart

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

                        List isHur = FilterEmail.Filter(senderMail, subjectMail, fileNames, lstHurMailConfig);
                        List isTap = FilterEmail.Filter(senderMail, subjectMail, fileNames, lstTapMailConfig);
                        List isRap = FilterEmail.Filter(senderMail, subjectMail, fileNames, lstRapMailConfig);
                        List isDfd = FilterEmail.Filter(senderMail, subjectMail, fileNames, lstDfdMailConfig);
                        List isMcl = FilterEmail.Filter(senderMail, subjectMail, fileNames, lstMclMailConfig);
                        if (!isHur.isEmpty()) {
                            String ipDb = (String) ((ArrayList) isHur.get(0)).get(7);
                            String user = (String) ((ArrayList) isHur.get(0)).get(8);
                            String password = (String) ((ArrayList) isHur.get(0)).get(9);
                            String tableImport = (String) ((ArrayList) isHur.get(0)).get(10);
                            int typeId = (int) ((ArrayList) isHur.get(0)).get(11);

                            boolean insertPending = InsertEmail.insertEmailPending(senderMail, subjectMail, fileNameLst, typeId, receiverMail, receivedDate);
                            if (insertPending) {
                                for (BodyPart bodyPart : bodyParts) {
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
                                    resultImport = ImportEmailHur.importData(content, ipDb, user, password, tableImport, typeId);
                                    if (!resultImport) {
                                        break;
                                    }
                                }
                                if (resultImport) {
                                    InsertEmail.updateStatus(senderMail, subjectMail, fileNameLst);
                                }
                            }
                        } else if (!isTap.isEmpty()) {
                            String ipDb = (String) ((ArrayList) isTap.get(0)).get(7);
                            String user = (String) ((ArrayList) isTap.get(0)).get(8);
                            String password = (String) ((ArrayList) isTap.get(0)).get(9);
                            String tableImport = (String) ((ArrayList) isTap.get(0)).get(10);
                            int typeId = (int) ((ArrayList) isTap.get(0)).get(11);

                            boolean insertPending = InsertEmail.insertEmailPending(senderMail, subjectMail, fileNameLst, typeId, receiverMail, receivedDate);
                            if (insertPending) {
                                for (BodyPart bodyPart : bodyParts) {
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
                                    resultImport = ImportEmailTap.importData(attachmentContent, ipDb, user, password, tableImport, typeId);
                                    if (!resultImport) {
                                        break;
                                    }
                                }
                                if (resultImport) {
                                    InsertEmail.updateStatus(senderMail, subjectMail, fileNameLst);
                                }
                            }
                        } else if (!isRap.isEmpty()) {
                            String ipDb = (String) ((ArrayList) isRap.get(0)).get(7);
                            String user = (String) ((ArrayList) isRap.get(0)).get(8);
                            String password = (String) ((ArrayList) isRap.get(0)).get(9);
                            String tableImport = (String) ((ArrayList) isRap.get(0)).get(10);
                            int typeId = (int) ((ArrayList) isRap.get(0)).get(11);

                            boolean insertPending = InsertEmail.insertEmailPending(senderMail, subjectMail, fileNameLst, typeId, receiverMail, receivedDate);
                            if (insertPending) {
                                for (BodyPart bodyPart : bodyParts) {
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
                                    resultImport = ImportEmailRapFile.importData(attachmentContent, ipDb, user, password, tableImport, typeId);
                                    if (!resultImport) {
                                        break;
                                    }
                                }
                                if (resultImport) {
                                    InsertEmail.updateStatus(senderMail, subjectMail, fileNameLst);
                                }
                            }
                        } else if (!isDfd.isEmpty()) {
                            String ipDb = (String) ((ArrayList) isDfd.get(0)).get(7);
                            String user = (String) ((ArrayList) isDfd.get(0)).get(8);
                            String password = (String) ((ArrayList) isDfd.get(0)).get(9);
                            String tableImport = (String) ((ArrayList) isDfd.get(0)).get(10);
                            int typeId = (int) ((ArrayList) isDfd.get(0)).get(11);

                            boolean insertPending = InsertEmail.insertEmailPending(senderMail, subjectMail, fileNameLst, typeId, receiverMail, receivedDate);
                            if (insertPending) {
                                for (BodyPart bodyPart : bodyParts) {
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
                                    resultImport = ImportEmailDfd.importData(attachmentContent, ipDb, user, password, tableImport, typeId);
                                    if (!resultImport) {
                                        break;
                                    }
                                }
                                if (resultImport) {
                                    InsertEmail.updateStatus(senderMail, subjectMail, fileNameLst);
                                }
                            }
                        } else if (!isMcl.isEmpty()) {
                            String ipDb = (String) ((ArrayList) isMcl.get(0)).get(7);
                            String user = (String) ((ArrayList) isMcl.get(0)).get(8);
                            String password = (String) ((ArrayList) isMcl.get(0)).get(9);
                            String tableImport = (String) ((ArrayList) isMcl.get(0)).get(10);
                            int typeId = (int) ((ArrayList) isMcl.get(0)).get(11);
                            boolean insertPending = InsertEmail.insertEmailPending(senderMail, subjectMail, fileNameLst, typeId, receiverMail, receivedDate);
                            if (insertPending) {
                                for (BodyPart bodyPart : bodyParts) {
                                    String xlsContent = "";
                                    InputStream is = bodyPart.getInputStream();
                                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    int n = 0;
                                    while ((n = is.read(buffer)) != -1) {
                                        output.write(buffer, 0, n);
                                    }
                                    xlsContent = output.toString();
                                    resultImport = ImportEmailMissingConfig.importData(xlsContent, ipDb, user, password, tableImport, typeId);
                                    if (!resultImport) {
                                        break;
                                    }
                                }
                                if (resultImport) {
                                    InsertEmail.updateStatus(senderMail, subjectMail, fileNameLst);
                                }
                            }
                        } else {
                            InsertEmail.insertTypeNotDefineEmail(senderMail, subjectMail, fileNameLst, 0, receiverMail, receivedDate);
                        }
                    }
                }
            }
            folder.close();
//            targetFolder.close();
            store.close();
        } catch (Exception e) {
            logger.error("import data fail \n" + e);
        }
    }

}
