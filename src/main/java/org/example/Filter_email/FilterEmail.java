package org.example.Filter_email;

import org.example.EmailObject.EmailConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FilterEmail {
    public static List<EmailConfig> Filter(String senderMail, String subjectMail, List<String> fileNames, List<List<EmailConfig>> lstMailConfig) {
        List<EmailConfig> listResult = new ArrayList<>();
        boolean checkSender = false;
        boolean checkSubject = false;
        boolean checkAttachmentName = false;
        boolean checkAttachmentType = false;
        for (List<EmailConfig> mailConfig : lstMailConfig) {

            // check sender mail
            if (mailConfig.get(1).equals("exactly")) {
                if (mailConfig.get(0).toString().contains(senderMail)) {
                    checkSender = true;
                }
            } else if (mailConfig.get(1).equals("like")) {
                String[] senderMailLikeLst = mailConfig.get(0).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String senderMailLike : senderMailLikeLst) {
                    if (senderMail.contains(senderMailLike)) {
                        checkSender = true;
                    }
                }
            } else if (mailConfig.get(1).equals("regex")) {
                String[] senderMailRegexLst = mailConfig.get(0).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String senderMailRegex : senderMailRegexLst) {
                    if (Pattern.matches(senderMailRegex, senderMail)) {
                        checkSender = true;
                    }
                }
            }

            // check subject
            if (mailConfig.get(3).equals("exactly")) {
                if (mailConfig.get(2).toString().contains(subjectMail)) {
                    checkSubject = true;
                }
            } else if (mailConfig.get(3).equals("like")) {
                String[] subjectMailLikeLst = mailConfig.get(2).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String subjectMailLike : subjectMailLikeLst) {
                    if (subjectMail.contains(subjectMailLike)) {
                        checkSubject = true;
                    }
                }
            } else if (mailConfig.get(3).equals("regex")) {
                String[] subjectMailRegexLst = mailConfig.get(2).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String subjectMailRegex : subjectMailRegexLst) {
                    if (Pattern.matches(subjectMailRegex, senderMail)) {
                        checkSubject = true;
                    }
                }
            }

            // check attachmentName
            if (mailConfig.get(5).equals("exactly")) {
                for (int i = 0; i < fileNames.size(); i++) {
                    if (mailConfig.get(4).toString().contains(fileNames.get(i))) {
                        checkAttachmentName = true;
                    } else {
                        checkAttachmentName = false;
                        break;
                    }
                }
            } else if (mailConfig.get(5).equals("like")) {
                String[] attachmentLst = mailConfig.get(4).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String attachmentLike : attachmentLst) {
                    if (fileNames.toString().contains(attachmentLike)) {
                        checkAttachmentName = true;
                    } else {
                        checkAttachmentName = false;
                        break;
                    }
                }
            } else if (mailConfig.get(5).equals("regex")) {
                String[] attachmentLst = mailConfig.get(4).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String attachmentMailRegex : attachmentLst) {
                    if (Pattern.matches(attachmentMailRegex, senderMail)) {
                        checkAttachmentName = true;
                    } else {
                        checkAttachmentName = false;
                        break;
                    }
                }
            }

            // check attachmentType
            for (String fileName : fileNames) {
                if (fileName.endsWith(mailConfig.get(6).toString())) {
                    checkAttachmentType = true;
                } else {
                    checkAttachmentType = false;
                    break;
                }
            }

            if (checkSender && checkSubject && checkAttachmentName && checkAttachmentType) {
                listResult.add(0, null);
                break;
            }
        }
        return listResult;
    }
    public static EmailConfig checkSenderSubject(String senderMail, String subjectMail, List<EmailConfig> lst) {
        EmailConfig emailConfigRs = new EmailConfig();
        boolean checkSender = false;
        boolean checkSubject = false;
        for (EmailConfig emailConfig : lst) {
            if (emailConfig.getSenderSelector().equals("exactly")) {
                if (emailConfig.getSenderMail().contains(senderMail)) {
                    checkSender = true;
                }
            } else if (emailConfig.getSenderSelector().equals("like")) {
                for (int i = 0; i < emailConfig.getSenderMail().size(); i++) {
                    if (senderMail.contains(emailConfig.getSenderMail().get(i))) {
                        checkSender = true;
                    }
                }
            } else if (emailConfig.getSenderSelector().equals("like")) {
                for (int i = 0; i < emailConfig.getSenderMail().size(); i++) {
                    if (Pattern.matches(emailConfig.getSenderMail().get(i), senderMail)) {
                        checkSender = true;
                    }
                }
            }
            if (emailConfig.getSubjectSelector().equals("exactly")) {
                if(emailConfig.getSubjectMail().contains(subjectMail)){
                    checkSubject = true;
                }
            } else if (emailConfig.getSubjectSelector().equals("like")) {
                for (int i = 0; i < emailConfig.getSubjectMail().size(); i++) {
                    if (subjectMail.contains(emailConfig.getSubjectMail().get(i))) {
                        checkSubject = true;
                    }
                }
            } else if (emailConfig.getSubjectSelector().equals("regex")) {
                for (int i = 0; i < emailConfig.getSubjectMail().size(); i++) {
                    if (Pattern.matches(emailConfig.getSubjectMail().get(i), subjectMail)) {
                        checkSubject = true;
                    }
                }
            }
            if (checkSender && checkSubject) {
                emailConfigRs = emailConfig;
                break;
            }
        }
        return emailConfigRs;
    }

    public static boolean checkAttachment(List<String> attachment, EmailConfig emailConfig) {
        boolean checkResult = false;
        if (emailConfig.getPatternSelector().equals("exactly")) {
            for (String att : attachment) {
                if (emailConfig.getPatternAttachment().contains(att)) {
                    checkResult = true;
                } else {
                    checkResult = false;
                    break;
                }
            }
        } else if (emailConfig.getPatternSelector().equals("like")) {
            for (String att : attachment) {
                for (int i = 0; i < emailConfig.getPatternAttachment().size(); i++) {
                    if (att.contains(emailConfig.getPatternAttachment().get(i))) {
                        checkResult = true;
                    }
                }
                if (!checkResult) {
                    break;
                }
            }
        } else if (emailConfig.getPatternSelector().equals("regex")) {
            for (String att : attachment) {
                for (int i = 0; i < emailConfig.getPatternAttachment().size(); i++) {
                    if (Pattern.matches(emailConfig.getPatternAttachment().get(i), att)) {
                        checkResult = true;
                    }
                }
                if (!checkResult) {
                    break;
                }
            }
        }

        return checkResult;
    }
}
