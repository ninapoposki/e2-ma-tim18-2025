package com.example.habitforge.presentation.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskType;
import com.example.habitforge.application.service.CategoryService;
import com.example.habitforge.application.service.TaskService;
import com.example.habitforge.data.repository.CategoryRepository;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;


public class TaskCalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TaskService taskService;
    private final Map<CalendarDay, List<Task>> taskMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);
        android.util.Log.i("TaskDetails", " TaskDetailsActivity started!");

        AndroidThreeTen.init(this);

        calendarView = findViewById(R.id.calendarView);
        taskService = new TaskService(this);

        loadTasksIntoCalendar();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<Task> dayTasks = taskMap.get(date);

            if (dayTasks == null || dayTasks.isEmpty()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("No Tasks")
                        .setMessage("There are no tasks scheduled for this date.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            String[] taskDisplay = dayTasks.stream()
                    .map(t -> {
                        StringBuilder info = new StringBuilder(t.getName());
                        if (t.getTaskType() == TaskType.ONE_TIME) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
                            info.append("   ⏰ ").append(sdf.format(t.getExecutionTime())).append(" h");
                        } else if (t.getTaskType() == TaskType.RECURRING) {
                            LocalDate start = Instant.ofEpochMilli(t.getRecurringStart())
                                    .atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate end = Instant.ofEpochMilli(t.getRecurringEnd())
                                    .atZone(ZoneId.systemDefault()).toLocalDate();

                            if (date.getDate().equals(start)) info.append("   🟢 START");
                            else if (date.getDate().equals(end)) info.append("   🔴 END");
                        }
                        return info.toString();
                    })
                    .toArray(String[]::new);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Tasks for " + date.getDate())
                    .setItems(taskDisplay, (dialog, which) -> {
                        Task selectedTask = dayTasks.get(which);

                        Intent intent = new Intent(TaskCalendarActivity.this, TaskDetailsActivity.class);
                        intent.putExtra("taskId", selectedTask.getId()); // radi samo ako Task implements Serializable
                        android.util.Log.i("Calendar", "➡ Opening details for: " + selectedTask.getName());
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        }



    private void loadTasksIntoCalendar() {
        taskService.getAllTasks(taskListTask -> {
            if (!taskListTask.isSuccessful() || taskListTask.getResult() == null) return;

            List<Task> tasks = taskListTask.getResult();
            runOnUiThread(() -> calendarView.removeDecorators());
            taskMap.clear();

            for (Task t : tasks) {

                int colorInt = getCategoryColorById(t.getCategoryId());
                String name = t.getName();

                // 🚫 Ako je ONE_TIME i obrisan, preskoči
                if (t.getTaskType() == TaskType.ONE_TIME &&
                        t.getStatus() == com.example.habitforge.application.model.enums.TaskStatus.CANCELED)
                    continue;

                if (t.getTaskType() == TaskType.ONE_TIME) {
                    LocalDate date = Instant.ofEpochMilli(t.getExecutionTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    CalendarDay day = CalendarDay.from(date);
                    taskMap.computeIfAbsent(day, k -> new java.util.ArrayList<>()).add(t);
                    calendarView.addDecorator(new TaskDecorator(colorInt, day, name, false, false));

                } else { // 🔁 Recurring
                    LocalDate start = Instant.ofEpochMilli(t.getRecurringStart())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    LocalDate end = Instant.ofEpochMilli(t.getRecurringEnd())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    LocalDate today = LocalDate.now();

                    // 🔹 Ako je obrisan (CANCELED), prikaži samo do trenutnog kraja (recurringEnd)
                    // 🔹 Ako NIJE obrisan, prikaži sve do stvarnog kraja (može biti i u budućnosti)
                    LocalDate visibleEnd = end;
                    if (t.getStatus() == com.example.habitforge.application.model.enums.TaskStatus.CANCELED
                            && end.isAfter(today)) {
                        visibleEnd = today; // ako je obrisan, ne idemo posle dana brisanja
                    }

                    int interval = t.getRecurringInterval() > 0 ? t.getRecurringInterval() : 1;
                    String unit = t.getRecurrenceUnit() != null ? t.getRecurrenceUnit().name() : "DAY";

                    LocalDate current = start;

                    while (!current.isAfter(visibleEnd)) {
                        CalendarDay day = CalendarDay.from(current);
                        taskMap.computeIfAbsent(day, k -> new java.util.ArrayList<>()).add(t);
                        calendarView.addDecorator(new TaskDecorator(colorInt, day, name, false, false));

                        switch (unit) {
                            case "DAY":
                                current = current.plusDays(interval);
                                break;
                            case "WEEK":
                                current = current.plusWeeks(interval);
                                break;
                            default:
                                current = current.plusDays(interval);
                        }
                    }
                }
            }
        });
    }





//            runOnUiThread(() -> {
//                for (Map.Entry<CalendarDay, Integer> entry : categoryColors.entrySet()) {
//                    calendarView.addDecorator(
//                            new FullColorDecorator(entry.getValue(), entry.getKey())
//                    );
//                }
//            });
//        });
//    }

    private int getCategoryColorById(String categoryId) {
        if (categoryId == null) {
            // fallback ako nema kategorije
            return 0xFFBDBDBD; // svetlo siva
        }

        CategoryService categoryService = new CategoryService(this);
        final int[] color = {0xFFBDBDBD}; // default siva boja

        categoryService.getCategoryById(categoryId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Category category = task.getResult();
                if (category.getColor() != null && !category.getColor().isEmpty()) {
                    try {
                        color[0] = android.graphics.Color.parseColor(category.getColor());
                    } catch (IllegalArgumentException e) {
                        color[0] = 0xFFBDBDBD; // fallback ako format nije dobar
                    }
                }
            }
        });

        return color[0];
    }




    static class TaskDecorator implements DayViewDecorator {
        private final int color;
        private final CalendarDay day;
        private final String taskName;
        private final boolean isStart;
        private final boolean isEnd;

        TaskDecorator(int color, CalendarDay day, String taskName, boolean isStart, boolean isEnd) {
            this.color = color;
            this.day = day;
            this.taskName = taskName;
            this.isStart = isStart;
            this.isEnd = isEnd;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(this.day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            // boja pozadine dana
            view.setBackgroundDrawable(new android.graphics.drawable.GradientDrawable() {{
                setColor(color);
                setCornerRadius(16f);
            }});

            // tekst datuma će biti crn i bold
            view.addSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD));
            view.addSpan(new android.text.style.ForegroundColorSpan(Color.BLACK));

            // ako je start/kraj dodaj oznaku tooltip-style
            if (isStart) {
                view.addSpan(new android.text.style.SuperscriptSpan());
            } else if (isEnd) {
                view.addSpan(new android.text.style.SubscriptSpan());
            }
        }

        public String getTaskName() {
            return taskName;
        }
    }


}
