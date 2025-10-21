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
import com.google.firebase.Timestamp;
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


    public TaskRepository(Context context) {
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
        Log.d("TaskQuota", "addTaskWithQuotaCheck() called for task: " + task.getName());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        long startOfPeriod = getStartOfPeriod(task);

        // provera taskova sa istom tezinom ili koji imaju istu bitnost
        db.collection("tasks")
                .whereGreaterThanOrEqualTo("createdAt", startOfPeriod)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int difficultyCount = 0;
                    int priorityCount = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Task existing = doc.toObject(Task.class);
                        if (existing != null) {
                            if (existing.getDifficulty() == task.getDifficulty()) {
                                difficultyCount++;
                            }
                            if (existing.getPriority() == task.getPriority()) {
                                priorityCount++;
                            }
                        }
                    }

                    boolean exceedsDifficulty = difficultyCount >= getQuotaForDifficulty(task.getDifficulty());
                    boolean exceedsPriority = priorityCount >= getQuotaForPriority(task.getPriority());

                    task.setExceedsQuota(exceedsDifficulty || exceedsPriority);

                    int difficultyXp = (task.getDifficulty() != null) ? task.getDifficulty().getXp() : 0;
                    int priorityXp = (task.getPriority() != null) ? task.getPriority().getXp() : 0;

                    if (!exceedsDifficulty && !exceedsPriority) {
                        task.setXp(difficultyXp + priorityXp);   // ništa nije prešao
                    } else if (exceedsDifficulty && !exceedsPriority) {
                        task.setXp(priorityXp);                  // prešao težinu → samo bitnost
                    } else if (!exceedsDifficulty && exceedsPriority) {
                        task.setXp(difficultyXp);                // prešao bitnost → samo težina
                    } else {
                        task.setXp(0);                           // prešao obe → ništa
                    }


                    Log.d("TaskQuota", "Difficulty count=" + difficultyCount +
                            ", Priority count=" + priorityCount +
                            ", exceedsDifficulty=" + exceedsDifficulty +
                            ", exceedsPriority=" + exceedsPriority);
                    addTask(task, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e("TaskQuota", "Error checking quota: ", e);
                    task.setExceedsQuota(false);
                    task.calculateXp();
                    addTask(task, callback);
                });
    }

    /** Kvota po težini */
    private int getQuotaForDifficulty(TaskDifficulty difficulty) {
        if (difficulty == TaskDifficulty.EASY || difficulty == TaskDifficulty.VERY_EASY)
            return 5; // dnevno
        else if (difficulty == TaskDifficulty.HARD)
            return 2; // dnevno
        else if (difficulty == TaskDifficulty.EXTREMELY_HARD)
            return 1; // nedeljno
        return 5;
    }

    /** Kvota po bitnosti */
    private int getQuotaForPriority(TaskPriority priority) {
        if (priority == TaskPriority.IMPORTANT)
            return 5; // dnevno
        else if (priority == TaskPriority.EXTREMELY_IMPORTANT)
            return 2; // dnevno
        else if (priority == TaskPriority.SPECIAL)
            return 1; // mesečno
        return 5;
    }

    /** Period za proveru kvote */
    private long getStartOfPeriod(Task task) {
        Calendar cal = Calendar.getInstance();
        TaskDifficulty diff = task.getDifficulty();
        TaskPriority prio = task.getPriority();

        if (diff == TaskDifficulty.EXTREMELY_HARD || prio == TaskPriority.SPECIAL) {
            if (diff == TaskDifficulty.EXTREMELY_HARD) cal.add(Calendar.DAY_OF_YEAR, -7);
            if (prio == TaskPriority.SPECIAL) cal.add(Calendar.MONTH, -1);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return cal.getTimeInMillis();
    }





}
