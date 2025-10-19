package com.example.habitforge.application.model;

import com.example.habitforge.application.model.enums.RecurrenceUnit;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String description;

    private String categoryId;
    //private String categoryColor;

    private TaskType taskType;     // one-time or multiple
    private int recurringInterval;
    private RecurrenceUnit recurrenceUnit; // day or week
    private long recurringStart;
    private long recurringEnd;
    private long executionTime;

    private TaskDifficulty difficulty;
    private TaskPriority priority;
    private int xp;                    // calculated xp (difficulty+priority)
    private boolean exceedsQuota;
    private long createdAt;


    private TaskStatus status;

    public Task() {}

    public Task(String id, String userId, String name, String description, String categoryId, TaskType taskType, int recurringInterval, long recurringStart, RecurrenceUnit recurrenceUnit, long recurringEnd, long executionTime, TaskDifficulty difficulty, TaskPriority priority, int xp, TaskStatus status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.taskType = taskType;
        this.recurringInterval = recurringInterval;
        this.recurringStart = recurringStart;
        this.recurrenceUnit = recurrenceUnit;
        this.recurringEnd = recurringEnd;
        this.executionTime = executionTime;
        this.difficulty = difficulty;
        this.priority = priority;
        this.xp = xp;
        this.status = status;
        this.createdAt = System.currentTimeMillis();

    }

    // --- getters and setters ---
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
    public void calculateXp(){
        int difficultyXp=(difficulty!=null)?difficulty.getXp():0;
        int priorityXp=(priority!=null)? priority.getXp() : 0;
        this.xp=difficultyXp+priorityXp;
    }

    public boolean isExceedsQuota() {
        return exceedsQuota;
    }

    public void setExceedsQuota(boolean exceedsQuota) {
        this.exceedsQuota = exceedsQuota;
    }
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt == 0 ? System.currentTimeMillis() : createdAt;
    }


}
