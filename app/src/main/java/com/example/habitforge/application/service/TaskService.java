package com.example.habitforge.application.service;

import android.content.Context;
import android.util.Log;

import com.example.habitforge.application.model.AllianceMission;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.session.SessionManager;
import com.example.habitforge.data.repository.TaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository;
    private final Context context;
    public TaskService(Context context) {
        this.taskRepository = TaskRepository.getInstance(context);
        this.context=context;
    }
    public void createTask(Task task, OnCompleteListener<Void>callback) {
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be empty!");
        }
        if(task.getUserId()==null||task.getUserId().trim().isEmpty()){
            throw new IllegalArgumentException("Task must be linked to a user!");
        }
//        task.calculateXp();
//        taskRepository.addTask(task,callback);
        taskRepository.addTaskWithQuotaCheck(task, callback);

    }

    public void editTask(Task task,OnCompleteListener<Void> callback) {
        if(task.getId()==null||task.getId().trim().isEmpty()){
            throw new IllegalArgumentException("Task id is required for update");
        }
        task.calculateXp();
//        taskRepository.updateTask(task,callback);
        taskRepository.updateTask(task, updateResult -> {
            if (updateResult.isSuccessful()) {
//azuriranje misije
                if (task.getStatus() == com.example.habitforge.application.model.enums.TaskStatus.COMPLETED) {
                    handleAllianceMissionProgress(task);
                }
            }

            callback.onComplete(updateResult);
        });
    }

    public void removeTask(String taskId,OnCompleteListener<Void>callback) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task id is required for deleting the task");
        }

        taskRepository.deleteTask(taskId, callback);    }

    public void getTaskById(String taskId, OnCompleteListener<Task> callback) {
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

    public void deleteTask(String taskId, OnCompleteListener<Void> callback) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID is required to delete the task");
        }

        taskRepository.deleteTask(taskId, callback);
    }

    //za brisanje samo od danasnjeg dana,zavrsene ponavljajuce ne
    public void deleteRecurringFutureTasks(Task recurringTask, OnCompleteListener<Void> listener) {
        getAllTasks(result -> {
            if (!result.isSuccessful() || result.getResult() == null) {
                listener.onComplete(Tasks.forException(
                        new Exception("Failed to fetch tasks for cleanup")));
                return;
            }

            long now = System.currentTimeMillis();

            for (Task t : result.getResult()) {
                if (t.getTaskType() == TaskType.RECURRING &&
                        t.getName().equals(recurringTask.getName()) &&
                        t.getRecurringStart() >= now) {
                    deleteTask(t.getId(), ignored -> {});
                }
            }

            listener.onComplete(Tasks.forResult(null));
        });
    }

    public void markOldTasksAsUncompleted() {
        getAllTasks(result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                long now = System.currentTimeMillis();
                long threeDaysMillis = 3 * 24 * 60 * 60 * 1000L;

                for (Task t : result.getResult()) {
                    if (t.getStatus() == com.example.habitforge.application.model.enums.TaskStatus.ACTIVE &&
                            now - t.getExecutionTime() > threeDaysMillis) {
                        t.setStatus(com.example.habitforge.application.model.enums.TaskStatus.UNCOMPLETED);
                        editTask(t, r -> {});
                    }
                }
            }
        });
    }
    public void handleAllianceMissionProgress(Task task) {
        AllianceMissionService missionService = new AllianceMissionService(context);
        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();

        Log.i("AllianceMission", "⚔️ handleAllianceMissionProgress STARTED for task: " + task.getName() + " [" + task.getStatus() + "]");

        UserService userService = new UserService(context);
        userService.getUserById(userId, new com.example.habitforge.data.repository.UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getAllianceId() == null) {
                    Log.w("AllianceMission", "🚫 User has no allianceId, skipping.");
                    return;
                }

                String allianceId = user.getAllianceId();
                Log.i("AllianceMission", "✅ User allianceId = " + allianceId);

                missionService.getAllianceMissions(allianceId, new AllianceMissionService.MissionListCallback() {
                    @Override
                    public void onSuccess(List<AllianceMission> missions) {
                        Log.i("AllianceMission", "🎯 Found " + missions.size() + " missions for alliance.");

                        if (missions.isEmpty()) {
                            Log.w("AllianceMission", "⚠️ No missions found for this alliance.");
                            return;
                        }

                        AllianceMission mission = missions.get(0);
                        Log.i("AllianceMission", "🧩 Active mission: " + mission.getId() + " | isActive=" + mission.isActive());

                        if (!mission.isActive()) {
                            Log.w("AllianceMission", "⚠️ Mission is not active, skipping.");
                            return;
                        }

                        int damage;
                        int limit;

                        if (task.getDifficulty() == TaskDifficulty.VERY_EASY
                                || task.getDifficulty() == TaskDifficulty.EASY
                                || task.getPriority() == TaskPriority.NORMAL
                                || task.getPriority() == TaskPriority.IMPORTANT) {

                            damage = 1;
                            limit = 10;

                            //  Posebno pravilo: EASY + NORMAL → računa se kao 2 puta
                            if (task.getDifficulty() == TaskDifficulty.EASY && task.getPriority() == TaskPriority.NORMAL) {
                                damage = 2;
                            }

                        } else {
                            //  Ostali zadaci (SPECIAL, CHALLENGE, itd.)
                            damage = 4;
                            limit = 6;
                        }

                        Log.i("AllianceMission", "💥 Task damage = " + damage +
                                " | limit = " + limit +
                                " | difficulty = " + task.getDifficulty() +
                                " | priority = " + task.getPriority());


                        Log.i("AllianceMission", "💥 Calculated damage = " + damage + ", limit = " + limit);

                        // umesto current damage, gledamo broj taskova (taskCount)
                        int taskCount = mission.getTaskCount() != null && mission.getTaskCount().containsKey(userId)
                                ? mission.getTaskCount().get(userId)
                                : 0;

                        Log.i("AllianceMission", "📊 Current completed tasks for user = " + taskCount);

                        if (taskCount < limit) {
                            int finalDamage = damage;
                            Log.i("AllianceMission", "➡️ addMemberProgress() + increment taskCount");

                            missionService.addMemberProgress(
                                    mission.getId(),
                                    userId,
                                    finalDamage,
                                    () -> {
                                        Log.i("AllianceMission", "✅ Boss HP −" + finalDamage + " from task " + task.getName());
                                        // 📈 povećaj broj taskova
                                        missionService.incrementTaskCount(mission.getId(), userId);
                                    },
                                    () -> Log.e("AllianceMission", "⚠️ Failed to update mission progress")
                            );
                        } else {
                            Log.i("AllianceMission", "ℹ️ Max number of completed tasks reached for this user.");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMission", "❌ Failed to get missions: " + e.getMessage());
                    }
                });
            }


            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "❌ Failed to get user by ID: " + e.getMessage());
            }
        });
    }


    //  BONUS: ako tokom specijalne misije korisnik nema nerešenih zadataka → +10 HP
    public void checkSpecialMissionBonus(String allianceId, String userId) {
        getTasksForUser(userId, result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                boolean hasUncompleted = result.getResult().stream()
                        .anyMatch(t -> t.getStatus() == com.example.habitforge.application.model.enums.TaskStatus.UNCOMPLETED);

                if (!hasUncompleted) {
                    AllianceMissionService missionService = new AllianceMissionService(context);
                    missionService.addMemberProgress(
                            allianceId,
                            userId,
                            10,
                            () -> System.out.println("🏆 +10 HP bonus — no uncompleted tasks!"),
                            () -> System.err.println("⚠️ Failed to add 10 HP bonus")
                    );
                } else {
                    System.out.println("❌ Bonus not granted — user has uncompleted tasks.");
                }
            } else {
                System.err.println("⚠️ Failed to load tasks for bonus check");
            }
        });
    }







}
