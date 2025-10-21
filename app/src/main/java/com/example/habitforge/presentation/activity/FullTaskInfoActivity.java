package com.example.habitforge.presentation.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.CategoryService;
import com.example.habitforge.application.service.TaskService;
import com.example.habitforge.application.service.UserService;
import com.example.habitforge.application.util.EnumMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FullTaskInfoActivity extends AppCompatActivity {

    private TextView tvTaskName, tvTaskDescription, tvCategory, tvTaskType,
            tvDifficulty, tvPriority, tvXP, tvStatus, tvDateRange;
    private Button btnEditTask, btnDeleteTask,btnChangeStatus;
    private TaskService taskService;

    private UserService userService;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(this);

        setContentView(R.layout.activity_full_task_info);

        // 🔹 UI
        tvTaskName = findViewById(R.id.tvTaskName);
        tvTaskDescription = findViewById(R.id.tvTaskDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvTaskType = findViewById(R.id.tvTaskType);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvPriority = findViewById(R.id.tvPriority);
        tvXP = findViewById(R.id.tvXP);
        tvStatus = findViewById(R.id.tvStatus);
        tvDateRange = findViewById(R.id.tvDateRange);
        btnEditTask = findViewById(R.id.btnEditTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);

        btnChangeStatus.setOnClickListener(v -> {
            if (currentTask != null) showStatusOptions(currentTask);
        });

        taskService = new TaskService(this);

        // preuzmi task id
        String taskId = getIntent().getStringExtra("taskId");
        if (taskId == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taskService.getTaskById(taskId, result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                currentTask = result.getResult();
                long now = System.currentTimeMillis();
                long threeDaysMillis = 3 * 24 * 60 * 60 * 1000L;

                if (currentTask.getStatus() == TaskStatus.ACTIVE && now - currentTask.getExecutionTime() > threeDaysMillis) {
                    currentTask.setStatus(TaskStatus.UNCOMPLETED);
                    taskService.editTask(currentTask, r -> {});
                }

                runOnUiThread(this::populateData);
            } else {
                Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
            }
        });

        btnEditTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTaskActivity.class);
            intent.putExtra("editTask", currentTask);
            startActivity(intent);
        });
        btnDeleteTask.setOnClickListener(v -> {
            if (currentTask == null) return;

            if (currentTask.getStatus() == com.example.habitforge.application.model.enums.TaskStatus.COMPLETED) {
                Toast.makeText(this, "❌ Completed tasks cannot be deleted", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage(currentTask.getTaskType() == com.example.habitforge.application.model.enums.TaskType.RECURRING
                            ? "Are you sure? This will delete all future repetitions."
                            : "Are you sure you want to delete this task?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", (dialog, which) -> deleteTaskConfirmed())
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });


    }

    private void deleteTaskConfirmed() {
        if (currentTask == null) return;

        if (currentTask.getStatus() == TaskStatus.CANCELED || currentTask.getStatus() == TaskStatus.UNCOMPLETED) {
            Toast.makeText(this, "❌ This task cannot be deleted!", Toast.LENGTH_SHORT).show();
            return;
        }


        //ako je ponavljajuci skrati kraj na danas
        if (currentTask.getTaskType() == TaskType.RECURRING) {
            long now = System.currentTimeMillis();
            if (currentTask.getRecurringEnd() > now) {
                currentTask.setRecurringEnd(now);
            }

            currentTask.setStatus(TaskStatus.COMPLETED);

            taskService.editTask(currentTask, result -> {
                runOnUiThread(() -> {
                    if (result.isSuccessful()) {
                        Toast.makeText(this, "🗓️ Future recurrences removed.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, TaskListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "⚠️ Error updating recurring task.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

        } else {
            // za jednokratni —potpuno brise iz baze
            taskService.deleteTask(currentTask.getId(), result -> {
                runOnUiThread(() -> {
                    if (result.isSuccessful()) {
                        Toast.makeText(this, "🗑️ Task deleted successfully.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, TaskListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "⚠️ Error deleting task.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void populateData() {
        tvTaskName.setText(currentTask.getName());
        tvTaskDescription.setText(currentTask.getDescription());

        CategoryService categoryService = new CategoryService(this);
        categoryService.getAllCategories(result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                for (Category c : result.getResult()) {
                    if (c.getId().equals(currentTask.getCategoryId())) {
                        runOnUiThread(() -> {
                            tvCategory.setText("Category: " + c.getName());
                            try {
                                tvCategory.setTextColor(android.graphics.Color.parseColor(c.getColor()));
                            } catch (Exception ignored) {}
                        });
                        return;
                    }
                }
            }
            runOnUiThread(() -> tvCategory.setText("Category: (unknown)"));
        });

        tvCategory.setText("Category: (hidden test)"); // privremeni placeholder


        tvTaskType.setText("Type: " + currentTask.getTaskType());
        tvDifficulty.setText("Difficulty: " + EnumMapper.getDifficultyLabel(currentTask.getDifficulty()));
        tvPriority.setText("Priority: " + EnumMapper.getPriorityLabel(currentTask.getPriority()));
        tvXP.setText("XP: " + currentTask.getXp());
        tvStatus.setText("Status: " + currentTask.getStatus());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        if (currentTask.getTaskType() == com.example.habitforge.application.model.enums.TaskType.ONE_TIME) {
            tvDateRange.setText("📅 " + sdf.format(new Date(currentTask.getExecutionTime())));
        } else {
            SimpleDateFormat sdfShort = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String start = sdfShort.format(new Date(currentTask.getRecurringStart()));
            String end = sdfShort.format(new Date(currentTask.getRecurringEnd()));
            tvDateRange.setText("📆 " + start + " → " + end +
                    "   ⏳ Every " + currentTask.getRecurringInterval() + " " +
                    currentTask.getRecurrenceUnit().name().toLowerCase());
        }
    }



    private void showStatusOptions(Task task) {
        String[] options = {
                "✅ Mark as Completed",
                "❌ Mark as Canceled",
                "⏸️ Pause Task",
                "🟢 Activate Again"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Change Task Status")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: updateTaskStatus(task, com.example.habitforge.application.model.enums.TaskStatus.COMPLETED); break;
                        case 1: updateTaskStatus(task, com.example.habitforge.application.model.enums.TaskStatus.CANCELED); break;
                        case 2: updateTaskStatus(task, com.example.habitforge.application.model.enums.TaskStatus.PAUSED); break;
                        case 3: updateTaskStatus(task, com.example.habitforge.application.model.enums.TaskStatus.ACTIVE); break;
                    }
                })
                .show();
    }

    private void updateTaskStatus(Task task, TaskStatus newStatus) {
        long now = System.currentTimeMillis();

        // doznovljeno menjanje za active ui paused
        if (task.getStatus() == TaskStatus.CANCELED || task.getStatus() == TaskStatus.UNCOMPLETED) {
            Toast.makeText(this, "❌ This task can no longer be modified.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (task.getStatus() != TaskStatus.ACTIVE && task.getStatus() != TaskStatus.PAUSED) {
            Toast.makeText(this, "❌ Only active or paused tasks can be updated.", Toast.LENGTH_SHORT).show();
            return;
        }


        //ne mzoe se oznaciti ako uradjeno ako nije proslo vreme izvrsavanja
        if (newStatus == TaskStatus.COMPLETED && task.getExecutionTime() > now) {
            Toast.makeText(this, "⏳ Task cannot be marked as completed before its time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // asko je proslo 3 dana-prelazi u neuradjen -ovo je rucno akd se radi
//        long threeDaysMillis = 3 * 24 * 60 * 60 * 1000L;
//        if (now - task.getExecutionTime() > threeDaysMillis && newStatus != TaskStatus.CANCELED) {
//            newStatus = TaskStatus.UNCOMPLETED;
//            Toast.makeText(this, "⚠️ Task marked as uncompleted (more than 3 days late).", Toast.LENGTH_SHORT).show();
      //  }

        // pauziran mzoe opet da se aktivira
        if (task.getStatus() == TaskStatus.PAUSED && newStatus == TaskStatus.ACTIVE) {
            Toast.makeText(this, "🔄 Task resumed.", Toast.LENGTH_SHORT).show();
        }
        if (task.getStatus() == TaskStatus.UNCOMPLETED && newStatus == TaskStatus.ACTIVE) {
            Toast.makeText(this, "❌ Uncompleted tasks cannot be reactivated.", Toast.LENGTH_SHORT).show();
            return;
        }

        // xp logika
        if (newStatus == TaskStatus.COMPLETED) {
            task.calculateXp();
             userService.addExperienceToCurrentUser(this, task.getXp(), task.getId());
        }
        else if (newStatus == TaskStatus.CANCELED || newStatus == TaskStatus.PAUSED) {
            task.setXp(0); // ne racuna se u XP
        }


        task.setStatus(newStatus);

        TaskStatus finalNewStatus = newStatus;
        taskService.editTask(task, res -> runOnUiThread(() -> {
            if (res.isSuccessful()) {
                tvStatus.setText("Status: " + finalNewStatus.name());
                Toast.makeText(this, "✅ Status changed to " + finalNewStatus.name(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Failed to update status.", Toast.LENGTH_SHORT).show();
            }
        }));
    }

}
