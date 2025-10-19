package com.example.habitforge.presentation.activity.ui.alliance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Alliance;
import com.google.firebase.auth.FirebaseAuth;

public class AllianceFragment extends Fragment {

    private AllianceViewModel viewModel;
    private TextView textAllianceName, textLeader, textMembers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textAllianceName = view.findViewById(R.id.text_alliance_name);
        textLeader = view.findViewById(R.id.text_leader);
        textMembers = view.findViewById(R.id.text_members);

        viewModel = new ViewModelProvider(this).get(AllianceViewModel.class);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(), alliance -> {
            textAllianceName.setText(alliance.getName());
            textLeader.setText("Leader: " + alliance.getLeaderId());
            textMembers.setText("Members: " + String.join(", ", alliance.getMemberIds()));
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
        );

        viewModel.loadAllianceForCurrentUser(currentUserId);
    }
}
