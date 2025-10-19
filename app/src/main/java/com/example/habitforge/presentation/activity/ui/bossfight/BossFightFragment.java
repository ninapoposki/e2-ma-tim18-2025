package com.example.habitforge.presentation.activity.ui.bossfight;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.habitforge.R;
import com.example.habitforge.application.model.Boss;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.application.model.User;
import com.example.habitforge.application.model.UserEquipment;
import com.example.habitforge.application.service.BossService;
import com.example.habitforge.application.service.UserService;

import com.example.habitforge.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BossFightFragment extends Fragment implements SensorEventListener {

    // UI Components
    private TextView tvBossTitle, tvHitChance, tvBossHP, tvUserPower;
    private TextView tvAttemptsLeft, tvAttackResult, tvShakePrompt;
    private TextView tvCoinsReward, tvItemReward;
    private ProgressBar pbBossHP, pbUserPP;
    private ImageView ivBoss, ivChest;
    private MaterialButton btnAttack, btnNextBoss;
    private LinearLayout llEquipmentIcons, llRewardContent;
    private View cardReward, cardAttack;

    // Services
    private UserService userService;
    private BossService bossService;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Game State
    private User currentUser;
    private Boss currentBoss;
    private int attemptsLeft = 5;
    private int totalAttempts = 5;
    private int userTotalPP = 0;
    private boolean bossDefeated = false;
    private boolean chestOpened = false;
    private int coinsReward = 0;
    private String itemReward = null;

    // Sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 800;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_boss_fight, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents(view);
        initializeSensor();
        loadBossFight();
    }

    private void initializeComponents(View view) {
        // Initialize services
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userService = new UserService(requireContext());
        bossService = new BossService();

        // Bind UI components
        tvBossTitle = view.findViewById(R.id.tvBossTitle);
        tvHitChance = view.findViewById(R.id.tvHitChance);
        tvBossHP = view.findViewById(R.id.tvBossHP);
        tvUserPower = view.findViewById(R.id.tvUserPower);
        tvAttemptsLeft = view.findViewById(R.id.tvAttemptsLeft);
        tvAttackResult = view.findViewById(R.id.tvAttackResult);
        tvShakePrompt = view.findViewById(R.id.tvShakePrompt);
        tvCoinsReward = view.findViewById(R.id.tvCoinsReward);
        tvItemReward = view.findViewById(R.id.tvItemReward);

        pbBossHP = view.findViewById(R.id.pbBossHP);
        pbUserPP = view.findViewById(R.id.pbUserPP);

        ivBoss = view.findViewById(R.id.ivBoss);
        ivChest = view.findViewById(R.id.ivChest);

        btnAttack = view.findViewById(R.id.btnAttack);
        btnNextBoss = view.findViewById(R.id.btnNextBoss);

        llEquipmentIcons = view.findViewById(R.id.llEquipmentIcons);
        llRewardContent = view.findViewById(R.id.llRewardContent);

        cardReward = view.findViewById(R.id.cardReward);
        cardAttack = view.findViewById(R.id.cardAttack);

        // Set listeners
        btnAttack.setOnClickListener(v -> performAttack());
        btnNextBoss.setOnClickListener(v -> startNextBoss());
    }

    private void initializeSensor() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void loadBossFight() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        // Load user data
        userService.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                calculateUserPower();
                loadCurrentBoss();
            }

            @Override
            public void onFailure(Exception e) {
                tvAttackResult.setText("Error loading user data");
                tvAttackResult.setVisibility(View.VISIBLE);
            }
        });
    }

    private void calculateUserPower() {
        if (currentUser == null) return;

        userTotalPP = currentUser.getPowerPoints();

        // Add equipment bonuses
        List<UserEquipment> activeEquipment = currentUser.getActiveEquipment();
        if (activeEquipment != null) {
            for (UserEquipment equipment : activeEquipment) {
                userTotalPP += equipment.getPowerBonus();
            }
            displayEquipment(activeEquipment);
        }

        tvUserPower.setText(userTotalPP + " PP");
        updateUserPPBar();
    }

//    private void displayEquipment(List<UserEquipment> equipment) {
//        llEquipmentIcons.removeAllViews();
//
//        if (equipment == null || equipment.isEmpty()) {
//            TextView noEquip = new TextView(requireContext());
//            noEquip.setText("No active equipment");
//            noEquip.setTextColor(getResources().getColor(R.color.gray, null));
//            noEquip.setTextSize(16);
//            llEquipmentIcons.addView(noEquip);
//            return;
//        }
//
//        for (UserEquipment item : equipment) {
//            TextView equipIcon = new TextView(requireContext());
//
//            // 🔹 Ikonica — koristi simbol po tipu (ako nema, stavi podrazumevanu)
//            String icon;
//            switch (item.getType()) {
//                case POTION:
//                    icon = "🧪";
//                    break;
//                case CLOTHING:
//                    icon = "🧥";
//                    break;
//                case WEAPON:
//                    icon = "⚔️";
//                    break;
//                default:
//                    icon = "🎒";
//            }
//            equipIcon.setText(icon);
//
//            // 🔹 Stil
//            equipIcon.setTextSize(28);
//            equipIcon.setPadding(12, 8, 12, 8);
//
//            // 🔹 Tooltip — prikazuje više informacija pri dužem pritisku
//            equipIcon.setOnLongClickListener(v -> {
//                String info = "Type: " + item.getType() +
//                        "\nEffect: " + String.format("%.2f", item.getEffect() * 100) + "%" +
//                        "\nLevel: " + item.getLevel() +
//                        (item.isActive() ? "\nStatus: Active" : "\nStatus: Inactive");
//                Toast.makeText(requireContext(), info, Toast.LENGTH_SHORT).show();
//                return true;
//            });
//
//            llEquipmentIcons.addView(equipIcon);
//        }
//    }
private void displayEquipment(List<UserEquipment> equipmentList) {
    llEquipmentIcons.removeAllViews();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    if (equipmentList == null || equipmentList.isEmpty()) {
        TextView noEquip = new TextView(requireContext());
        noEquip.setText("No active equipment");
        noEquip.setTextColor(getResources().getColor(R.color.gray, null));
        noEquip.setTextSize(16);
        llEquipmentIcons.addView(noEquip);
        return;
    }

    for (UserEquipment userEquip : equipmentList) {
        db.collection("equipment")
                .document(userEquip.getEquipmentId())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Equipment equipment = document.toObject(Equipment.class);

                        if (equipment != null) {
                            ImageView equipIcon = new ImageView(requireContext());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
                            params.setMargins(8, 8, 8, 8);
                            equipIcon.setLayoutParams(params);

                            String imageName = equipment.getImage();
                            if (imageName != null && !imageName.isEmpty()) {
                                // 🔹 1️⃣ Ako je URL (počinje sa http)
                                if (imageName.startsWith("http")) {
                                    Glide.with(this)
                                            .load(imageName)
                                            .placeholder(R.drawable.unavailable)
                                            .into(equipIcon);
                                }
                                // 🔹 2️⃣ Ako je ime resursa (npr. "color1", "armor_blue", itd.)
                                else {
                                    int resId = getResources().getIdentifier(
                                            imageName, "drawable", requireContext().getPackageName());

                                    if (resId != 0) {
                                        equipIcon.setImageResource(resId);
                                    } else {
                                        equipIcon.setImageResource(R.drawable.unavailable);
                                    }
                                }
                            } else {
                                equipIcon.setImageResource(R.drawable.unavailable);
                            }

                            // 🔹 Tooltip info (duži pritisak)
                            equipIcon.setOnLongClickListener(v -> {
                                String info = "Name: " + equipment.getName() +
                                        "\nType: " + equipment.getType() +
                                        "\nEffect: " + String.format("%.2f", userEquip.getEffect() * 100) + "%" +
                                        (userEquip.isActive() ? "\nStatus: Active" : "\nStatus: Inactive");
                                Toast.makeText(requireContext(), info, Toast.LENGTH_SHORT).show();
                                return true;
                            });

                            llEquipmentIcons.addView(equipIcon);
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}




    private void updateUserPPBar() {
        // Normalize PP to 0-100 scale (assuming max PP around 200)
        int progress = Math.min(100, (userTotalPP * 100) / 200);
        pbUserPP.setProgress(progress);
    }

    private void loadCurrentBoss() {
        if (currentBoss != null && !currentBoss.isDefeated()) {
            Toast.makeText(requireContext(), "⚠️ The same boss returns!", Toast.LENGTH_SHORT).show();
            updateUI();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        int userLevel = currentUser.getLevel();

        db.collection("users").document(userId).collection("bosses")
                .document("currentBoss")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentBoss = document.toObject(Boss.class);

                        if (currentBoss != null && !currentBoss.isDefeated()) {
                            attemptsLeft = document.getLong("attemptsLeft") != null ?
                                    document.getLong("attemptsLeft").intValue() : totalAttempts;
                            updateUI();
                            return;
                        }
                        else if (currentBoss != null && currentBoss.isDefeated()) {
                            createNewBoss(currentBoss.getLevel() + 1);
                            return;
                        }
                    }
                    createNewBoss(1);

                });

    }

    private void createNewBoss(int level) {
        int bossHP = calculateBossHP(level);
        currentBoss = new Boss(level, bossHP, bossHP);
        attemptsLeft = totalAttempts;
        saveBossProgress();
        updateUI();
    }

    private int calculateBossHP(int level) {
        if (level == 1) return 200;

        int prevHP = calculateBossHP(level - 1);
        return prevHP * 2 + prevHP / 2; // nextHP = prevHP * 2.5
    }

    private void updateUI() {
        if (currentBoss == null) return;

        // Boss title
        String bossEmoji = getBossEmoji(currentBoss.getLevel());
        tvBossTitle.setText("Boss Level " + currentBoss.getLevel() + " " + bossEmoji);

        // 🔹 Uzimamo realan success rate iz Firestore-a
        if (currentUser != null) {
            userService.getSuccessRate(currentUser.getUserId(), new UserRepository.SuccessRateCallback() {
                @Override
                public void onSuccess(int rate) {
                    tvHitChance.setText("Hit chance: " + rate + "%");
                }

                @Override
                public void onFailure(Exception e) {
                    tvHitChance.setText("Hit chance: 50%");
                }
            });
        } else {
            tvHitChance.setText("Hit chance: 50%");
        }

        // Boss HP
        tvBossHP.setText(currentBoss.getCurrentHP() + " / " + currentBoss.getMaxHP());
        int hpPercent = (currentBoss.getCurrentHP() * 100) / currentBoss.getMaxHP();
        pbBossHP.setProgress(hpPercent);

        // Attempts
        tvAttemptsLeft.setText("Attempts: " + attemptsLeft + "/" + totalAttempts);

        // Enable/disable attack button
        btnAttack.setEnabled(attemptsLeft > 0 && !bossDefeated);
    }


    private String getBossEmoji(int level) {
        String[] emojis = {"🐉", "👹", "💀", "🦖", "🐲", "👾", "🤖", "🧟"};
        return emojis[(level - 1) % emojis.length];
    }

    private void performAttack() {
        if (attemptsLeft <= 0 || currentBoss == null || bossDefeated) return;

        // uzimamo success rate iz Firestore-a (realni procenat uspešnosti)
        userService.getSuccessRate(currentUser.getUserId(), new UserRepository.SuccessRateCallback() {
            @Override
            public void onSuccess(int hitChance) {
                performAttackWithChance(hitChance); // nastavlja borbu sa realnim procentom
            }

            @Override
            public void onFailure(Exception e) {
                performAttackWithChance(50); // fallback na 50% ako ne može da se učita
            }
        });
    }
    private void performAttackWithChance(int hitChance) {
        userService.useAllActivePotions(currentUser, null);
        userService.useAllActiveClothing(currentUser, null);
        calculateUserPower(); // samo jednom, čisto

        attemptsLeft--;

        Random random = new Random();
        int roll = random.nextInt(100);

        if (roll < hitChance) {
            int damage = userTotalPP;
            currentBoss.takeDamage(damage);

            tvAttackResult.setText("💥 You hit for −" + damage + " HP!");
            tvAttackResult.setTextColor(getResources().getColor(R.color.purple_500, null));
            animateBossHit();

            if (currentBoss.getCurrentHP() <= 0) {
                bossDefeated = true;
                handleBossDefeated();
            }
        } else {
            tvAttackResult.setText("❌ Missed!");
            tvAttackResult.setTextColor(getResources().getColor(R.color.gray, null));
            animateBossDodge();
        }

        tvAttackResult.setVisibility(View.VISIBLE);
        updateUI();
        saveBossProgress();

        if (attemptsLeft <= 0 && !bossDefeated) {
            handleAttemptsExhausted();
        }
    }




    private void animateBossHit() {
        ivBoss.startAnimation(AnimationUtils.loadAnimation(requireContext(),
                android.R.anim.fade_in));

        ObjectAnimator shake = ObjectAnimator.ofFloat(ivBoss, "translationX",
                0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(500);
        shake.start();
    }

    private void animateBossDodge() {
        ObjectAnimator fade = ObjectAnimator.ofFloat(ivBoss, "alpha", 1f, 0.5f, 1f);
        fade.setDuration(300);
        fade.start();
    }

    private void handleBossDefeated() {
        calculateRewards(true);
        showRewardScreen();
        saveRewardsToUser();
    }

    private void handleAttemptsExhausted() {
        if (currentBoss.getCurrentHP() <= currentBoss.getMaxHP() * 0.5) {
            // Partial rewards if boss HP <= 50%
            calculateRewards(false);
            showRewardScreen();
            saveRewardsToUser();
        } else {
            tvAttackResult.setText("💔 Boss survived! No rewards.");
            tvAttackResult.setVisibility(View.VISIBLE);
            btnAttack.setEnabled(false);
            // Omogući "Next Boss" samo ako je trenutni boss poražen i korisnik je prešao sledeći nivo
            if (bossDefeated && currentUser.getLevel() > currentBoss.getLevel()) {
                btnNextBoss.setEnabled(true);
                btnNextBoss.setVisibility(View.VISIBLE);
            } else {
                btnNextBoss.setEnabled(false);
                btnNextBoss.setVisibility(View.GONE);
            }

        }
    }

    private void calculateRewards(boolean fullVictory) {
        // Calculate coins
        int baseCoins = 200;
        int levelMultiplier = (int) Math.pow(1.2, currentBoss.getLevel() - 1);
        coinsReward = baseCoins * levelMultiplier;

        if (!fullVictory) {
            coinsReward /= 2; // Half rewards for partial victory
        }

        // 20% chance for equipment reward
        Random random = new Random();
        if (random.nextInt(100) < 20) {
            if (random.nextInt(100) < 95) {
                // 95% chance for clothing
                itemReward = "New Armor 👕";
            } else {
                // 5% chance for weapon
                itemReward = "New Weapon ⚔️";
            }
        }
    }

    private void showRewardScreen() {
        cardAttack.setVisibility(View.GONE);
        cardReward.setVisibility(View.VISIBLE);

        String promptText = bossDefeated ?
                "🎉 Victory! Shake to open chest!" :
                "⚡ Partial Victory! Shake to open chest!";
        tvShakePrompt.setText(promptText);

        // Register shake sensor
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void openChest() {
        if (chestOpened) return;
        chestOpened = true;

        // Animate chest opening
        ivChest.setImageResource(R.drawable.new_chest_open);
        ObjectAnimator scale = ObjectAnimator.ofFloat(ivChest, "scaleX", 1f, 1.2f, 1f);
        scale.setDuration(500);
        scale.start();

        // Show rewards
        tvShakePrompt.setVisibility(View.GONE);
        llRewardContent.setVisibility(View.VISIBLE);

        tvCoinsReward.setText("+" + coinsReward + " Coins 💰");

        if (itemReward != null) {
            tvItemReward.setText("+ " + itemReward);
            tvItemReward.setVisibility(View.VISIBLE);
        } else {
            tvItemReward.setVisibility(View.GONE);
        }

        // Unregister sensor
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void saveRewardsToUser() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("coins", currentUser.getCoins() + coinsReward);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Optionally add equipment to inventory if itemReward exists
                    if (itemReward != null) {
                        addItemToInventory(userId);
                    }
                });
    }

    private void addItemToInventory(String userId) {
        // Create new equipment item and add to user's inventory
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("name", itemReward);
        newItem.put("powerBonus", 10 + currentBoss.getLevel() * 5);
        newItem.put("acquiredAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .collection("inventory")
                .add(newItem);
    }

    private void saveBossProgress() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null || currentBoss == null) return;

        Map<String, Object> bossData = new HashMap<>();
        bossData.put("level", currentBoss.getLevel());
        bossData.put("currentHP", currentBoss.getCurrentHP());
        bossData.put("maxHP", currentBoss.getMaxHP());
        bossData.put("defeated", bossDefeated);
        bossData.put("attemptsLeft", attemptsLeft);

        db.collection("users").document(userId)
                .collection("bosses")
                .document("currentBoss")
                .set(bossData);
    }

    private void startNextBoss() {
        if (!bossDefeated) {
            Toast.makeText(requireContext(), "⚔️ Defeat the current boss first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getLevel() <= currentBoss.getLevel()) {
            Toast.makeText(requireContext(), "📈 Complete the next level to unlock a new boss!", Toast.LENGTH_LONG).show();
            return;
        }

        int nextLevel = currentBoss.getLevel() + 1;
        bossDefeated = false;
        chestOpened = false;
        coinsReward = 0;
        itemReward = null;

        cardReward.setVisibility(View.GONE);
        cardAttack.setVisibility(View.VISIBLE);
        tvAttackResult.setVisibility(View.GONE);
        ivChest.setImageResource(R.drawable.new_chest_close);
        llRewardContent.setVisibility(View.GONE);
        tvShakePrompt.setVisibility(View.VISIBLE);

        createNewBoss(nextLevel);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = x * x + y * y + z * z;
            float accelerationMinusGravity = Math.abs(acceleration - SensorManager.GRAVITY_EARTH
                    * SensorManager.GRAVITY_EARTH);

            long currentTime = System.currentTimeMillis();

            if (accelerationMinusGravity > SHAKE_THRESHOLD) {
                if (currentTime - lastShakeTime > 1000) {
                    lastShakeTime = currentTime;

                    if (!chestOpened && cardReward.getVisibility() == View.VISIBLE) {
                        openChest();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null && !chestOpened && cardReward.getVisibility() == View.VISIBLE) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}