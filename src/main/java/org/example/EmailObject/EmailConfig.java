package org.example.EmailObject;

public class EmailConfig {
    private Long emailConfigId;
    private String typeName;
    private String partnerCode;
    private String senderMail;
    private String senderSelector;
    private String patternAttachment;
    private String patternSelector;
    private String attachFileType;
    private String mailSubject;
    private String mailSubjectSelector;
    private String ipDb;
    private String userPasswordDb;
    private String tns;

    public Long getEmailConfigId() {
        return emailConfigId;
    }

    public void setEmailConfigId(Long emailConfigId) {
        this.emailConfigId = emailConfigId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getSenderMail() {
        return senderMail;
    }

    public void setSenderMail(String senderMail) {
        this.senderMail = senderMail;
    }

    public String getSenderSelector() {
        return senderSelector;
    }

    public void setSenderSelector(String senderSelector) {
        this.senderSelector = senderSelector;
    }

    public String getPatternAttachment() {
        return patternAttachment;
    }

    public void setPatternAttachment(String patternAttachment) {
        this.patternAttachment = patternAttachment;
    }

    public String getPatternSelector() {
        return patternSelector;
    }

    public void setPatternSelector(String patternSelector) {
        this.patternSelector = patternSelector;
    }

    public String getAttachFileType() {
        return attachFileType;
    }

    public void setAttachFileType(String attachFileType) {
        this.attachFileType = attachFileType;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailSubjectSelector() {
        return mailSubjectSelector;
    }

    public void setMailSubjectSelector(String mailSubjectSelector) {
        this.mailSubjectSelector = mailSubjectSelector;
    }

    public String getIpDb() {
        return ipDb;
    }

    public void setIpDb(String ipDb) {
        this.ipDb = ipDb;
    }

    public String getUserPasswordDb() {
        return userPasswordDb;
    }

    public void setUserPasswordDb(String userPasswordDb) {
        this.userPasswordDb = userPasswordDb;
    }

    public String getTns() {
        return tns;
    }

    public void setTns(String tns) {
        this.tns = tns;
    }
}
