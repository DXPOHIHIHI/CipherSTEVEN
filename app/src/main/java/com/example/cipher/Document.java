package com.example.cipher;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private String documentName;
    private String title;
    private String senderName;
    private String senderEmail;
    private Long senderTimestamp;
    private String senderStatus;
    private String senderUserId;
    private List<Recipient> recipients;

    public Document(String documentName, String title, String senderName, String senderEmail, Long senderTimestamp, String senderStatus, String senderUserId, List<Recipient> recipients) {
        this.documentName = documentName;
        this.title = title;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderTimestamp = senderTimestamp;
        this.senderStatus = senderStatus;
        this.senderUserId = senderUserId;
        this.recipients = recipients != null ? recipients : new ArrayList<>();
    }

    // Getters
    public String getDocumentName() { return documentName; }
    public String getTitle() { return title; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public Long getSenderTimestamp() { return senderTimestamp; }
    public String getSenderStatus() { return senderStatus; }
    public String getSenderUserId() { return senderUserId; }
    public List<Recipient> getRecipients() { return recipients; }
}


