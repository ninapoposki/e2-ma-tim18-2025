package com.example.habitforge.application.model;

import java.util.HashMap;
import java.util.Map;

public class AllianceMission {
    private String id;
    private String allianceId;
    private String name;
    private String description;
    private boolean active;
    private boolean completed;
    private long startTime;
    private long endTime;
    private int bossHP;
    private int memberCount;
    private Map<String, Integer> progress = new HashMap<>(); // userId → doprinos (HP damage)
    private Map<String, Integer> taskCount; // novi map za broj taskova po korisniku



    public AllianceMission() {}

    public AllianceMission(String allianceId, String name, String description) {
        this.allianceId = allianceId;
        this.name = name;
        this.description = description;
        this.active = true;
        this.completed = false;
        this.startTime = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public int getBossHP() { return bossHP; }
    public void setBossHP(int bossHP) { this.bossHP = bossHP; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public Map<String, Integer> getProgress() { return progress; }
    public void setProgress(Map<String, Integer> progress) { this.progress = progress; }
    public Map<String, Integer> getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Map<String, Integer> taskCount) {
        this.taskCount = taskCount;
    }
}
