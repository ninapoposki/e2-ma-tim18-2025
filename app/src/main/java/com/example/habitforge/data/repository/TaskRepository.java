package com.example.habitforge.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.data.database.TaskLocalDataSource;
import com.example.habitforge.data.firebase.TaskRemoteDataSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskRepository {

    private static TaskRepository instance;
    private final TaskLocalDataSource localDb;
    private final TaskRemoteDataSource remoteDb;


    private TaskRepository(Context context) {
        this.localDb = new TaskLocalDataSource(context);
        this.remoteDb = new TaskRemoteDataSource();
    }

    // Singleton instanca
    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context.getApplicationContext());
        }
        return instance;
    }

    // ---ADD---
    public void addTask(Task task, OnCompleteListener<Void> callback) {
        remoteDb.saveTask(task,remoteTask->{
            if(remoteTask.isSuccessful()){
                localDb.addTask(task);
            }
            callback.onComplete(remoteTask);
        });
    }

    //---UPDATE---
    public void updateTask(Task task,OnCompleteListener<Void> callback) {
        remoteDb.saveTask(task,remoteTask->{
            if(remoteTask.isSuccessful()){
                localDb.updateTask(task);
            }
            callback.onComplete(remoteTask);
        });
    }

    public void deleteTask(String taskId,OnCompleteListener<Void> callback) {
        remoteDb.deleteTask(taskId,remoteTask->{
            localDb.deleteTask(taskId);
            callback.onComplete(remoteTask);
        });
    }

    public void getTaskById(String taskId,OnCompleteListener<Task> callback) {
        Task cached = localDb.getTaskById(taskId);
        if (cached != null) {
            callback.onComplete((Tasks.forResult(cached)));
        }

        remoteDb.fetchTaskById(taskId, remoteTask -> {
            if (remoteTask.isSuccessful() && remoteTask.getResult() != null & remoteTask.getResult().exists()) {
                Task cloudTask = remoteTask.getResult().toObject(Task.class);
                if (cloudTask != null) {
                    localDb.addTask(cloudTask);
                    callback.onComplete(Tasks.forResult(cloudTask));
                }
            } else if (cached == null) {
                callback.onComplete(Tasks.forException(remoteTask.getException()));
            }
        });
    }

    //---GET ALL FOR USER---
    public void getAllTasksForUser(String userId, OnCompleteListener<List<Task>> callback) {
        List<Task> cached = localDb.getAllTasksForUser(userId);

        if (!cached.isEmpty()) {
            callback.onComplete(Tasks.forResult(cached));
        }

        remoteDb.fetchTasksForUser(userId, remoteTask -> {
            if (remoteTask.isSuccessful() && remoteTask.getResult() != null) {
                List<Task> remoteTasks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : remoteTask.getResult()) {
                    Task t = doc.toObject(Task.class);
                    remoteTasks.add(t);
                    localDb.addTask(t); // update local cache
                }
                callback.onComplete(Tasks.forResult(remoteTasks));
            } else if (cached.isEmpty()) {
                callback.onComplete(Tasks.forException(remoteTask.getException()));
            }
        });
    }

    // ---GET ALL TASKS (for calendar or admin view)---
    public void getAllTasks(OnCompleteListener<List<Task>> callback) {
        // 1️⃣ prvo pokušaj iz lokalne baze
        List<Task> cached = localDb.getAllTasks();
        if (!cached.isEmpty()) {
            callback.onComplete(Tasks.forResult(cached));
        }

        // 2️⃣ zatim povuci iz Firestore
        remoteDb.fetchAllTasks(remoteTask -> {
            if (remoteTask.isSuccessful() && remoteTask.getResult() != null) {
                List<Task> remoteTasks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : remoteTask.getResult()) {
                    Task t = doc.toObject(Task.class);
                    remoteTasks.add(t);
                    localDb.addTask(t); // update cache
                }
                callback.onComplete(Tasks.forResult(remoteTasks));
            } else if (cached.isEmpty()) {
                callback.onComplete(Tasks.forException(remoteTask.getException()));
            }
        });
    }
    // --- CHECK QUOTA BEFORE ADDING TASK ---
    public void addTaskWithQuotaCheck(Task task, OnCompleteListener<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int quota = getQuotaForTask(task);
        long startOfPeriod = getStartOfPeriod(task);

        db.collection("users")
                .document(task.getUserId())
                .collection("tasks")
                .whereEqualTo("difficulty", task.getDifficulty().name())
                .whereEqualTo("priority", task.getPriority().name())
                .whereGreaterThanOrEqualTo("createdAt", startOfPeriod)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // ✅ LOG mora biti ovde
                    Log.d("TaskQuota", "Found " + querySnapshot.size() + " tasks for "
                            + task.getDifficulty() + " + " + task.getPriority());
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Log.d("TaskQuota", "Existing task ID: " + doc.getId());
                    }

                    int existingCount = querySnapshot.size();
                    boolean exceeds = existingCount >= quota;
                    task.setExceedsQuota(exceeds);

                    if (!exceeds) {
                        task.calculateXp();
                    } else {
                        task.setXp(0);
                    }

                    addTask(task, callback);
                })
                .addOnFailureListener(e -> {
                    task.setExceedsQuota(false);
                    task.calculateXp();
                    addTask(task, callback);
                });
    }


    /** Quota limits based on difficulty + priority */
    private int getQuotaForTask(Task task) {
        TaskDifficulty difficulty = task.getDifficulty();
        TaskPriority priority = task.getPriority();

        if ((difficulty == TaskDifficulty.VERY_EASY && priority == TaskPriority.NORMAL) ||
                (difficulty == TaskDifficulty.EASY && priority == TaskPriority.IMPORTANT)) {
            return 5; // per day
        } else if (difficulty == TaskDifficulty.HARD && priority == TaskPriority.EXTREMELY_IMPORTANT) {
            return 2; // per day
        } else if (difficulty == TaskDifficulty.EXTREMELY_HARD) {
            return 1; // per week
        } else if (priority == TaskPriority.SPECIAL) {
            return 1; // per month
        }
        return 5;
    }

    /** Time range for quota evaluation */
    private long getStartOfPeriod(Task task) {
        Calendar cal = Calendar.getInstance();
        TaskDifficulty difficulty = task.getDifficulty();
        TaskPriority priority = task.getPriority();

        if (difficulty == TaskDifficulty.EXTREMELY_HARD) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
        } else if (priority == TaskPriority.SPECIAL) {
            cal.add(Calendar.MONTH, -1);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return cal.getTimeInMillis();
    }




}
