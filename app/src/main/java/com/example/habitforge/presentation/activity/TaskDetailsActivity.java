package com.example.habitforge.presentation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.habitforge.R;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.TaskService;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import java.text.SimpleDateFormat;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView taskName, taskDescription, taskDate, taskTime, taskXp, taskStatus;
    private Button changeStatusBtn;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        taskName = findViewById(R.id.taskName);
        taskDescription = findViewById(R.id.taskDescription);
        taskDate = findViewById(R.id.taskDate);
        taskTime = findViewById(R.id.taskTime);
        taskXp = findViewById(R.id.taskXp);
        taskStatus = findViewById(R.id.taskStatus);
        changeStatusBtn = findViewById(R.id.changeStatusBtn);

        task = (Task) getIntent().getSerializableExtra("task");

        if (task != null) fillDetails(task);

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

    private void updateTaskStatus(Task task, TaskStatus newStatus) {
        task.setStatus(newStatus);
        TaskService service = new TaskService(this);
        service.editTask(task, res -> runOnUiThread(() ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage("Status changed to " + newStatus)
                        .setPositiveButton("OK", (d, w) -> finish())
                        .show()
        ));
    }

}
