package com.example.habitforge.application.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userId;        // Firebase UID
    private String email;
    private String username;      // korisničko ime (stalno)
    private String avatar;        // izabran avatar (stalno)
    private boolean isActive;     // aktivacija preko mejla

    // Profil podaci
    private int level;
    private String title;
    private int powerPoints;
    private int experiencePoints;
    private int coins;
    private List<String> badges;
    private List<UserEquipment> equipment = new ArrayList<>();

    private String qrCode;

    // --- KONSTRUKTORI ---
    public User() {}

    public User(String userId, String email, String username, String avatar) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.isActive = false;
        this.level = 1;
        this.title = "Beginner";
        this.powerPoints = 0;
        this.experiencePoints = 0;
        this.coins = 0;
    }

    public User(String userId, String email, String username, String avatar, Boolean isActive) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.isActive = isActive;
        this.level = 1;
        this.title = "Beginner";
        this.powerPoints = 0;
        this.experiencePoints = 0;
        this.coins = 0;
    }

    // --- GETTERI I SETTERI ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }

    public void setUsername(String username){ this.username = username;}

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPowerPoints() { return powerPoints; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }

    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int experiencePoints) { this.experiencePoints = experiencePoints; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }

    public List<UserEquipment> getEquipment() {
        if (equipment == null) {
            equipment = new ArrayList<>();
        }
        return equipment;
    }

    public void setEquipment(List<UserEquipment> equipment) {
        this.equipment = equipment;
    }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public List<UserEquipment> getActiveEquipment() {
        List<UserEquipment> activeList = new ArrayList<>();
        if (equipment != null) {
            for (UserEquipment item : equipment) {
                if (item.isActive()) {
                    activeList.add(item);
                }
            }
        }
        return activeList;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", level=" + level +
                ", title='" + title + '\'' +
                ", experiencePoints=" + experiencePoints +
                ", powerPoints=" + powerPoints +
                ", coins=" + coins +
                ", badges=" + badges +
                ", equipment=" + equipment  +
                '}';
    }

}
