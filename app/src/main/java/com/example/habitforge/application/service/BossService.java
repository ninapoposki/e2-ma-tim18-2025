package com.example.habitforge.application.service;

import com.example.habitforge.application.model.Boss;

public class BossService {

    public Boss createBoss(int level) {
        int maxHP = calculateBossHP(level);
        return new Boss(level, maxHP, maxHP);
    }

    public int calculateBossHP(int level) {
        if (level == 1) return 200;
        int prevHP = calculateBossHP(level - 1);
        return prevHP * 2 + prevHP / 2;
    }
}
