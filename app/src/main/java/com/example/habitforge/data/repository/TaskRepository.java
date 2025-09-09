package com.example.habitforge.data.repository;

import android.content.Context;

import com.example.habitforge.application.model.Task;
import com.example.habitforge.data.database.TaskLocalDataSource;

import java.util.List;

public class TaskRepository {

    private static TaskRepository instance;
    private final TaskLocalDataSource localDataSource;

    private TaskRepository(Context context) {
        this.localDataSource = new TaskLocalDataSource(context);
    }

    // Singleton instanca
    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context.getApplicationContext());
        }
        return instance;
    }

    // CRUD metode
    public void addTask(Task task) {
        this.localDataSource.addTask(task);
    }

    public void updateTask(Task task) {
        this.localDataSource.updateTask(task);
    }

    public void deleteTask(String taskId) {
        this.localDataSource.deleteTask(taskId);
    }

    public Task getTaskById(String taskId) {
        return localDataSource.getTaskById(taskId);
    }


}
