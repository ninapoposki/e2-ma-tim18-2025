package com.example.habitforge.presentation.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.habitforge.R;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.TaskService;
import com.example.habitforge.application.service.UserService;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import java.text.SimpleDateFormat;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView taskName, taskDescription, taskDate, taskTime, taskXp, taskStatus;
    private Button changeStatusBtn;
    private Task task;

    private UserService userService;

    private TextView btnViewMore;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        userService = new UserService(this);

        taskName = findViewById(R.id.taskName);
        taskDescription = findViewById(R.id.taskDescription);
        taskDate = findViewById(R.id.taskDate);
        taskTime = findViewById(R.id.taskTime);
        taskXp = findViewById(R.id.taskXp);
        taskStatus = findViewById(R.id.taskStatus);
        changeStatusBtn = findViewById(R.id.changeStatusBtn);
        btnViewMore = findViewById(R.id.btnViewMore);

        String taskId = getIntent().getStringExtra("taskId");
        if (taskId == null || taskId.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Task ID not provided.")
                    .setPositiveButton("OK", (d, w) -> finish())
                    .show();
            return;
        }

        TaskService service = new TaskService(this);
        service.getTaskById(taskId, result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                task = result.getResult();

                if (task.getName() == null) task.setName("(Unnamed Task)");
                if (task.getDescription() == null) task.setDescription("(No description)");
                if (task.getStatus() == null)
                    task.setStatus(com.example.habitforge.application.model.enums.TaskStatus.ACTIVE);
                if (task.getXp() == 0) task.calculateXp();

                runOnUiThread(() -> fillDetails(task));
                btnViewMore.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(this, FullTaskInfoActivity.class);
                    intent.putExtra("taskId", task.getId());
                    startActivity(intent);
                });


            } else {
                runOnUiThread(() -> new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Could not load task details.")
                        .setPositiveButton("OK", (d, w) -> finish())
                        .show());
            }
        });


        changeStatusBtn.setOnClickListener(v -> showStatusOptions(task));
    }

    @SuppressLint("SetTextI18n")
    private void fillDetails(Task t) {
        taskName.setText(t.getName());
        taskDescription.setText(t.getDescription() != null ? t.getDescription() : "(no description)");
        taskXp.setText("⭐ XP: " + t.getXp());
        taskStatus.setText("📌 Status: " + t.getStatus().name());

        if (t.getTaskType() == TaskType.RECURRING) {
            LocalDate start = Instant.ofEpochMilli(t.getRecurringStart()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = Instant.ofEpochMilli(t.getRecurringEnd()).atZone(ZoneId.systemDefault()).toLocalDate();
            taskDate.setText("📅 Period: " + start + " ➜ " + end);
            taskTime.setText("🔁 Recurring task");
        } else {
            taskDate.setText("📅 Date: " + Instant.ofEpochMilli(t.getExecutionTime()).atZone(ZoneId.systemDefault()).toLocalDate());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            taskTime.setText("⏰ Time: " + sdf.format(t.getExecutionTime()));
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
                    TaskStatus newStatus = null;
                    switch (which) {
                        case 0: newStatus = TaskStatus.COMPLETED; break;
                        case 1: newStatus = TaskStatus.CANCELED; break;
                        case 2: newStatus = TaskStatus.PAUSED; break;
                        case 3: newStatus = TaskStatus.ACTIVE; break;
                    }
                    if (newStatus != null) updateTaskStatus(task, newStatus);
                })
                .show();
    }

//    private void updateTaskStatus(Task task, TaskStatus newStatus) {
//        task.setStatus(newStatus);
//        TaskService service = new TaskService(this);
//
//        service.editTask(task, res -> runOnUiThread(() -> {
//            if (res.isSuccessful()) {
//                taskStatus.setText("📌 Status: " + newStatus.name()); // 🟢 odmah prikaži novi status
//                new androidx.appcompat.app.AlertDialog.Builder(this)
//                        .setTitle("Status Updated")
//                        .setMessage("Status changed to " + newStatus.name() + ".")
//                        .setPositiveButton("OK", (d, w) -> d.dismiss())
//                        .show();
//            } else {
//                new androidx.appcompat.app.AlertDialog.Builder(this)
//                        .setTitle("Error")
//                        .setMessage("Failed to update task status.")
//                        .setPositiveButton("OK", (d, w) -> d.dismiss())
//                        .show();
//            }
//        }));
//    }
private void updateTaskStatus(Task task, TaskStatus newStatus) {
    long now = System.currentTimeMillis();

    if (task.getStatus() == TaskStatus.CANCELED || task.getStatus() == TaskStatus.UNCOMPLETED) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Not Allowed")
                .setMessage("❌ This task can no longer be modified.")
                .setPositiveButton("OK", null)
                .show();
        return;
    }

    if (task.getStatus() != TaskStatus.ACTIVE && task.getStatus() != TaskStatus.PAUSED) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Invalid Action")
                .setMessage("❌ Only active or paused tasks can be updated.")
                .setPositiveButton("OK", null)
                .show();
        return;
    }

    if (newStatus == TaskStatus.COMPLETED && task.getExecutionTime() > now) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Too Early")
                .setMessage("⏳ You cannot mark this task as completed before its scheduled time.")
                .setPositiveButton("OK", null)
                .show();
        return;
    }

    if (task.getStatus() == TaskStatus.UNCOMPLETED && newStatus == TaskStatus.ACTIVE) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Not Allowed")
                .setMessage("❌ Uncompleted tasks cannot be reactivated.")
                .setPositiveButton("OK", null)
                .show();
        return;
    }

    if (task.getStatus() == TaskStatus.PAUSED && newStatus == TaskStatus.ACTIVE) {
        Toast.makeText(this, "🔄 Task resumed.", Toast.LENGTH_SHORT).show();
    }

    if (newStatus == TaskStatus.COMPLETED) {
        task.calculateXp();
        userService.addExperienceToCurrentUser(this, task.getXp());

    } else if (newStatus == TaskStatus.CANCELED || newStatus == TaskStatus.PAUSED) {
        task.setXp(0);
    }

    task.setStatus(newStatus);
    TaskService service = new TaskService(this);

    TaskStatus finalNewStatus = newStatus;
    service.editTask(task, res -> runOnUiThread(() -> {
        if (res.isSuccessful()) {
            taskStatus.setText("📌 Status: " + finalNewStatus.name());
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Status Updated")
                    .setMessage("✅ Status changed to " + finalNewStatus.name() + ".")
                    .setPositiveButton("OK", (d, w) -> {
                        // 🔁 automatski redirektuj nazad na listu zadataka
                        Intent intent = new Intent(this, TaskListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        } else {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("⚠️ Failed to update task status.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }));
}



}
