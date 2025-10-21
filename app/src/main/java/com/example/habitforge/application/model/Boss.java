package com.example.habitforge.application.model;

public class Boss {
    private int level;
    private int currentHP;
    private int maxHP;
    private boolean defeated;

    private int attemptsLeft;

    private int refreshedForLevel;



    public Boss() {
        this.level = 1;
        this.defeated = false;
        this.attemptsLeft = 5;
    }

    public Boss(int level, int currentHP, int maxHP) {
        this.level = level;
        this.currentHP = currentHP;
        this.maxHP = maxHP;
        this.defeated = false;
        this.attemptsLeft=5;
        this.refreshedForLevel=0;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getCurrentHP() { return currentHP; }
    public void setCurrentHP(int currentHP) { this.currentHP = currentHP; }

    public int getMaxHP() { return maxHP; }
    public void setMaxHP(int maxHP) { this.maxHP = maxHP; }

    public boolean isDefeated() { return defeated; }
    public void setDefeated(boolean defeated) { this.defeated = defeated; }

    public int getAttemptsLeft() { return attemptsLeft; }
    public void setAttemptsLeft(int attemptsLeft) { this.attemptsLeft = attemptsLeft; }


    public int getRefreshedForLevel() { return refreshedForLevel; }
    public void setRefreshedForLevel(int refreshedForLevel) { this.refreshedForLevel = refreshedForLevel; }


    public void takeDamage(int damage) {
        this.currentHP = Math.max(0, this.currentHP - damage);
        if (this.currentHP == 0) this.defeated = true;
    }
}
