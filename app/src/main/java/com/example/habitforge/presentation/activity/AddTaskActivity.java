package com.example.habitforge.presentation.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.CategoryService;
import com.example.habitforge.application.service.TaskService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskName, etTaskDescription;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerPriority;
    private RadioGroup rgTaskType;
    private MaterialButton btnSaveTask;

    private TaskService taskService;
    private CategoryService categoryService;

    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        rgTaskType = findViewById(R.id.rgTaskType);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        taskService = new TaskService(this);
        categoryService = new CategoryService(this);

        loadCategories();
        setupSpinners();

        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void loadCategories() {
        categoryService.getAllCategories(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categories = task.getResult();
                List<String> categoryNames = new ArrayList<>();
                for (Category c : categories) {
                    categoryNames.add(c.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }
        });
    }

    private void setupSpinners() {
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TaskDifficulty.values()));
        spinnerPriority.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TaskPriority.values()));
    }

    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Task name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryIndex = spinnerCategory.getSelectedItemPosition();
        if (categoryIndex < 0 || categoryIndex >= categories.size()) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = categories.get(categoryIndex);
        TaskDifficulty difficulty = (TaskDifficulty) spinnerDifficulty.getSelectedItem();
        TaskPriority priority = (TaskPriority) spinnerPriority.getSelectedItem();

        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setName(name);
        task.setDescription(description);
        task.setCategoryId(selectedCategory.getId());
        task.setDifficulty(difficulty);
        task.setPriority(priority);
        task.calculateXp();
        task.setUserId("demoUser"); //za sada hardkodovano do spajanja


        int selectedType = rgTaskType.getCheckedRadioButtonId();
        task.setTaskType(selectedType == R.id.rbRecurring
                ? TaskType.RECURRING
                : TaskType.ONE_TIME);

        taskService.createTask(task, t -> {
            if (t.isSuccessful()) {
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + t.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
