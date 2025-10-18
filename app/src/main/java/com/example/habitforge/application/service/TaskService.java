package com.example.habitforge.application.service;

import android.content.Context;

import com.example.habitforge.application.model.Task;
import com.example.habitforge.data.repository.TaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(Context context) {
        this.taskRepository = TaskRepository.getInstance(context);
    }
    public void createTask(Task task, OnCompleteListener<Void>callback) {
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be empty!");
        }
        if(task.getUserId()==null||task.getUserId().trim().isEmpty()){
            throw new IllegalArgumentException("Task must be linked to a user!");
        }
        task.calculateXp();
        taskRepository.addTask(task,callback);
    }

    public void editTask(Task task,OnCompleteListener<Void> callback) {
        if(task.getId()==null||task.getId().trim().isEmpty()){
            throw new IllegalArgumentException("Task id is required for update");
        }
        task.calculateXp();
        taskRepository.updateTask(task,callback);
    }

    public void removeTask(String taskId,OnCompleteListener<Void>callback) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task id is required for deleting the task");
        }

        taskRepository.deleteTask(taskId, callback);    }

    public void getTask(String taskId, OnCompleteListener<Task> callback) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task id is required");
        }

        taskRepository.getTaskById(taskId, callback);
    }
    public void getAllTasks(OnCompleteListener<List<Task>> callback) {
        taskRepository.getAllTasks(callback);
    }

    public void getTasksForUser(String userId, OnCompleteListener<List<Task>> callback) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User id is required");
        }

        taskRepository.getAllTasksForUser(userId, callback);
    }

    public void updateTask(Task task, OnCompleteListener<Void> callback) {
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Task id is required for update");
        }
        task.calculateXp(); // da se xp racuna ako se nesto menja-ali ne bi trebalo
        taskRepository.updateTask(task, callback);
    }


}
