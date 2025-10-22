package com.example.habitforge.application.service;

import android.util.Log;

import com.example.habitforge.application.model.AllianceMission;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.model.enums.EquipmentType;
import com.example.habitforge.data.repository.AllianceMissionRepository;
import com.example.habitforge.data.repository.UserRepository;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AllianceMissionService {

    private final AllianceMissionRepository missionRepository;
    private final Context context;

    public AllianceMissionService(Context context) {
        this.missionRepository = new AllianceMissionRepository();
        this.context=context;
    }


    public void startMission(String allianceId, String name, String description, Runnable onSuccess, Runnable onFailure) {
        // provera da li ima aktivne misije
        missionRepository.getMissionsForAlliance(allianceId, new AllianceMissionRepository.AllianceMissionListCallback() {
            @Override
            public void onSuccess(List<AllianceMission> missions) {
                for (AllianceMission m : missions) {
                    if (m.isActive() && !m.isCompleted()) {
                        Log.w("AllianceMissionService", "Alliance already has an active mission.");
                        onFailure.run(); // prekini jer već postoji aktivna
                        return;
                    }
                }

                // 2ako nema aktivne,sve normalno
                missionRepository.getAllianceMemberCount(allianceId, memberCount -> {
                    int bossHP = 100 * Math.max(1, memberCount); // sigurnosno ako je null ili 0

                    AllianceMission mission = new AllianceMission(allianceId, name, description);
                    mission.setBossHP(bossHP);
                    mission.setMemberCount(memberCount);
                    mission.setActive(true);
                    mission.setCompleted(false);

                    // 14 dana trajanja
                    long twoWeeksMillis = 14L * 24 * 60 * 60 * 1000;
                    mission.setEndTime(System.currentTimeMillis() + twoWeeksMillis);

                    missionRepository.createAllianceMission(mission, success -> {
                        if (success) {
                            Log.i("AllianceMissionService", "Alliance mission created with HP: " + bossHP);
                            assignMissionToAllMembers(allianceId, mission, onSuccess, onFailure);
                        } else {
                            Log.e("AllianceMissionService", "Failed to create alliance mission.");
                            onFailure.run();
                        }
                    });
                }, error -> {
                    Log.e("AllianceMissionService", "Error fetching member count: " + error.getMessage());
                    onFailure.run();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMissionService", "Error checking existing missions: " + e.getMessage());
                onFailure.run();
            }
        });
    }



    public void applyMissionDamage(String allianceId, String userId, int damageValue) {
        missionRepository.getMissionsForAlliance(allianceId, new AllianceMissionRepository.AllianceMissionListCallback() {
            @Override
            public void onSuccess(List<AllianceMission> missions) {
                if (missions.isEmpty()) return;
                AllianceMission mission = missions.get(0);
                if (!mission.isActive()) return;

                // Smanji boss HP
                int newHp = Math.max(0, mission.getBossHP() - damageValue);
                mission.setBossHP(newHp);

                // Ažuriraj korisnikov napredak
                missionRepository.updateMemberProgress(mission.getId(), userId, damageValue, success -> {
                    if (success) {
                        missionRepository.updateBossHp(mission.getId(), newHp, result -> {
                            if (result)
                                Log.i("AllianceMissionService", "Boss HP reduced by " + damageValue);
                        });
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMissionService", "Failed to apply mission damage: " + e.getMessage());
            }
        });
    }


    public void addMemberProgress(String missionId, String userId, int value, Runnable onSuccess, Runnable onFailure) {
        missionRepository.updateMemberProgress(missionId, userId, value, success -> {
            if (!success) {
                onFailure.run();
                return;
            }

            // 💥 Kad je progress uspešno dodat, ažuriraj boss HP
            missionRepository.getMissionById(missionId, new AllianceMissionRepository.SingleMissionCallback() {
                @Override
                public void onSuccess(AllianceMission mission) {
                    int newHp = Math.max(0, mission.getBossHP() - value);
                    missionRepository.updateBossHp(missionId, newHp, result -> {
                        if (result) {
                            Log.i("AllianceMissionService", "Boss HP updated to: " + newHp);
                            onSuccess.run();
                        } else {
                            Log.e("AllianceMissionService", "Failed to update boss HP");
                            onFailure.run();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("AllianceMissionService", "Failed to get mission by ID: " + e.getMessage());
                    onFailure.run();
                }
            });
        });
    }
    private void distributeRewardsToMembers(AllianceMission mission) {
        if (mission == null || mission.getAllianceId() == null) {
            Log.w("AllianceMissionService", "⚠️ Cannot distribute rewards — mission or allianceId missing.");
            return;
        }

        AllianceMissionRepository repo = new AllianceMissionRepository();
        UserService userService = new UserService(context);

        repo.getAllianceMembers(mission.getAllianceId(), members -> {
            if (members == null || members.isEmpty()) {
                Log.w("AllianceMissionService", "⚠️ No members found for alliance.");
                return;
            }

            for (String userId : members) {
                userService.getUserById(userId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        if (user.getEquipment() == null) user.setEquipment(new ArrayList<>());
                        if (user.getBadges() == null) user.setBadges(new ArrayList<>());

                        int nextBossReward = 100;
                        user.setCoins(user.getCoins() + nextBossReward / 2);

                        List<UserEquipment> eq = user.getEquipment();

                        UserEquipment potion = new UserEquipment(
                                UUID.randomUUID().toString(),
                                "victory_potion",
                                EquipmentType.POTION
                        );
                        potion.setEffect(0.1);

                        UserEquipment clothing = new UserEquipment(
                                UUID.randomUUID().toString(),
                                "champion_outfit",
                                EquipmentType.CLOTHING,
                                0.15,
                                5
                        );

                        eq.add(potion);
                        eq.add(clothing);
                        user.setEquipment(eq);

                        user.addBadge();

                        userService.updateUser(user, success -> {
                            if (success) {
                                Log.i("AllianceMissionService", "🎁 Rewards given to " + user.getUsername());
                            } else {
                                Log.e("AllianceMissionService", "⚠️ Failed to update rewards for " + user.getUsername());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMissionService", "❌ Failed to load user " + userId + ": " + e.getMessage());
                    }
                });
            }

        }, error -> Log.e("AllianceMissionService", "❌ Failed to fetch alliance members: " + error.getMessage()));
    }


    public void getAllianceMissions(String allianceId, MissionListCallback callback) {
        missionRepository.getMissionsForAlliance(allianceId, new AllianceMissionRepository.AllianceMissionListCallback() {
            @Override
            public void onSuccess(List<AllianceMission> missions) {
                callback.onSuccess(missions);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void finishMission(String missionId, Runnable onSuccess, Runnable onFailure) {
        missionRepository.completeAllianceMission(missionId, success -> {
            if (success) {
                Log.i("AllianceMissionService", "✅ Mission successfully completed: " + missionId);

                // 🔹 Preuzmi misiju da znamo njen allianceId i članove
                missionRepository.getMissionById(missionId, new AllianceMissionRepository.SingleMissionCallback() {
                    @Override
                    public void onSuccess(AllianceMission mission) {
                        if (mission != null) {
                            // 🏆 Dodeli nagrade svima u savezu
                            distributeRewardsToMembers(mission);
                        }
                        onSuccess.run();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMissionService", "❌ Failed to fetch mission for rewards: " + e.getMessage());
                        onFailure.run();
                    }
                });

            } else {
                Log.e("AllianceMissionService", "⚠️ Error while completing the mission!");
                onFailure.run();
            }
        });
    }


    public void assignMissionToAllMembers(String allianceId, AllianceMission mission, Runnable onSuccess, Runnable onFailure) {
        missionRepository.getAllianceMembers(allianceId, members -> {
            missionRepository.assignMissionToMembers(members, mission, success -> {
                if (success) {
                    Log.i("AllianceMissionService", "Mission assigned to all alliance members.");
                    onSuccess.run();
                } else {
                    Log.e("AllianceMissionService", "Failed to assign mission to members.");
                    onFailure.run();
                }
            });
        }, error -> {
            Log.e("AllianceMissionService", "Error fetching alliance members: " + error.getMessage());
            onFailure.run();
        });
    }
    public void incrementTaskCount(String missionId, String userId) {
        FirebaseFirestore.getInstance()
                .collection("AllianceMissions")
                .document(missionId)
                .update("taskCount." + userId, FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.i("AllianceMission", "📈 Task count incremented for " + userId))
                .addOnFailureListener(e -> Log.e("AllianceMission", "❌ Failed to increment task count: " + e.getMessage()));
    }

    // --- Callback interfejs ---
    public interface MissionListCallback {
        void onSuccess(List<AllianceMission> missions);
        void onFailure(Exception e);
    }
}
