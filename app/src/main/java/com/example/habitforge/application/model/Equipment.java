package com.example.habitforge.application.model;

import com.example.habitforge.application.model.enums.EquipmentType;

public class Equipment {
    private String id;
    private String name;
    private EquipmentType type; // enum umesto String
    private double bonus;
    private boolean permanent;
    private int duration; // broj borbi ako nije trajno
    private double priceMultiplier;
    private String image;
    private String description;

    public Equipment() {}

    public Equipment(String id, String name, EquipmentType type, double bonus, boolean permanent,
                     int duration, double priceMultiplier, String image, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.bonus = bonus;
        this.permanent = permanent;
        this.duration = duration;
        this.priceMultiplier = priceMultiplier;
        this.image = image;
        this.description = description;
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

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
