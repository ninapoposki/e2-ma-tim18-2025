package com.example.habitforge.application.model;


public class AllianceInvite {
    private String id;
    private String fromUserId; // vođa saveza
    private String toUserId;   // korisnik koji dobija poziv
    private String allianceId;
    private String status; // pending / accepted / declined

    public AllianceInvite() {} // za Firestore

    public AllianceInvite(String fromUserId, String toUserId, String allianceId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.allianceId = allianceId;
        this.status = "pending";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
