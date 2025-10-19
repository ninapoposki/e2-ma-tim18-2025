package com.example.habitforge.application.model;


public class FriendRequest {
    private String id;          // Auto generisano Firestore ID
    private String fromUserId;  // ko šalje zahtev
    private String toUserId;    // kome je zahtev upućen
    private long timestamp;     // kada je poslat

    public FriendRequest() {}

    public FriendRequest(String fromUserId, String toUserId, long timestamp) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
