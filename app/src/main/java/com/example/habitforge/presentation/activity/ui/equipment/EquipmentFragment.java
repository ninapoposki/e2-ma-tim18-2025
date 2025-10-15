package com.example.habitforge.presentation.activity.ui.equipment;
import com.example.habitforge.databinding.FragmentEquipmentBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class EquipmentFragment extends Fragment {

    private FragmentEquipmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EquipmentViewModel equipmentViewModel =
                new ViewModelProvider(this).get(EquipmentViewModel.class);

        binding = FragmentEquipmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textEquipment;
        equipmentViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}