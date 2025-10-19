package com.example.habitforge.application.model;

import com.example.habitforge.application.model.enums.EquipmentType;

public class UserEquipment {
    private String id; // id predmeta (npr. potion_20pp, gloves_10)
    private String equipmentId;
    private EquipmentType type; // POTION, CLOTHING, WEAPON

    // Polja koja se koriste po tipu predmeta
    private boolean active; // da li je aktiviran
    private int duration; // za odeću (broj borbi)
    private double effect; // % pojačanja (odeća, napitak, weapon)
    private boolean usedInNextBossFight; // jednokratni napitci

    private int level; // samo za oružje

    // Prazni konstruktor za Firestore
    public UserEquipment() {}

    // Konstruktor za napitke
    public UserEquipment(String id, String equipmentId, EquipmentType type) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.type = type;
        this.effect = 0;
        this.active = false;
        this.usedInNextBossFight = false;
        this.duration = 0;
        this.level = 0;
    }

    // Konstruktor za odeću
    public UserEquipment(String id, String equipmentId, EquipmentType type, double effect, int duration) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.type = type;
        this.effect = effect;
        this.duration = duration;
        this.active = false;
        this.usedInNextBossFight = false;
        this.level = 0;
    }

    // Konstruktor za oružje
    public UserEquipment(String equipmentId, EquipmentType type, double effect) {
        this.id = id;
        this.equipmentId  = equipmentId;
        this.type = type;
        this.effect = effect;
        this.level = 1;
        this.active = false;
        this.duration = 0;
        this.usedInNextBossFight = false;
    }

    // --- GETTERI I SETTERI ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getEffect() { return effect; }
    public void setEffect(double effect) { this.effect = effect; }

    public boolean isUsedInNextBossFight() { return usedInNextBossFight; }
    public void setUsedInNextBossFight(boolean usedInNextBossFight) { this.usedInNextBossFight = usedInNextBossFight; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

//    public int getPowerBonus() {
//        // ako je efekat izrazen ako procenat(npr. 0.1 = +10%)
//        // Za jednostavno testiranje možeš vratiti efekat u integer obliku:
//        return (int) (effect * 100);
//    }

    public int getPowerBonus() {
        return (int) (effect * 100);
    }




}
