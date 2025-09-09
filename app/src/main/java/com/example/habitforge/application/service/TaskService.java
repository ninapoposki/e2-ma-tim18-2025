package com.example.habitforge.application.service;

import android.content.Context;

import com.example.habitforge.application.model.Task;
import com.example.habitforge.data.repository.TaskRepository;

import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(Context context) {
        this.taskRepository = TaskRepository.getInstance(context);
    }
    public void createTask(Task task) {
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Naziv taska ne može biti prazan");
        }
        taskRepository.addTask(task);
    }

    public void editTask(Task task) {
        taskRepository.updateTask(task);
    }

    public void removeTask(String taskId) {
        taskRepository.deleteTask(taskId);
    }

    public Task getTask(String taskId) {
        return taskRepository.getTaskById(taskId);
    }

}
