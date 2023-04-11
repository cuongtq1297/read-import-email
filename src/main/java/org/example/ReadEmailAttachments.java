package org.example;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class ReadEmailAttachments {
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
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        // Tạo một đối tượng Session để tương tác với mail server
        Session session = Session.getInstance(props,
                new Authenticator() {
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
                System.out.println("---------------------------------");
                System.out.println("Số thứ tự: " + (i + 1));
                System.out.println("Tiêu đề: " + message.getSubject());
                System.out.println("Người gửi: " + message.getFrom()[0]);
                System.out.println("Nội dung: " + message.getContent().toString());
                // kiểm tra các phần của email có multipart hay là text
                if (message.isMimeType("multipart/*")) {
                    Multipart multipart = (Multipart) message.getContent();
                    // duyệt qua từng phần của multipart
                    for (int j = 0; j < multipart.getCount(); j++) {
                        BodyPart bodyPart = multipart.getBodyPart(j);
                        // kiểm tra phần có phải là file đính kèm được gửi từ email hay không
                        if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase("attachment")) {
                            // kiểm tra định dạng file
                            String fileName = bodyPart.getFileName();
                            if (fileName.endsWith(".txt") || fileName.endsWith(".csv")) {
                                MimeBodyPart part = (MimeBodyPart) bodyPart;
                                String content = part.getContent().toString();
                                System.out.println("File đính kèm: " + fileName);
                                // xử lý chuỗi đã lấy được từ tệp tin
                                System.out.println("Nội dung file đính kèm: " + content);
                            }
                        }
                    }
                }
            }
            folder.close(false);
            store.close();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}

