package com.example.habitforge.presentation.activity.ui.progress;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.session.SessionManager;
import com.example.habitforge.data.repository.UserRepository;


public class UserProgressFragment extends Fragment {

        private TextView tvTitle, tvLevel, tvPP, tvXPInfo;
        private ProgressBar progressXP;
        private UserRepository userRepository;

        public UserProgressFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_user_progress, container, false);

            tvTitle = view.findViewById(R.id.tvTitle);
            tvLevel = view.findViewById(R.id.tvLevel);
            tvPP = view.findViewById(R.id.tvPP);
            tvXPInfo = view.findViewById(R.id.tvXPInfo);
            progressXP = view.findViewById(R.id.progressXP);

            userRepository = new UserRepository(requireContext());

            loadUserProgress();

            return view;
        }

        private void loadUserProgress() {
            SessionManager session = new SessionManager(requireContext());
            String userId = session.getUserId();

            if (userId != null) {
                userRepository.getUserById(userId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        if (user == null) return;

                        tvTitle.setText(user.getTitle());
                        tvLevel.setText("Level " + user.getLevel());
                        tvPP.setText("PP: " + user.getPowerPoints());

                        int currentXP = user.getExperiencePoints();
                        int nextLevelXP = calculateXPForNextLevel(user.getLevel());
                        tvXPInfo.setText("XP: " + currentXP + " / " + nextLevelXP);

                        // postavi progress bar
                        int progress = (int) ((double) currentXP / nextLevelXP * 100);
                        progressXP.setProgress(progress);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("ProgressFragment", "Failed to load user", e);
                    }
                });
            }
        }

        // ista formula koju koristiš za level
        private int calculateXPForNextLevel(int currentLevel) {
            int xp = 200;
            for (int i = 1; i < currentLevel; i++) {
                xp = (int) (Math.ceil((xp * 2 + xp / 2.0) / 100.0) * 100);
            }
            return xp;
        }
    }
