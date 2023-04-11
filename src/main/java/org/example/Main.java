package org.example;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class Main {
    public static void main(String[] args) throws MessagingException, IOException {
        final String username = "cuongtq1297@gmail.com";
        final String password = "bfuygcnkrisgrdus";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Tạo một đối tượng Session để tương tác với mail server
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = null;
        try {
            // Kết nối tới Store và hiển thị các Message trong Inbox
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", username, password);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            System.out.println("Có " + messages.length + " thư từ trong INBOX");
            for (int i = 0; i < messages.length; i++) {
                message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Số thứ tự: " + (i + 1));
                System.out.println("Tiêu đề: " + message.getSubject());
                System.out.println("Người gửi: " + message.getFrom()[0]);
                System.out.println("Nội dung: " + message.getContent().toString());
                Object content = message.getContent();
                // lấy toàn bộ phần đính kèm của một tin nhắn email
                Multipart multipart = (Multipart) message.getContent();
                // đếm số phần đính kèm có trong email
                int numberOfParts = multipart.getCount();
                // duyệt qua từng phần đính kèm
                for (int i1 = 0; i1 < numberOfParts; i1++) {
                    MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i1);

                    // lấy tên của phần đính kèm
                    String fileName = part.getFileName();

                    // kiểm tra xem phần đính kèm có phải là một tệp không
                    if (fileName != null && (fileName.endsWith(".txt") || fileName.endsWith(".csv"))) {
                        // nếu là tệp txt hoặc csv, lấy nội dung và lưu vào biến chuỗi
                        String content1 = part.getContent().toString();
                        System.out.println("file dinh kem");
                        // xử lý chuỗi đã lấy được từ tệp tin ở đây
                        System.out.println(content1);
                    }
                }
            }
            folder.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

