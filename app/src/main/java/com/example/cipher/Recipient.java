package com.example.cipher;

public class Recipient {
    private String name;
    private String status;
    private String email;
    private Long timestamp;
    private String userId;

    public Recipient(String name, String status, String email, Long timestamp, String userId) {
        this.name = name;
        this.status = status;
        this.email = email;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }
    public Long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
}
