package com.example.habitforge.application.service;

import android.content.Context;

import com.example.habitforge.application.model.Boss;
import com.example.habitforge.application.model.Equipment;
import com.example.habitforge.data.repository.EquipmentRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(Context context) {
        this.equipmentRepository = new EquipmentRepository(context);
    }

    public interface EquipmentCallback {
        void onSuccess(Equipment equipment);
        void onFailure(Exception e);
    }

    public void getEquipmentById(String equipmentId, EquipmentCallback callback) {
        equipmentRepository.getEquipmentById(equipmentId, new EquipmentRepository.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                callback.onSuccess(equipment);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void calculateEquipmentPriceFromBoss(String userId, String equipmentId, boolean fullVictory, PriceCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1️⃣ Prvo uzmi aktivnog bossa
        db.collection("users").document(userId)
                .collection("bosses")
                .get()
                .addOnSuccessListener(query -> {
                    Boss activeBoss = null;
                    int maxLevelDefeated = 0;

                    for (var doc : query.getDocuments()) {
                        Boss b = doc.toObject(Boss.class);
                        if (b == null) continue;
                        if (!b.isDefeated()) activeBoss = b;
                        else if (b.getLevel() > maxLevelDefeated) maxLevelDefeated = b.getLevel();
                    }

                    // Ako nema aktivnog, vrati nula
                    if (activeBoss == null) {
                        callback.onSuccess(0.0);
                        return;
                    }

                    // 2️⃣ Izračunaj coinsReward
                    int coinsReward = calculateCoinsReward(activeBoss.getLevel(), fullVictory);

                    // 3️⃣ Učitaj equipment po ID-ju
                    equipmentRepository.getEquipmentById(equipmentId, new EquipmentRepository.EquipmentCallback() {
                        @Override
                        public void onSuccess(Equipment equipment) {
                            if (equipment == null) {
                                callback.onFailure(new Exception("Equipment not found"));
                                return;
                            }

                            double priceMultiplier = equipment.getPriceMultiplier();
                            double finalPrice = coinsReward * priceMultiplier;

                            callback.onSuccess(finalPrice);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }


    private int calculateCoinsReward(int bossLevel, boolean fullVictory) {
        int baseCoins = 200;
        int levelMultiplier = (int) Math.pow(1.2, bossLevel - 1);
        int coinsReward = baseCoins * levelMultiplier;
        if (!fullVictory) coinsReward /= 2;
        return coinsReward;
    }
    public interface PriceCallback {
        void onSuccess(double finalPrice);
        void onFailure(Exception e);
    }

}
