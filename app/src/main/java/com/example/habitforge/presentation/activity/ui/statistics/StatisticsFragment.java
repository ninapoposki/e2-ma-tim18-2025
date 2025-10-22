package com.example.habitforge.presentation.activity.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.enums.TaskStatus;
import com.example.habitforge.data.repository.TaskRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private TextView tvActiveDays, tvLongestStreak;
    private ProgressBar progressBar;
    private PieChart donutChart;
    private BarChart categoryChart;
    private LineChart xpChart;
    private LineChart avgDifficultyChart;


    private TaskRepository taskRepository;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        tvActiveDays = view.findViewById(R.id.tvActiveDays);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        progressBar = view.findViewById(R.id.progressBar);
        donutChart = view.findViewById(R.id.donutChart);
        categoryChart = view.findViewById(R.id.categoryChart);
        xpChart = view.findViewById(R.id.xpChart);
        avgDifficultyChart = view.findViewById(R.id.avgDifficultyChart);


        taskRepository = TaskRepository.getInstance(getContext());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        taskRepository.getAllTasksForUser(currentUserId, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                List<Task> tasks = task.getResult();
                if (!tasks.isEmpty()) {
                    calculateAndDisplayStatistics(tasks);
                } else {
                    Toast.makeText(getContext(), "Nema zadataka za prikaz statistike.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Greška pri učitavanju zadataka.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayStatistics(List<Task> tasks) {
        // 1️⃣ Broj dana aktivnog korišćenja
        HashMap<String, Boolean> activeDays = new HashMap<>();
        for (Task t : tasks) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(t.getCreatedAt());
            String dayKey = c.get(Calendar.YEAR) + "-" + c.get(Calendar.DAY_OF_YEAR);
            activeDays.put(dayKey, true);
        }
        tvActiveDays.setText("Aktivnih dana: " + activeDays.size());

        // 2️⃣ Broj urađenih, neurađenih, otkazanih, ukupnih
        int done = 0, notDone = 0, canceled = 0;
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) done++;
            else if (t.getStatus() == TaskStatus.CANCELED) canceled++;
            else notDone++;
        }
        setupDonutChart(done, notDone, canceled);

        // 3️⃣ Najduži niz uspešnih dana
        int longestStreak = calculateLongestStreak(tasks);
        tvLongestStreak.setText("Najduži niz: " + longestStreak + " dana");

        // 4️⃣ Završeni zadaci po kategoriji
        HashMap<String, Integer> categoryCount = new HashMap<>();
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) {
                categoryCount.put(t.getCategoryId(), categoryCount.getOrDefault(t.getCategoryId(), 0) + 1);
            }
        }
        setupCategoryChart(categoryCount);
        setupAverageDifficultyChart(tasks);


        // 5️⃣ XP po danima (poslednjih 7 dana)
        setupXPChart(tasks);
    }

    private void setupDonutChart(int done, int notDone, int canceled) {
        int total = done + notDone + canceled; // ➕ broj ukupno kreiranih zadataka

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(done, "Urađeni"));
        entries.add(new PieEntry(notDone, "Neurađeni"));
        entries.add(new PieEntry(canceled, "Otkazani"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#FFC107"), Color.parseColor("#F44336")});
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        donutChart.setData(data);
        donutChart.setDrawHoleEnabled(true);
        donutChart.setHoleRadius(40f);
        donutChart.setTransparentCircleRadius(45f);
        donutChart.setCenterText("Zadaci\nUkupno: " + total);
        donutChart.setCenterTextSize(15f);
        donutChart.setCenterTextColor(Color.DKGRAY);
        donutChart.getDescription().setEnabled(false);
        donutChart.invalidate();
    }

    private void setupCategoryChart(HashMap<String, Integer> categoryCount) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        for (String cat : categoryCount.keySet()) {
            entries.add(new BarEntry(i++, categoryCount.get(cat)));
            labels.add(cat);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Završeni zadaci po kategoriji");
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        categoryChart.setData(data);
        categoryChart.getDescription().setEnabled(false);
        categoryChart.invalidate();
    }

    private void setupXPChart(List<Task> tasks) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, -6); // poslednjih 7 dana

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int totalXp = 0;
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, -6 + i); // pomeraj unapred, ne unazad
            for (Task t : tasks) {
                Calendar tc = Calendar.getInstance();
                tc.setTimeInMillis(t.getCreatedAt());
                if (tc.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                        tc.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR) &&
                        t.getStatus() == TaskStatus.COMPLETED) {
                    totalXp += t.getXp();
                }
            }
            entries.add(new Entry(i, totalXp));
        }

        // 🔹 više ne treba Collections.reverse()
        LineDataSet dataSet = new LineDataSet(entries, "XP u poslednjih 7 dana");
        dataSet.setColor(Color.parseColor("#3F51B5"));
        dataSet.setCircleColor(Color.parseColor("#303F9F"));
        dataSet.setValueTextSize(12f);

        LineData data = new LineData(dataSet);
        xpChart.setData(data);
        xpChart.getXAxis().setGranularity(1f);
        xpChart.getXAxis().setAxisMinimum(0f);
        xpChart.getXAxis().setAxisMaximum(6f); // sprečava negativan range
        xpChart.getDescription().setEnabled(false);
        xpChart.invalidate();
    }


    private int calculateLongestStreak(List<Task> tasks) {
        List<Long> completedDays = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(t.getCreatedAt());
                long day = c.get(Calendar.DAY_OF_YEAR) + 365L * c.get(Calendar.YEAR);
                completedDays.add(day);
            }
        }
        if (completedDays.isEmpty()) return 0;

        Collections.sort(completedDays);
        int streak = 1, max = 1;
        for (int i = 1; i < completedDays.size(); i++) {
            if (completedDays.get(i) == completedDays.get(i - 1) + 1) streak++;
            else streak = 1;
            if (streak > max) max = streak;
        }
        return max;
    }

    private void setupAverageDifficultyChart(List<Task> tasks) {
        // mapiraj kategoriju na (ukupno XP, broj zadataka)
        HashMap<String, int[]> categoryXp = new HashMap<>();

        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) {
                int[] xpData = categoryXp.getOrDefault(t.getCategoryId(), new int[]{0, 0});
                xpData[0] += t.getXp(); // ukupno XP
                xpData[1] += 1;        // broj zadataka
                categoryXp.put(t.getCategoryId(), xpData);
            }
        }

        // napravi linijski graf prosečne težine
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        for (String cat : categoryXp.keySet()) {
            int[] xpData = categoryXp.get(cat);
            float avgXp = (float) xpData[0] / xpData[1];
            entries.add(new Entry(i, avgXp));
            labels.add(cat);
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Prosečna težina zadataka po kategoriji");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setCircleColor(Color.parseColor("#F57C00"));
        dataSet.setValueTextSize(12f);
        dataSet.setLineWidth(2f);

        LineData data = new LineData(dataSet);
        avgDifficultyChart.setData(data);
        avgDifficultyChart.getXAxis().setGranularity(1f);
        avgDifficultyChart.getXAxis().setAxisMinimum(0f);
        avgDifficultyChart.getDescription().setEnabled(false);
        avgDifficultyChart.invalidate();
    }

}
