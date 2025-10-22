package com.example.habitforge.presentation.activity.ui.mission;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habitforge.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.habitforge.R;
import com.example.habitforge.application.model.AllianceMission;
import com.example.habitforge.application.service.AllianceMissionService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Map;

public class AllianceMissionFragment extends Fragment {

    private TextView tvBossHp, tvRewardText, tvAllianceMissionTitle;
    private ProgressBar pbBossHp;
    private LinearLayout layoutTeamProgress;
    private MaterialButton buttonFinishMission;
    private MaterialCardView cardAllianceReward;
    private ImageView ivRewardChest, ivAllianceBoss;

    private AllianceMissionService missionService;
    private String allianceId;
    private String currentUserId;
    private TextView tvMissionTimeLeft;
    private AllianceMission mission;
    private ImageView ivBadgeReward;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔹 Inicijalizacija view elemenata
        tvBossHp = view.findViewById(R.id.tvBossHp);
        pbBossHp = view.findViewById(R.id.pbBossHp);
        tvAllianceMissionTitle = view.findViewById(R.id.tvAllianceMissionTitle);
        layoutTeamProgress = view.findViewById(R.id.layoutTeamProgress);
        buttonFinishMission = view.findViewById(R.id.buttonFinishMission);
        ivAllianceBoss = view.findViewById(R.id.ivAllianceBoss);
        cardAllianceReward = view.findViewById(R.id.cardAllianceReward);
        ivRewardChest = view.findViewById(R.id.ivRewardChest);
        tvRewardText = view.findViewById(R.id.tvRewardText);
        tvMissionTimeLeft = view.findViewById(R.id.tvMissionTimeLeft);
        ivBadgeReward = view.findViewById(R.id.ivBadgeReward);



        missionService = new AllianceMissionService(getContext());
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔹 Preuzmi ID saveza iz Bundle-a
        allianceId = getArguments() != null ? getArguments().getString("allianceId") : null;
        if (allianceId == null) {
            Toast.makeText(getContext(), "Alliance ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Učitaj podatke o misiji
        loadMissionData();

        // 🔹 Finish dugme (samo za vođu)
//        buttonFinishMission.setOnClickListener(v -> {
//            missionService.finishMission(allianceId,
//                    () -> {
//                        Toast.makeText(getContext(), "Mission completed successfully!", Toast.LENGTH_SHORT).show();
//                        showReward();
//                    },
//                    () -> Toast.makeText(getContext(), "Error finishing mission!", Toast.LENGTH_SHORT).show());
//        });
        buttonFinishMission.setOnClickListener(v -> {
            missionService.finishMission(mission.getId(),
                    () -> {
                        Toast.makeText(getContext(), "Mission completed successfully!", Toast.LENGTH_SHORT).show();
                        showReward();

                        com.example.habitforge.application.service.TaskService taskService =
                                new com.example.habitforge.application.service.TaskService(getContext());
                        taskService.checkSpecialMissionBonus(allianceId, currentUserId);
                    },
                    () -> Toast.makeText(getContext(), "Error finishing mission!", Toast.LENGTH_SHORT).show()
            );
        });


    }

    private void loadMissionData() {
        missionService.getAllianceMissions(allianceId, new AllianceMissionService.MissionListCallback() {
            @Override
            public void onSuccess(java.util.List<AllianceMission> missions) {
                if (missions.isEmpty()) {
                    tvAllianceMissionTitle.setText("No active mission");
                    buttonFinishMission.setVisibility(View.GONE);
                    return;
                }

                mission = missions.get(0);

                //Prikaz osnovnih informacija
                tvAllianceMissionTitle.setText(mission.getName());
                int currentHp = mission.getBossHP();
                int maxHp = 100 * mission.getMemberCount();
                tvBossHp.setText(currentHp + " / " + maxHp);

                int progressPercent = 100 - (int) ((currentHp * 100.0) / maxHp);
                pbBossHp.setProgress(progressPercent);

                // koliko je ostalo do kraja misije
                long now = System.currentTimeMillis();
                long remainingMillis = mission.getEndTime() - now;
                long remainingDays = remainingMillis / (1000 * 60 * 60 * 24);

                if (remainingDays > 0) {
                    tvMissionTimeLeft.setText("⏳ " + remainingDays + " days left");

                    //misija traje i finish je onemogucen
                    buttonFinishMission.setEnabled(false);
                    buttonFinishMission.setAlpha(0.5f);
                    buttonFinishMission.setText("Mission in progress...");
                } else {
                    tvMissionTimeLeft.setText("✅ Mission ended");

                    //tek nakon 14 se zavrsava
                    buttonFinishMission.setEnabled(true);
                    buttonFinishMission.setAlpha(1f);
                    buttonFinishMission.setText("Finish Mission");
                }



                // 🔹 Prikaz doprinosa članova
                layoutTeamProgress.removeAllViews();
                UserRepository userRepo = new UserRepository(getContext());

                if (mission.getProgress() != null && !mission.getProgress().isEmpty()) {
                    for (Map.Entry<String, Integer> entry : mission.getProgress().entrySet()) {
                        String userId = entry.getKey();
                        int damage = entry.getValue();

                        // 🔹 Inflater za prikaz jednog reda doprinosa
                        View progressItem = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_alliance_progress, layoutTeamProgress, false);

                        TextView tvName = progressItem.findViewById(R.id.tv_member_name);
                        TextView tvDamage = progressItem.findViewById(R.id.tv_member_damage);

                        tvDamage.setText("-" + damage + " HP");

                        // 🔹 Učitaj korisnika po ID-ju
                        userRepo.getUserById(userId, new UserRepository.UserCallback() {
                            @Override
                            public void onSuccess(com.example.habitforge.application.model.User user) {
                                tvName.setText("🧍 " + user.getUsername());

                            }

                            @Override
                            public void onFailure(Exception e) {
                                tvName.setText("Unknown user");
                            }
                        });

                        layoutTeamProgress.addView(progressItem);
                    }
                } else {
                    TextView noProgress = new TextView(getContext());
                    noProgress.setText("No progress yet");
                    noProgress.setTextColor(getResources().getColor(R.color.gray, null));
                    layoutTeamProgress.addView(noProgress);
                }


                // 🔹 Ako je boss poražen (HP <= 0)
                if (currentHp <= 0) {
                    showReward();
                    if (currentHp <= 0) {
                        showReward();

                        // ⚡ Automatski završi misiju ako boss ima 0 HP
                        if (mission != null && !mission.isCompleted()) {
                            missionService.finishMission(mission.getId(),
                                    () -> {
                                        Toast.makeText(getContext(), "Mission auto-completed (boss defeated)!", Toast.LENGTH_SHORT).show();
                                    },
                                    () -> Toast.makeText(getContext(), "Error finishing mission automatically!", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }

                }
            }

            @Override
            public void onFailure(Exception e) {
                tvAllianceMissionTitle.setText("Error loading mission data");
            }
        });
    }

    private void showReward() {
        cardAllianceReward.setVisibility(View.VISIBLE);
        ivRewardChest.setImageResource(R.drawable.new_chest_open);
        tvRewardText.setText(
                "🎉 Alliance Victory!\n\n" +
                        "🏅 Reward summary:\n" +
                        "• 🧪 Victory Potion (+10% boost)\n" +
                        "• 👕 Champion Outfit (+15%, lasts 5 fights)\n" +
                        "• 💰 +50% of next boss reward\n" +
                        "• 🏆 New badge earned!"
        );
        // 🔹 Prikaži poslednji osvojeni bedž
        UserRepository userRepo = new UserRepository(getContext());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRepo.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(com.example.habitforge.application.model.User user) {
                List<String> badges = user.getBadges();

                if (badges != null && !badges.isEmpty()) {
                    // Uzimamo poslednji bedž iz liste
                    String lastBadge = badges.get(badges.size() - 1);

                    String badgeKey = lastBadge
                            .replace(".png", "")
                            .replace(".jpg", "")
                            .replace("R.drawable.", "")
                            .trim()
                            .toLowerCase();

                    int resId = getResources().getIdentifier(badgeKey, "drawable", requireContext().getPackageName());
                    Log.d("BadgeDebug", "lastBadge=" + lastBadge + " | resId=" + resId);

                    if (resId != 0) {
                        ivBadgeReward.setImageResource(resId);
                        ivBadgeReward.setVisibility(View.VISIBLE);
                    } else {
                        ivBadgeReward.setVisibility(View.GONE);
                    }
                } else {
                    ivBadgeReward.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                ivBadgeReward.setVisibility(View.GONE);
            }
        });

//        buttonFinishMission.setVisibility(View.GONE);
    }

}
