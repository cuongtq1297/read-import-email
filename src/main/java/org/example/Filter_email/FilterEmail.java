package org.example.Filter_email;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FilterEmail {
    public static List<Object> Filter(String senderMail, String subjectMail, String fileName, List<List<Object>> lstMailConfig) {
        List<Object> listResult = new ArrayList<>();
        boolean checkSender = false;
        boolean checkSubject = false;
        boolean checkAttachmentName = false;
        boolean checkAttachmentType = false;
        for (List<Object> mailConfig : lstMailConfig) {

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
                if (mailConfig.get(4).toString().contains(subjectMail)) {
                    checkAttachmentName = true;
                }
            } else if (mailConfig.get(5).equals("like")) {
                String[] attachmentLst = mailConfig.get(4).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String attachmentLike : attachmentLst) {
                    if (fileName.contains(attachmentLike)) {
                        checkAttachmentName = true;
                    }
                }
            } else if (mailConfig.get(5).equals("regex")) {
                String[] attachmentLst = mailConfig.get(4).toString().replace(" ", "").replace("[", "").replace("]", "").split(",");
                for (String attachmentMailRegex : attachmentLst) {
                    if (Pattern.matches(attachmentMailRegex, senderMail)) {
                        checkAttachmentName = true;
                    }
                }
            }

            // check attachmentType
            if (fileName.endsWith(mailConfig.get(6).toString())) {
                checkAttachmentType = true;
            }
            if (checkSender && checkSubject && checkAttachmentName && checkAttachmentType) {
                listResult.add(0, mailConfig);
                break;
            }
        }
        return listResult;
    }
}
