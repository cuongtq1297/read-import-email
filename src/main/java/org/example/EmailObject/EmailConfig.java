package org.example.EmailObject;

import java.util.List;

public class EmailConfig {
    private Long emailConfigId;
    private String partnerCode;
    private String typeName;
    private List<String> senderMail;
    private String senderSelector;
    private List<String> subjectMail;
    private String subjectSelector;
    private List<String> patternAttachment;
    private String patternSelector;
    private String attachFileType;

    public Long getEmailConfigId() {
        return emailConfigId;
    }

    public void setEmailConfigId(Long emailConfigId) {
        this.emailConfigId = emailConfigId;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<String> getSenderMail() {
        return senderMail;
    }

    public void setSenderMail(List<String> senderMail) {
        this.senderMail = senderMail;
    }

    public String getSenderSelector() {
        return senderSelector;
    }

    public void setSenderSelector(String senderSelector) {
        this.senderSelector = senderSelector;
    }

    public String getSubjectSelector() {
        return subjectSelector;
    }

    public void setSubjectSelector(String subjectSelector) {
        this.subjectSelector = subjectSelector;
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

    public List<String> getSubjectMail() {
        return subjectMail;
    }

    public void setSubjectMail(List<String> subjectMail) {
        this.subjectMail = subjectMail;
    }

    public List<String> getPatternAttachment() {
        return patternAttachment;
    }

    public void setPatternAttachment(List<String> patternAttachment) {
        this.patternAttachment = patternAttachment;
    }
}
