package org.example.Process;

import com.sun.mail.pop3.POP3Message;

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

public class Test {

    public static void main(String[] args) throws Exception {

        // Server mail POP3
        String host = "pop.gmail.com";
        int port = 995;

        // Thông tin đăng nhập tài khoản mail
        String username = "cuongtq2602@gmail.com";
        String password = "lfayytxmvnkxatlv";

        Properties properties = new Properties();
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
        properties.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // Tạo session mail
        Session session = Session.getDefaultInstance(properties);

        // Kết nối tới hộp thư mail
        Store store = session.getStore("pop3");
        store.connect(host, username, password);

        // Đọc thư mục inbox
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // Đọc các email trong inbox
        Message[] messages = inbox.getMessages();
        System.out.println("Tổng số email trong hộp thư: " + messages.length);
        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            System.out.println("Email số " + (i + 1));
            System.out.println("Tiêu đề: " + message.getSubject());
            System.out.println("Người gửi: " + message.getFrom()[0]);
            System.out.println("Nội dung: " + message.getContent().toString());
            System.out.println("messageID : " + ((POP3Message) message).getMessageID());
        }

        // Đóng kết nối
        inbox.close(false);
        store.close();

    }
}

