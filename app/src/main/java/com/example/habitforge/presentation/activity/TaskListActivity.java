package com.example.habitforge.presentation.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.CategoryService;
import com.example.habitforge.application.service.TaskService;
import com.example.habitforge.presentation.adapter.TaskAdapter;
import com.google.android.material.tabs.TabLayout;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private TaskAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();


    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); //automatski osvezi
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TaskListActivity", "onCreate start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        recyclerView = findViewById(R.id.taskRecyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, new ArrayList<>(),new ArrayList<>());
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("One-time"));
        tabLayout.addTab(tabLayout.newTab().setText("Recurring"));
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();

//        loadTasks();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterTasks(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadTasks() {
        TaskService taskService = new TaskService(this);
        taskService.getAllTasks(taskResult -> {
            if (taskResult.isSuccessful() && taskResult.getResult() != null) {
                allTasks = taskResult.getResult();

                CategoryService categoryService = new CategoryService(this);
                categoryService.getAllCategories(catResult -> {
                    if (catResult.isSuccessful() && catResult.getResult() != null) {
                        runOnUiThread(() -> {
                            adapter.updateCategories(catResult.getResult());
                            filterTasks(tabLayout.getSelectedTabPosition());
                        });
                    }
                });
            }
        });
    }


    private void filterTasks(int tabIndex) {
        LocalDate today = LocalDate.now();
        List<Task> filtered = new ArrayList<>();

        for (Task t : allTasks) {
            // 👉 Prikazujemo samo aktivne i pauzirane zadatke
            if (t.getStatus() != TaskStatus.ACTIVE && t.getStatus() != TaskStatus.PAUSED)
                continue;

            if (t.getTaskType() == TaskType.ONE_TIME) {
                LocalDate execDate = Instant.ofEpochMilli(t.getExecutionTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                // prikazi sve aktivne/pauzirane ONE_TIME zadatke (ne mora od danas)
                if (tabIndex == 0)
                    filtered.add(t);

            } else if (t.getTaskType() == TaskType.RECURRING) {
                LocalDate start = Instant.ofEpochMilli(t.getRecurringStart())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate end = Instant.ofEpochMilli(t.getRecurringEnd())
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                // prikazi sve aktivne/pauzirane RECURRING zadatke
                if (tabIndex == 1)
                    filtered.add(t);
            }
        }

        runOnUiThread(() -> adapter.updateTasks(filtered));
    }



}
