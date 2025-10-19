package com.example.habitforge.presentation.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.habitforge.application.session.SessionManager;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.RecurrenceUnit;
import com.example.habitforge.application.model.enums.TaskDifficulty;
import com.example.habitforge.application.model.enums.TaskPriority;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.CategoryService;
import com.example.habitforge.application.service.TaskService;
import com.example.habitforge.application.util.EnumMapper;
import com.example.habitforge.presentation.adapter.CategorySpinnerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskName, etTaskDescription, etRecurringInterval;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerPriority, spinnerRecurrenceUnit;
    private RadioGroup rgTaskType;
    private MaterialButton btnSaveTask, btnPickExecutionTime, btnPickStartDate, btnPickEndDate;

    private View layoutOneTime, layoutRecurring;

    private long selectedExecutionTime = 0;
    private long recurringStart = 0;
    private long recurringEnd = 0;

    private TaskService taskService;
    private CategoryService categoryService;

    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        // nije nista prikazano dok se ne odabere tip (ponavlja se ili ne)
        layoutOneTime = findViewById(R.id.layoutOneTime);
        layoutRecurring = findViewById(R.id.layoutRecurring);
        layoutOneTime.setVisibility(View.GONE);
        layoutRecurring.setVisibility(View.GONE);


        // 🔹 Initialize UI
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerRecurrenceUnit = findViewById(R.id.spinnerRecurrenceUnit);
        etRecurringInterval = findViewById(R.id.etRecurringInterval);

        rgTaskType = findViewById(R.id.rgTaskType);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnPickExecutionTime = findViewById(R.id.btnPickExecutionTime);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);

        layoutOneTime = findViewById(R.id.layoutOneTime);
        layoutRecurring = findViewById(R.id.layoutRecurring);

        taskService = new TaskService(this);
        categoryService = new CategoryService(this);

        loadCategories();
        setupSpinners();
        setupTaskTypeToggle();
        setupDatePickers();

        btnSaveTask.setOnClickListener(v -> saveTask());

//        Button viewTasksBtn = findViewById(R.id.btnViewTasks);
//        viewTasksBtn.setOnClickListener(v ->
//                startActivity(new Intent(AddTaskActivity.this, TaskCalendarActivity.class))
//        );

    }

    // ------------------ LOAD CATEGORIES ------------------
    private void loadCategories() {
        categoryService.getAllCategories(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categories = task.getResult();

                if (categories.isEmpty()) {
                    Toast.makeText(this, "No categories found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error loading categories!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidDateRange(long start, long end) {
        if (start <= 0 || end <= 0) return false;

        // kraj ne moze biti pre pocetka
        if (end < start) {
            Toast.makeText(this, "End date cannot be before start date!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // pocetak ne moze u proslosti
        if (start < System.currentTimeMillis()) {
            Toast.makeText(this, "Start date cannot be in the past!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // predugacak interval a premalo dana
        long diff = end - start;
        long maxDuration = 1000L * 60 * 60 * 24 * 365; // 1 godina
        if (diff > maxDuration) {
            Toast.makeText(this, "Interval is too long (max 1 year)!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void setupSpinners() {
        // 🔹 Difficulty spinner
        List<String> difficultyLabels = new ArrayList<>();
        for (TaskDifficulty d : TaskDifficulty.values()) {
            difficultyLabels.add(EnumMapper.getDifficultyLabel(d));
        }

        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficultyLabels);
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(diffAdapter);

        // 🔹 Priority spinner
        List<String> priorityLabels = new ArrayList<>();
        for (TaskPriority p : TaskPriority.values()) {
            priorityLabels.add(EnumMapper.getPriorityLabel(p));
        }

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, priorityLabels);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // 🔹 Recurrence unit
        ArrayAdapter<RecurrenceUnit> recurrenceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, RecurrenceUnit.values());
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurrenceUnit.setAdapter(recurrenceAdapter);
    }


    private void setupTaskTypeToggle() {
        rgTaskType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOneTime) {
                layoutOneTime.setVisibility(View.VISIBLE);
                layoutRecurring.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbRecurring) {
                layoutOneTime.setVisibility(View.GONE);
                layoutRecurring.setVisibility(View.VISIBLE);
            }
        });
    }

    // date pickers
    private void setupDatePickers() {
        // jednokratni
        btnPickExecutionTime.setOnClickListener(v -> showDateTimePicker((timestamp, formatted) -> {
            selectedExecutionTime = timestamp;
            btnPickExecutionTime.setText(formatted);
        }));

        // ponavljajuci pocetak
        btnPickStartDate.setOnClickListener(v -> showDatePicker((timestamp, formatted) -> {
            recurringStart = timestamp;
            btnPickStartDate.setText(formatted);
        }));

        // ponavljajuci kraj
        btnPickEndDate.setOnClickListener(v -> showDatePicker((timestamp, formatted) -> {
            recurringEnd = timestamp;
            btnPickEndDate.setText(formatted);
        }));
    }

    private void showDateTimePicker(OnDateTimePicked callback) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timeDialog = new TimePickerDialog(this,
                            (timeView, hour, minute) -> {
                                calendar.set(year, month, dayOfMonth, hour, minute);
                                long timestamp = calendar.getTimeInMillis();
                                String formatted = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                        .format(calendar.getTime());
                                callback.onPicked(timestamp, formatted);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timeDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    private void showDatePicker(OnDateTimePicked callback) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    long timestamp = calendar.getTimeInMillis();
                    String formatted = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            .format(calendar.getTime());
                    callback.onPicked(timestamp, formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dateDialog.show();
    }

    interface OnDateTimePicked {
        void onPicked(long timestamp, String formatted);
    }

    // ------------------ SAVE TASK ------------------
    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Task name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedType = rgTaskType.getCheckedRadioButtonId();
        if (selectedType == -1) {
            Toast.makeText(this, "Select task type", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryIndex = spinnerCategory.getSelectedItemPosition();
        if (categoryIndex < 0 || categoryIndex >= categories.size()) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = categories.get(categoryIndex);
//        TaskDifficulty difficulty = (TaskDifficulty) spinnerDifficulty.getSelectedItem();
//        TaskPriority priority = (TaskPriority) spinnerPriority.getSelectedItem();
        String difficultyLabel = (String) spinnerDifficulty.getSelectedItem();
        TaskDifficulty difficulty = null;
        for (TaskDifficulty d : TaskDifficulty.values()) {
            if (EnumMapper.getDifficultyLabel(d).equals(difficultyLabel)) {
                difficulty = d;
                break;
            }
        }

        String priorityLabel = (String) spinnerPriority.getSelectedItem();
        TaskPriority priority = null;
        for (TaskPriority p : TaskPriority.values()) {
            if (EnumMapper.getPriorityLabel(p).equals(priorityLabel)) {
                priority = p;
                break;
            }
        }


        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(System.currentTimeMillis());
        task.setName(name);
        task.setDescription(description);
        task.setCategoryId(selectedCategory.getId());
        task.setDifficulty(difficulty);
        task.setPriority(priority);
        task.calculateXp();
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();
        task.setStatus(TaskStatus.ACTIVE); //inicijalno pri kreiranju je aktivan


        if (userId != null) {
            task.setUserId(userId);
        } else {
            task.setUserId("unknownUser");
        }

        if (selectedType == R.id.rbOneTime) {
            if (selectedExecutionTime == 0) {
                Toast.makeText(this, "Please choose execution date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Validacija da nije prošlost
            if (selectedExecutionTime < System.currentTimeMillis()) {
                Toast.makeText(this, "Execution time cannot be in the past!", Toast.LENGTH_SHORT).show();
                return;
            }

            task.setExecutionTime(selectedExecutionTime);
            task.setTaskType(TaskType.ONE_TIME);

        } else if (selectedType == R.id.rbRecurring) {
            if (recurringStart == 0 || recurringEnd == 0) {
                Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDateRange(recurringStart, recurringEnd)) {
                return;
            }

            int interval = 1;
            try {
                interval = Integer.parseInt(etRecurringInterval.getText().toString());
            } catch (Exception ignored) {}

            if (interval <= 0) {
                Toast.makeText(this, "Interval must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            task.setRecurringStart(recurringStart);
            task.setRecurringEnd(recurringEnd);
            task.setRecurringInterval(interval);
            task.setRecurrenceUnit((RecurrenceUnit) spinnerRecurrenceUnit.getSelectedItem());
            task.setTaskType(TaskType.RECURRING);

        }


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
