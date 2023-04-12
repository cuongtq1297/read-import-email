package org.example;

import com.sun.mail.util.BASE64DecoderStream;

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
    private static String USER_NAME;
    private static String PASSWORD;
    private static String HOST;
    private static String PORT;
    public static final String CONFIG_FILE_PATH =  "config/email-infor.cfg";
    static {
        Properties properties = new Properties();
        FileInputStream propsFile = null;
        try{
            propsFile = new FileInputStream(CONFIG_FILE_PATH);
            properties.load(propsFile);
            USER_NAME = properties.getProperty("USERNAME");
            PASSWORD = properties.getProperty("PASSWORD");
            HOST = properties.getProperty("HOST");
            PORT = properties.getProperty("PORT");
        } catch (Exception e){
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

    public static void saveBASE64DecoderStreamContentToFile(BASE64DecoderStream base64DecoderStream, String fileName) throws IOException {
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        int len;
        while ((len = base64DecoderStream.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.close();
    }
    public static void EmailProcess(Boolean validToRun){
        if (validToRun){
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);

            // Tạo một đối tượng Session để tương tác với mail server
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(USER_NAME, PASSWORD);
                        }
                    });
            try {
                // Kết nối tới Store và hiển thị các Message trong Inbox
                Store store = session.getStore("imaps");
                store.connect("imap.gmail.com", USER_NAME, PASSWORD);
                Folder folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages();
                System.out.println("Có " + messages.length + " thư từ trong INBOX");
                for (int i = 0; i < messages.length; i++) {
                    Message message = messages[i];
//                System.out.println("---------------------------------");
//                System.out.println("Số thứ tự: " + (i + 1));
//                System.out.println("Tiêu đề: " + message.getSubject());
//                System.out.println("Người gửi: " + message.getFrom()[0]);
//                System.out.println("Nội dung: " + message.getContent().toString());

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
                                if (fileName.contains("HUR") && fileName.endsWith(".csv")) {
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
                                    ImportEmailHur.importData(content);
                                }
                                if (fileName.endsWith(".txt")) {
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
                                    // email canh bao missing tap file
                                    if(attachmentContent.contains("TAP IN pending")){

                                    }
                                    // email canh bao rap file
                                    if(attachmentContent.contains("RAP IN")){
                                        ImportEmailRapFile.importData(attachmentContent);
                                    }
                                    // email canh bao dfdr
                                    if(attachmentContent.contains("DAILY FILE DELIVERY REPORT")){
                                        ImportEmailDfd.importData(attachmentContent);
                                    }
                                }
                                // email canh bao missing config
                                if (fileName.endsWith(".xls")&& fileName.contains("missing_configuration_list")) {
                                    String xlsContent = "";
                                    InputStream is = bodyPart.getInputStream();
                                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    int n = 0;
                                    while ((n = is.read(buffer)) != -1) {
                                        output.write(buffer, 0, n);
                                    }
                                    xlsContent = output.toString();
//                                ImportEmailMissingConfig.importData(xlsContent);
                                }
                            }
                        }
                    }
                }
                folder.close(false);
                store.close();
            } catch (MessagingException | IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws SQLException {
        Boolean invalidToRun = CheckValidToRun.checkValidToRun();
        EmailProcess(invalidToRun);
    }
}
