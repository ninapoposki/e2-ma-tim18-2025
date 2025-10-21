package com.example.habitforge.application.service;

import com.example.habitforge.application.model.Boss;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BossService {

    private final FirebaseFirestore db;
    private final UserRepository userRepository;

    public BossService(FirebaseFirestore db, UserRepository userRepository) {
        this.db = db;
        this.userRepository = userRepository;
    }
    public Boss createBoss(int level) {
        int maxHP = calculateBossHP(level);
        return new Boss(level, maxHP, maxHP);
    }
//    public void refreshBossIfNeeded(User user, Boss boss, Runnable onSuccess, Runnable onFailure) {
//        int userLevel = user.getLevel();
//
//        // 🔹 Ako je korisnik prešao na novi nivo i prethodni boss je poražen:
//        if (boss.isDefeated() && boss.getLevel() < userLevel) {
//            int nextLevel = boss.getLevel() + 1;
//            int newMaxHP = calculateBossHP(nextLevel);
//
//            boss.setLevel(nextLevel);
//            boss.setMaxHP(newMaxHP);
//            boss.setCurrentHP(newMaxHP);
//            boss.setDefeated(false);
//            boss.setAttemptsLeft(5);
//            boss.setRefreshedForLevel(userLevel);
//
//            saveBossToFirestore(user, boss, onSuccess, onFailure);
//            return;
//        }
//
//        // 🔸 Ako boss nije poražen, a korisnik je prešao nivo — treba samo da ga “refresuje”
//        if (!boss.isDefeated() && boss.getRefreshedForLevel() < userLevel) {
//            int newMaxHP = calculateBossHP(boss.getLevel());
//            boss.setCurrentHP(newMaxHP);
//            boss.setMaxHP(newMaxHP);
//            boss.setAttemptsLeft(5);
//            boss.setRefreshedForLevel(userLevel);
//
//            saveBossToFirestore(user, boss, onSuccess, onFailure);
//        } else {
//            onFailure.run();
//        }
//    }

    public void loadCurrentBoss(User user, BossLoadCallback callback) {
        userRepository.getCurrentBoss(user.getUserId(), new UserRepository.BossCallback() {
            @Override
            public void onSuccess(Boss boss) {
                if (boss == null) {
                    Boss newBoss = createBoss(1);
                    saveBossToFirestore(user.getUserId(), newBoss,
                            () -> callback.onLoaded(newBoss, true),
                            () -> callback.onError(new Exception("Failed to create boss 1")));
                    return;
                }

                if (boss.isDefeated() && user.getLevel() <= boss.getLevel()) {
                    callback.onLoaded(boss, false);
                    return;
                }

                if (boss.isDefeated() && user.getLevel() > boss.getLevel()) {
                    int nextLevel = boss.getLevel() + 1;
                    Boss nextBoss = createBoss(nextLevel);
                    saveBossToFirestore(user.getUserId(), nextBoss,
                            () -> callback.onLoaded(nextBoss, true),
                            () -> callback.onError(new Exception("Failed to create next boss")));
                    return;
                }

                if (!boss.isDefeated()
                        && user.getLevel() > boss.getRefreshedForLevel()
                        && (boss.getAttemptsLeft() <= 0 || boss.getCurrentHP() <= 0)) {
                    boss.setCurrentHP(boss.getMaxHP());
                    boss.setAttemptsLeft(5);
                    boss.setDefeated(false);
                    boss.setRefreshedForLevel(user.getLevel());

                    saveBossToFirestore(user.getUserId(), boss,
                            () -> callback.onLoaded(boss, true),
                            () -> callback.onError(new Exception("Failed to refresh boss for next level")));
                    return;
                }

                // 🔸 Ako boss nije poražen i korisnik je na istom nivou — koristi ga
                callback.onLoaded(boss, false);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onError(e);
            }
        });


}


    public void saveBossToFirestore(String userId, Boss boss, Runnable onSuccess, Runnable onFailure) {
        Map<String, Object> bossData = new HashMap<>();
        bossData.put("level", boss.getLevel());
        bossData.put("currentHP", boss.getCurrentHP());
        bossData.put("maxHP", boss.getMaxHP());
        bossData.put("defeated", boss.isDefeated());
        bossData.put("attemptsLeft", boss.getAttemptsLeft());
        bossData.put("refreshedForLevel", boss.getRefreshedForLevel());

        db.collection("users").document(userId)
                .collection("bosses")
                .document("boss_" + boss.getLevel())
                .set(bossData)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    public int calculateBossHP(int level) {
        if (level == 1) return 200;
        int prevHP = calculateBossHP(level - 1);
        return prevHP * 2 + prevHP / 2; // 2.5x
    }

    public interface BossLoadCallback {
        void onLoaded(Boss boss, boolean refreshed);
        void onError(Exception e);
    }
}
