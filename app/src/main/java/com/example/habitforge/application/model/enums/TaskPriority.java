package com.example.habitforge.application.model.enums;

public enum TaskPriority {
    NORMAL(1),
    IMPORTANT(3),
    EXTREMELY_IMPORTANT(10),
    SPECIAL(100);

    private final int xp;

    TaskPriority(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }
}
