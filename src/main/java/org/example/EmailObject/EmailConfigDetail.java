package org.example.EmailObject;

public class EmailConfigDetail {
    private Long emailConfigDetailId;
    private Long typeEmailId; //emailConfigId EmailConfig
    private String field;
    private String type;
    private int seqInFile;
    private String columnImport;
    private String nameConfig;

    public Long getEmailConfigDetailId() {
        return emailConfigDetailId;
    }

    public void setEmailConfigDetailId(Long emailConfigDetailId) {
        this.emailConfigDetailId = emailConfigDetailId;
    }

    public Long getTypeEmailId() {
        return typeEmailId;
    }

    public void setTypeEmailId(Long typeEmailId) {
        this.typeEmailId = typeEmailId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSeqInFile() {
        return seqInFile;
    }

    public void setSeqInFile(int seqInFile) {
        this.seqInFile = seqInFile;
    }

    public String getColumnImport() {
        return columnImport;
    }

    public void setColumnImport(String columnImport) {
        this.columnImport = columnImport;
    }

    public String getNameConfig() {
        return nameConfig;
    }

    public void setNameConfig(String nameConfig) {
        this.nameConfig = nameConfig;
    }
}
