package org.example.Process;

import com.sun.mail.util.BASE64DecoderStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.EmailAccount.GetEmailAccount;
import org.example.Get_config.GetEmailConfig;
import org.example.Insert_email_infor.CheckEmail;

import javax.mail.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HurEmailProcess {
    private static final Logger logger = LogManager.getLogger(HurEmailProcess.class);

    public static String readContentFromBASE64DecoderStream(BASE64DecoderStream base64DecoderStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(base64DecoderStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

//    public static void HurEmailProcess() {
//        List<Object> lstAccount = GetEmailAccount.getAccount();
//        for (Object account : lstAccount) {
//            String username = ((ArrayList) account).get(0).toString();
//            String password = ((ArrayList) account).get(1).toString();
//            Properties props = new Properties();
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", "true");
//            props.put("mail.smtp.host", "smtp.gmail.com");
//            props.put("mail.smtp.port", "587");
//            Session session = Session.getInstance(props,
//                    new javax.mail.Authenticator() {
//                        protected PasswordAuthentication getPasswordAuthentication() {
//                            return new PasswordAuthentication(username, password);
//                        }
//                    });
//            try {
//                List<List<Object>> lstHurMailConfig = GetEmailConfig.getConfig("HUR");
//                Store store = session.getStore("imaps");
//                store.connect("imap.gmail.com", username, password);
//                Folder folder = store.getFolder("INBOX");
//                folder.open(Folder.READ_WRITE);
//                Message[] messages = folder.getMessages();
//                Message[] failMessages = null;
//                for (int i = 0; i < messages.length; i++) {
//                    boolean checkEmail = false;
//                    Message message = messages[i];
//                    // xử lý string sender mail
//                    int startIdx = message.getFrom()[0].toString().indexOf("<") + 1;
//                    int endIdx = message.getFrom()[0].toString().indexOf(">");
//                    String senderMail = message.getFrom()[0].toString().substring(startIdx, endIdx);
//                    String subjectMail = message.getSubject();
//                    // xử lý string người nhận
//                    String receiverMail = "";
//                    if (message.getAllRecipients()[0].toString().contains("<")) {
//                        int start = message.getAllRecipients()[0].toString().indexOf("<") + 1;
//                        int end = message.getAllRecipients()[0].toString().indexOf(">");
//                        receiverMail = message.getAllRecipients()[0].toString().substring(start, end);
//                    } else {
//                        receiverMail = message.getAllRecipients()[0].toString();
//                    }
//                    String receivedDate = message.getReceivedDate().toString();
//
//                    // kiểm tra email đã xử lý hay chưa
//                    checkEmail = CheckEmail.check(senderMail, receiverMail, subjectMail, receivedDate);
//                    boolean resultImport = false;
//                    List<String> fileNames = new ArrayList<>();
//                    List<BodyPart> bodyParts = new ArrayList<>();
//                    if (checkEmail) {
//
//                    }
//                }
//
//            } catch (Exception e){
//
//            }
//
//        }
//    }

//    public static void main(String[] args) {
//        HurEmailProcess();
//    }
}
