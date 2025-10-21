package com.example.habitforge.application.model;

import java.util.Date;

public class AllianceMessage {
    private String id;           // Firestore id poruke
    private String allianceId;   // ID saveza
    private String senderId;     // ID korisnika koji šalje
    private String senderName;   // Username pošiljaoca
    private String content;      // Tekst poruke
    private long timestamp;      // vreme u milisekundama

    public AllianceMessage() {} // Firestore zahteva prazni konstruktor

    public AllianceMessage(String allianceId, String senderId, String senderName, String content) {
        this.allianceId = allianceId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = new Date().getTime();
    }

    // Getteri i setteri
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
