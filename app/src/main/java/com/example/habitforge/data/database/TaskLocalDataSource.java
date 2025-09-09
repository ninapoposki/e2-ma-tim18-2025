package com.example.habitforge.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.model.enums.RecurrenceUnit;

import java.util.ArrayList;
import java.util.List;

public class TaskLocalDataSource {

    private final DatabaseHelper dbHelper;

    public TaskLocalDataSource(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // --- INSERT ---
    public void addTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("id", task.getId());
        cv.put("user_id", task.getUserId());
        cv.put("name", task.getName());
        cv.put("description", task.getDescription());
        cv.put("category_id", task.getCategoryId());
        cv.put("task_type", task.getTaskType() != null ? task.getTaskType().name() : null);
        cv.put("recurring_interval", task.getRecurringInterval());
        cv.put("recurrence_unit", task.getRecurrenceUnit() != null ? task.getRecurrenceUnit().name() : null);
        cv.put("recurring_start", task.getRecurringStart());
        cv.put("recurring_end", task.getRecurringEnd());
        cv.put("execution_time", task.getExecutionTime());
        cv.put("difficulty", task.getDifficulty() != null ? task.getDifficulty().name() : null);
        cv.put("priority", task.getPriority() != null ? task.getPriority().name() : null);
        cv.put("xp", task.getXp());
        cv.put("status", task.getStatus() != null ? task.getStatus().name() : null);

        db.insert(DatabaseHelper.T_TASKS, null, cv);
        db.close();
    }

    // --- UPDATE ---
    public void updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name", task.getName());
        cv.put("description", task.getDescription());
        cv.put("category_id", task.getCategoryId());
        cv.put("task_type", task.getTaskType() != null ? task.getTaskType().name() : null);
        cv.put("recurring_interval", task.getRecurringInterval());
        cv.put("recurrence_unit", task.getRecurrenceUnit() != null ? task.getRecurrenceUnit().name() : null);
        cv.put("recurring_start", task.getRecurringStart());
        cv.put("recurring_end", task.getRecurringEnd());
        cv.put("execution_time", task.getExecutionTime());
        cv.put("difficulty", task.getDifficulty() != null ? task.getDifficulty().name() : null);
        cv.put("priority", task.getPriority() != null ? task.getPriority().name() : null);
        cv.put("xp", task.getXp());
        cv.put("status", task.getStatus() != null ? task.getStatus().name() : null);

        db.update(DatabaseHelper.T_TASKS, cv, "id = ?", new String[]{task.getId()});
        db.close();
    }

    // --- DELETE ---
    public void deleteTask(String taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.T_TASKS, "id = ?", new String[]{taskId});
        db.close();
    }

    // --- GET BY ID ---
    public Task getTaskById(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_TASKS, null, "id = ?", new String[]{taskId}, null, null, null);
        Task task = null;
        if (c != null && c.moveToFirst()) {
            task = cursorToTask(c);
            c.close();
        }
        db.close();
        return task;
    }

    // --- GET ALL FOR USER ---
    public List<Task> getAllTasksForUser(String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_TASKS, null, "user_id = ?", new String[]{userId}, null, null, "execution_time ASC");

        if (c != null && c.moveToFirst()) {
            do {
                tasks.add(cursorToTask(c));
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return tasks;
    }

    // --- MAP CURSOR TO TASK ---
    private Task cursorToTask(Cursor c) {
        Task task = new Task();

        task.setId(c.getString(c.getColumnIndexOrThrow("id")));
        task.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        task.setName(c.getString(c.getColumnIndexOrThrow("name")));
        task.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        task.setCategoryId(c.getString(c.getColumnIndexOrThrow("category_id")));

        String taskType = c.getString(c.getColumnIndexOrThrow("task_type"));
        if (taskType != null) task.setTaskType(TaskType.valueOf(taskType));

        task.setRecurringInterval(c.getInt(c.getColumnIndexOrThrow("recurring_interval")));

        String recUnit = c.getString(c.getColumnIndexOrThrow("recurrence_unit"));
        if (recUnit != null) task.setRecurrenceUnit(RecurrenceUnit.valueOf(recUnit));

        task.setRecurringStart(c.getLong(c.getColumnIndexOrThrow("recurring_start")));
        task.setRecurringEnd(c.getLong(c.getColumnIndexOrThrow("recurring_end")));
        task.setExecutionTime(c.getLong(c.getColumnIndexOrThrow("execution_time")));

        String diff = c.getString(c.getColumnIndexOrThrow("difficulty"));
        if (diff != null) task.setDifficulty(TaskDifficulty.valueOf(diff));

        String prio = c.getString(c.getColumnIndexOrThrow("priority"));
        if (prio != null) task.setPriority(TaskPriority.valueOf(prio));

        task.setXp(c.getInt(c.getColumnIndexOrThrow("xp")));

        String status = c.getString(c.getColumnIndexOrThrow("status"));
        if (status != null) task.setStatus(TaskStatus.valueOf(status));

        return task;
    }


}
