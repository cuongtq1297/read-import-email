package org.example.Process;

import com.sun.mail.util.BASE64DecoderStream;
import org.example.CreateSessionMail.GetMessage;
import org.example.EmailAccount.GetEmailAccount;
import org.example.EmailObject.EmailAccount;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import java.util.ArrayList;
import java.util.List;

import static org.example.Process.EmailProcess.readContentFromBASE64DecoderStream;

public class TapEmailProcess {
    public static void TapEmailProcess() throws Exception{
        try {
            List<EmailAccount> accountList = GetEmailAccount.getAccount();
            for (EmailAccount account : accountList){
                Message[] messages = GetMessage.getMessageFromInboxFolder(account.getUserName(),account.getPassword());
                for (int i = 0; i < messages.length; i++) {
                    Message message = messages[i];
                    System.out.println("Email số " + (i+1));
                    System.out.println("Tiêu đề: " + message.getSubject());
                    System.out.println("Người gửi: " + message.getFrom()[0]);
                    System.out.println("Nội dung: " + message.getContent().toString());
                    System.out.println("Ngày gửi: " + message.getSentDate());
                }

            }
        } catch (Exception e){

        }
    }

    public static void main(String[] args) throws Exception {
        TapEmailProcess();
    }
}
