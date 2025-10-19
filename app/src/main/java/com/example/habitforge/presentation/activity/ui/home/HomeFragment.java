package com.example.habitforge.presentation.activity.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitforge.application.service.TaskService;
import com.example.habitforge.databinding.FragmentHomeBinding;
import com.example.habitforge.presentation.activity.AddCategoryActivity;
import com.example.habitforge.presentation.activity.AddTaskActivity;
import com.example.habitforge.presentation.activity.CategoryListActivity;
import com.example.habitforge.presentation.activity.TaskCalendarActivity;
import com.example.habitforge.presentation.activity.TaskDetailsActivity;
import com.example.habitforge.presentation.activity.TaskListActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TaskService taskService = new TaskService(requireContext());
        taskService.markOldTasksAsUncompleted();


        binding.cardCreateTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTaskActivity.class);
            startActivity(intent);
        });

        binding.cardViewCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TaskCalendarActivity.class);
            startActivity(intent);
        });

        binding.cardViewList.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TaskListActivity.class);
            startActivity(intent);
        });


        binding.cardCreateCategory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryListActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
