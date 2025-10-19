package com.example.habitforge.application.model;


import java.util.ArrayList;
import java.util.List;

public class Alliance {
    private String id;
    private String name;
    private String leaderId; // korisnik koji je kreirao savez
    private List<String> memberIds = new ArrayList<>();
    private boolean missionStarted = false; // da li je misija aktivna

    public Alliance() {
    } // za Firestore

    public Alliance(String name, String leaderId) {
        this.name = name;
        this.leaderId = leaderId;
        this.memberIds.add(leaderId); // vođa je član
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public boolean isMissionStarted() {
        return missionStarted;
    }

    public void setMissionStarted(boolean missionStarted) {
        this.missionStarted = missionStarted;
    }
}
