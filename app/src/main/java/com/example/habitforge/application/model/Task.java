package com.example.habitforge.application.model;

import com.example.habitforge.application.model.enums.RecurrenceUnit;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;

public class Task {
    private String id;
    private String userId;
    private String name;
    private String description;

    private String categoryId;     // ID kategorije (npr. "health")
    //private String categoryColor;  // boja kategorije (npr. "#FF0000")

    private TaskType taskType;     // JEDNOKRATNI ili PONAVLJAJUCI
    private int recurringInterval; // npr. 1, 2, 3
    private RecurrenceUnit recurrenceUnit; // DAN ili NEDELJA
    private long recurringStart;   // millis timestamp
    private long recurringEnd;     // millis timestamp
    private long executionTime;    // vreme izvršenja

    private TaskDifficulty difficulty; // Veoma lak, Lak, Težak, Ekstremno težak
    private TaskPriority priority;     // Normalan, Važan, Ekstremno važan, Specijalan
    private int xp;                    // izračunato XP (difficulty + priority)

    private TaskStatus status;   // AKTIVAN, ZAVRSEN, OTKAZAN

    // --- KONSTRUKTOR ---
    public Task() {}

    // --- GETTERI I SETTERI ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

   // public String getCategoryColor() { return categoryColor; }
   // public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public int getRecurringInterval() { return recurringInterval; }
    public void setRecurringInterval(int recurringInterval) { this.recurringInterval = recurringInterval; }

    public RecurrenceUnit getRecurrenceUnit() { return recurrenceUnit; }
    public void setRecurrenceUnit(RecurrenceUnit recurrenceUnit) { this.recurrenceUnit = recurrenceUnit; }

    public long getRecurringStart() { return recurringStart; }
    public void setRecurringStart(long recurringStart) { this.recurringStart = recurringStart; }

    public long getRecurringEnd() { return recurringEnd; }
    public void setRecurringEnd(long recurringEnd) { this.recurringEnd = recurringEnd; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

    public TaskDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(TaskDifficulty difficulty) { this.difficulty = difficulty; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
