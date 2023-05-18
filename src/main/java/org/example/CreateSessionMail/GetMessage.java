package org.example.CreateSessionMail;

import javax.mail.*;
import java.util.Properties;

public class GetMessage {
    public static Message[] getMessageFromInboxFolder(String username, String password, String host, int port) throws Exception {
        Message[] messages = null;
        try {

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
            messages = inbox.getMessages();
        } catch (Exception ex){

        }

        return messages;
    }
}
