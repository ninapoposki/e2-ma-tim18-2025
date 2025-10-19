package com.example.habitforge.application.util;

import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;

import java.util.HashMap;
import java.util.Map;

public class EnumMapper {

    private static final Map<TaskDifficulty, String> DIFFICULTY_MAP = new HashMap<>();
    private static final Map<TaskPriority, String> PRIORITY_MAP = new HashMap<>();

    static {
        // 🔹 TaskDifficulty
        DIFFICULTY_MAP.put(TaskDifficulty.VERY_EASY, "Very Easy");
        DIFFICULTY_MAP.put(TaskDifficulty.EASY, "Easy");
        DIFFICULTY_MAP.put(TaskDifficulty.HARD, "Hard");
        DIFFICULTY_MAP.put(TaskDifficulty.EXTREMELY_HARD, "Extremely Hard");

        // 🔹 TaskPriority
        PRIORITY_MAP.put(TaskPriority.NORMAL, "Normal");
        PRIORITY_MAP.put(TaskPriority.IMPORTANT, "Important");
        PRIORITY_MAP.put(TaskPriority.EXTREMELY_IMPORTANT, "Extremely Important");
        PRIORITY_MAP.put(TaskPriority.SPECIAL, "Special");
    }

    public static String getDifficultyLabel(TaskDifficulty difficulty) {
        return DIFFICULTY_MAP.getOrDefault(difficulty, "Unknown");
    }

    public static String getPriorityLabel(TaskPriority priority) {
        return PRIORITY_MAP.getOrDefault(priority, "Unknown");
    }



}
