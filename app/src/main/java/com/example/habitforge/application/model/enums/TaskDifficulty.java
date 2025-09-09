package com.example.habitforge.application.model.enums;

public enum TaskDifficulty {
    VERY_EASY(1),
    EASY(3),
    HARD(7),
    EXTREMELY_HARD(20);

    private final int xp;

    TaskDifficulty(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }
}

